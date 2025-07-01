package gr.aegean.icsd.fms.controller;

import gr.aegean.icsd.fms.model.dto.request.CreatePerformanceRequest;
import gr.aegean.icsd.fms.model.dto.request.UpdatePerformanceRequest;
import gr.aegean.icsd.fms.model.dto.request.SubmitPerformanceRequest;
import gr.aegean.icsd.fms.model.entity.Performance;
import gr.aegean.icsd.fms.model.entity.User;
import gr.aegean.icsd.fms.service.PerformanceService;
import gr.aegean.icsd.fms.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.security.Principal;
import java.util.List;

/**
 * REST controller for performance operations.
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Performance Management")
public class PerformanceController {

    private final PerformanceService performanceService;
    private final UserService userService;

    @PostMapping("/festivals/{festivalId}/performances")
    @Operation(summary = "Create performance", security = @SecurityRequirement(name = "bearer-jwt"))
    public ResponseEntity<Performance> createPerformance(@PathVariable Long festivalId,
                                                         @Valid @RequestBody CreatePerformanceRequest request,
                                                         Principal principal) {
        User creator = userService.findByUsername(principal.getName());
        // ensure DTO festivalId matches path
        request.setFestivalId(festivalId);
        Performance perf = performanceService.createPerformance(request, creator);
        return ResponseEntity.status(HttpStatus.CREATED).body(perf);
    }

    @GetMapping("/festivals/{festivalId}/performances")
    @Operation(summary = "List festival performances")
    public ResponseEntity<List<Performance>> listPerformances(@PathVariable Long festivalId) {
        return ResponseEntity.ok(performanceService.findByFestival(festivalId));
    }

    @GetMapping("/performances/{performanceId}")
    @Operation(summary = "Get performance details")
    public ResponseEntity<Performance> getPerformance(@PathVariable Long performanceId) {
        return ResponseEntity.ok(performanceService.findById(performanceId));
    }

    @PutMapping("/performances/{performanceId}")
    @Operation(summary = "Update performance", security = @SecurityRequirement(name = "bearer-jwt"))
    public ResponseEntity<Performance> updatePerformance(@PathVariable Long performanceId,
                                                         @Valid @RequestBody UpdatePerformanceRequest request,
                                                         Principal principal) {
        User updater = userService.findByUsername(principal.getName());
        Performance updated = performanceService.updatePerformance(performanceId, request, updater);
        return ResponseEntity.ok(updated);
    }

    @PostMapping("/performances/{performanceId}/submit")
    @Operation(summary = "Submit performance", security = @SecurityRequirement(name = "bearer-jwt"))
    public ResponseEntity<Performance> submitPerformance(@PathVariable Long performanceId,
                                                         @Valid @RequestBody SubmitPerformanceRequest request,
                                                         Principal principal) {
        User submitter = userService.findByUsername(principal.getName());
        Performance submitted = performanceService.submitPerformance(performanceId, request, submitter);
        return ResponseEntity.ok(submitted);
    }
}
