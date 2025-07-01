package gr.aegean.icsd.fms.controller;

import gr.aegean.icsd.fms.model.dto.request.CreateFestivalRequest;
import gr.aegean.icsd.fms.model.dto.request.UpdateFestivalRequest;
import gr.aegean.icsd.fms.model.dto.response.FestivalResponse;
import gr.aegean.icsd.fms.model.entity.Festival;
import gr.aegean.icsd.fms.model.entity.User;
import gr.aegean.icsd.fms.model.enums.PerformanceState;
import gr.aegean.icsd.fms.model.enums.UserRole;
import gr.aegean.icsd.fms.repository.PerformanceRepository;
import gr.aegean.icsd.fms.security.CustomUserDetailsService;
import gr.aegean.icsd.fms.service.FestivalService;
import gr.aegean.icsd.fms.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.security.Principal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * REST controller for festival management operations.
 */
@RestController
@RequestMapping("/api/festivals")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Festival Management", description = "Endpoints for managing festivals")
public class FestivalController {
    
    private final FestivalService festivalService;
    private final UserService userService;
    private final PerformanceRepository performanceRepository;
    
    /**
     * Create a new festival
     */
    @PostMapping
    @Operation(summary = "Create a new festival", security = @SecurityRequirement(name = "bearer-jwt"))
    public ResponseEntity<FestivalResponse> createFestival(
            @Valid @RequestBody CreateFestivalRequest request,
            Principal principal) {
        
        log.info("Creating festival: {} by user: {}", request.getName(), principal.getName());
        
        User creator = userService.findByUsername(principal.getName());
        Festival festival = festivalService.createFestival(request, creator);
        
        // Get organizer info
        List<FestivalResponse.UserInfo> organizers = List.of(
            FestivalResponse.UserInfo.builder()
                .userId(creator.getUserId())
                .username(creator.getUsername())
                .fullName(creator.getFullName())
                .role(UserRole.ORGANIZER.name())
                .build()
        );
        
        FestivalResponse response = FestivalResponse.forOrganizer(
            festival, organizers, List.of(), 0, 0, 0);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    /**
     * Update festival information
     */
    @PutMapping("/{festivalId}")
    @Operation(summary = "Update festival information", security = @SecurityRequirement(name = "bearer-jwt"))
    public ResponseEntity<FestivalResponse> updateFestival(
            @PathVariable Long festivalId,
            @Valid @RequestBody UpdateFestivalRequest request,
            Principal principal) {
        
        log.info("Updating festival: {} by user: {}", festivalId, principal.getName());
        
        User updater = userService.findByUsername(principal.getName());
        Festival festival = festivalService.updateFestival(festivalId, request, updater);
        
        return ResponseEntity.ok(buildFestivalResponse(festival, updater));
    }
    
    /**
     * Delete a festival
     */
    @DeleteMapping("/{festivalId}")
    @Operation(summary = "Delete a festival", security = @SecurityRequirement(name = "bearer-jwt"))
    public ResponseEntity<Void> deleteFestival(
            @PathVariable Long festivalId,
            Principal principal) {
        
        log.info("Deleting festival: {} by user: {}", festivalId, principal.getName());
        
        User deleter = userService.findByUsername(principal.getName());
        festivalService.deleteFestival(festivalId, deleter);
        
        return ResponseEntity.noContent().build();
    }
    
    /**
     * Get festival details
     */
    @GetMapping("/{festivalId}")
    @Operation(summary = "Get festival details")
    public ResponseEntity<FestivalResponse> getFestival(
            @PathVariable Long festivalId,
            Principal principal) {
        
        Festival festival = festivalService.findById(festivalId);
        User viewer = principal != null ? userService.findByUsername(principal.getName()) : null;
        
        return ResponseEntity.ok(buildFestivalResponse(festival, viewer));
    }
    
    /**
     * Search festivals
     */
    @GetMapping
    @Operation(summary = "Search festivals")
    public ResponseEntity<Page<FestivalResponse>> searchFestivals(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) String venue,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @PageableDefault(size = 20, sort = "startDate") Pageable pageable,
            Principal principal) {
        
        Page<Festival> festivals = festivalService.searchFestivals(
            name, description, venue, startDate, endDate, pageable);
        
        User viewer = principal != null ? userService.findByUsername(principal.getName()) : null;
        
        Page<FestivalResponse> response = festivals.map(f -> buildFestivalResponse(f, viewer));
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Advance festival state
     */
    @PostMapping("/{festivalId}/advance-state")
    @Operation(summary = "Advance festival to next state", security = @SecurityRequirement(name = "bearer-jwt"))
    public ResponseEntity<FestivalResponse> advanceFestivalState(
            @PathVariable Long festivalId,
            Principal principal) {
        
        log.info("Advancing state of festival: {} by user: {}", festivalId, principal.getName());
        
        User advancer = userService.findByUsername(principal.getName());
        Festival festival = festivalService.advanceState(festivalId, advancer);
        
        return ResponseEntity.ok(buildFestivalResponse(festival, advancer));
    }
    
    /**
     * Add organizers to festival
     */
    @PostMapping("/{festivalId}/organizers")
    @Operation(summary = "Add organizers to festival", security = @SecurityRequirement(name = "bearer-jwt"))
    public ResponseEntity<Void> addOrganizers(
            @PathVariable Long festivalId,
            @RequestBody List<Long> userIds,
            Principal principal) {
        
        log.info("Adding organizers to festival: {} by user: {}", festivalId, principal.getName());
        
        User adder = userService.findByUsername(principal.getName());
        festivalService.addOrganizers(festivalId, userIds, adder);
        
        return ResponseEntity.ok().build();
    }
    
    /**
     * Add staff to festival
     */
    @PostMapping("/{festivalId}/staff")
    @Operation(summary = "Add staff members to festival", security = @SecurityRequirement(name = "bearer-jwt"))
    public ResponseEntity<Void> addStaff(
            @PathVariable Long festivalId,
            @RequestBody List<Long> userIds,
            Principal principal) {
        
        log.info("Adding staff to festival: {} by user: {}", festivalId, principal.getName());
        
        User adder = userService.findByUsername(principal.getName());
        festivalService.addStaff(festivalId, userIds, adder);
        
        return ResponseEntity.ok().build();
    }
    
    /**
     * Get my festivals (where I have a role)
     */
    @GetMapping("/my-festivals")
    @Operation(summary = "Get festivals where current user has a role", 
              security = @SecurityRequirement(name = "bearer-jwt"))
    public ResponseEntity<List<FestivalResponse>> getMyFestivals(
            @RequestParam(required = false) UserRole role,
            Principal principal) {
        
        User user = userService.findByUsername(principal.getName());
        
        List<Festival> festivals;
        if (role != null) {
            festivals = festivalService.findByUserRole(user.getUserId(), role);
        } else {
            // Get all festivals where user has any role
            festivals = userService.getUserRoles(user.getUserId()).stream()
                .map(ur -> ur.getFestival())
                .distinct()
                .collect(Collectors.toList());
        }
        
        List<FestivalResponse> response = festivals.stream()
            .map(f -> buildFestivalResponse(f, user))
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Build festival response based on viewer role
     */
    private FestivalResponse buildFestivalResponse(Festival festival, User viewer) {
        // For anonymous users or users without role in festival
        if (viewer == null || !userService.getUserRoles(viewer.getUserId()).stream()
                .anyMatch(ur -> ur.getFestival().equals(festival))) {
            
            // Get organizer names for visitors
            List<String> organizerNames = userService.findOrganizersByFestival(festival.getFestivalId())
                .stream()
                .map(ur -> ur.getUser().getFullName())
                .collect(Collectors.toList());
            
            return FestivalResponse.forVisitor(festival, organizerNames);
        }
        
        // For users with roles in the festival
        boolean isOrganizer = userService.hasRoleInFestival(
            viewer.getUserId(), festival.getFestivalId(), UserRole.ORGANIZER);
        
        if (isOrganizer) {
            // Get detailed information for organizers
            List<FestivalResponse.UserInfo> organizers = userService
                .findOrganizersByFestival(festival.getFestivalId()).stream()
                .map(ur -> FestivalResponse.UserInfo.builder()
                    .userId(ur.getUser().getUserId())
                    .username(ur.getUser().getUsername())
                    .fullName(ur.getUser().getFullName())
                    .role(ur.getRole().name())
                    .build())
                .collect(Collectors.toList());
            
            List<FestivalResponse.UserInfo> staff = userService
                .findStaffByFestival(festival.getFestivalId()).stream()
                .map(ur -> FestivalResponse.UserInfo.builder()
                    .userId(ur.getUser().getUserId())
                    .username(ur.getUser().getUsername())
                    .fullName(ur.getUser().getFullName())
                    .role(ur.getRole().name())
                    .build())
                .collect(Collectors.toList());
            
            // Get statistics
            long totalPerformances = performanceRepository.countByFestivalAndRole(
                festival.getFestivalId(), null);
            long scheduledPerformances = performanceRepository.countPerformancesByState(
                festival.getFestivalId(), PerformanceState.SCHEDULED);
            long pendingReviews = performanceRepository.countPerformancesByState(
                festival.getFestivalId(), PerformanceState.SUBMITTED);
            
            return FestivalResponse.forOrganizer(
                festival, organizers, staff, 
                (int) totalPerformances, (int) scheduledPerformances, (int) pendingReviews);
        }
        
        // For other authenticated users with roles
        return FestivalResponse.builder()
            .festivalId(festival.getFestivalId())
            .name(festival.getName())
            .description(festival.getDescription())
            .startDate(festival.getStartDate())
            .endDate(festival.getEndDate())
            .venue(festival.getVenue())
            .state(festival.getState())
            .createdAt(festival.getCreatedAt())
            .updatedAt(festival.getUpdatedAt())
            .build();
    }
}