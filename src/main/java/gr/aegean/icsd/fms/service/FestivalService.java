package gr.aegean.icsd.fms.service;

import gr.aegean.icsd.fms.exception.InvalidStateException;
import gr.aegean.icsd.fms.exception.ResourceNotFoundException;
import gr.aegean.icsd.fms.exception.UnauthorizedException;
import gr.aegean.icsd.fms.model.dto.request.CreateFestivalRequest;
import gr.aegean.icsd.fms.model.dto.request.UpdateFestivalRequest;
import gr.aegean.icsd.fms.model.entity.Festival;
import gr.aegean.icsd.fms.model.entity.User;
import gr.aegean.icsd.fms.model.entity.UserRole;
import gr.aegean.icsd.fms.model.enums.FestivalState;
import gr.aegean.icsd.fms.model.enums.UserRoleType;
import gr.aegean.icsd.fms.repository.FestivalRepository;
import gr.aegean.icsd.fms.repository.UserRoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Service class for festival management operations.
 */
@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class FestivalService {
    
    private final FestivalRepository festivalRepository;
    private final UserRoleRepository userRoleRepository;
    private final UserService userService;
    
    /**
     * Create a new festival
     * @param request the creation request
     * @param creator the user creating the festival
     * @return the created festival
     */
    public Festival createFestival(CreateFestivalRequest request, User creator) {
        log.info("Creating festival '{}' by user {}", request.getName(), creator.getUsername());
        
        // Check if festival name already exists
        if (festivalRepository.existsByName(request.getName())) {
            throw new IllegalArgumentException("Festival name already exists: " + request.getName());
        }
        
        // Create festival
        Festival festival = new Festival();
        festival.setName(request.getName());
        festival.setDescription(request.getDescription());
        festival.setStartDate(request.getStartDate());
        festival.setEndDate(request.getEndDate());
        festival.setVenue(request.getVenue());
        festival.setVenueLayout(request.getVenueLayout());
        festival.setBudgetInfo(request.getBudgetInfo());
        festival.setVendorManagement(request.getVendorManagement());
        festival.setState(FestivalState.CREATED);
        
        Festival savedFestival = festivalRepository.save(festival);
        
        // Assign creator as ORGANIZER
        userService.assignRole(creator, savedFestival, UserRoleType.ORGANIZER);
        
        log.info("Festival '{}' created with ID {}", savedFestival.getName(), savedFestival.getFestivalId());
        return savedFestival;
    }
    
    /**
     * Update festival information
     * @param festivalId the festival ID
     * @param request the update request
     * @param updater the user performing the update
     * @return the updated festival
     */
    public Festival updateFestival(Long festivalId, UpdateFestivalRequest request, User updater) {
        log.info("Updating festival {} by user {}", festivalId, updater.getUsername());
        
        Festival festival = findById(festivalId);
        
        // Check if user is organizer
        if (!userService.hasRoleInFestival(updater.getUserId(), festivalId, UserRoleType.ORGANIZER)) {
            throw new UnauthorizedException(updater.getUsername(), "update festival", 
                                          "ORGANIZER", "Festival " + festivalId);
        }
        
        // Check if festival can be updated
        if (festival.getState() == FestivalState.ANNOUNCED) {
            throw new InvalidStateException("Festival", festival.getState().toString(), 
                                          "not ANNOUNCED", "update");
        }
        
        // Update fields if provided
        if (request.getName() != null) {
            // Check if new name is unique
            if (!festival.getName().equals(request.getName()) && 
                festivalRepository.existsByName(request.getName())) {
                throw new IllegalArgumentException("Festival name already exists: " + request.getName());
            }
            festival.setName(request.getName());
        }
        
        if (request.getDescription() != null) {
            festival.setDescription(request.getDescription());
        }
        
        if (request.getStartDate() != null) {
            festival.setStartDate(request.getStartDate());
        }
        
        if (request.getEndDate() != null) {
            festival.setEndDate(request.getEndDate());
        }
        
        if (request.getVenue() != null) {
            festival.setVenue(request.getVenue());
        }
        
        if (request.getVenueLayout() != null) {
            festival.setVenueLayout(request.getVenueLayout());
        }
        
        if (request.getBudgetInfo() != null) {
            festival.setBudgetInfo(request.getBudgetInfo());
        }
        
        if (request.getVendorManagement() != null) {
            festival.setVendorManagement(request.getVendorManagement());
        }
        
        return festivalRepository.save(festival);
    }
    
    /**
     * Delete a festival
     * @param festivalId the festival ID
     * @param deleter the user performing the deletion
     */
    public void deleteFestival(Long festivalId, User deleter) {
        log.info("Deleting festival {} by user {}", festivalId, deleter.getUsername());
        
        Festival festival = findById(festivalId);
        
        // Check if user is organizer
        if (!userService.hasRoleInFestival(deleter.getUserId(), festivalId, UserRoleType.ORGANIZER)) {
            throw new UnauthorizedException(deleter.getUsername(), "delete festival", 
                                          "ORGANIZER", "Festival " + festivalId);
        }
        
        // Check if festival can be deleted
        if (festival.getState() != FestivalState.CREATED) {
            throw new InvalidStateException("Festival", festival.getState().toString(), 
                                          "CREATED", "delete");
        }
        
        festivalRepository.delete(festival);
        log.info("Festival {} deleted", festivalId);
    }
    
    /**
     * Advance festival to next state
     * @param festivalId the festival ID
     * @param advancer the user advancing the state
     * @return the updated festival
     */
    public Festival advanceState(Long festivalId, User advancer) {
        log.info("Advancing state of festival {} by user {}", festivalId, advancer.getUsername());
        
        Festival festival = findById(festivalId);
        
        // Check if user is organizer
        if (!userService.hasRoleInFestival(advancer.getUserId(), festivalId, UserRoleType.ORGANIZER)) {
            throw new UnauthorizedException(advancer.getUsername(), "advance festival state", 
                                          "ORGANIZER", "Festival " + festivalId);
        }
        
        FestivalState currentState = festival.getState();
        FestivalState nextState = currentState.getNextState();
        
        if (nextState == null) {
            throw new InvalidStateException("Festival is already in final state: " + currentState);
        }
        
        // Additional validations based on state transitions
        switch (currentState) {
            case SUBMISSION:
                // Ensure all staff are assigned before moving to ASSIGNMENT
                long staffCount = userRoleRepository.countByFestivalAndRole(festivalId, UserRoleType.STAFF);
                if (staffCount == 0) {
                    throw new IllegalStateException("Cannot advance to ASSIGNMENT: No staff members assigned");
                }
                break;
            
            case ASSIGNMENT:
                // Could check if all performances have assigned staff
                break;
                
            // Add more validation as needed
        }
        
        festival.setState(nextState);
        Festival updated = festivalRepository.save(festival);
        
        log.info("Festival {} advanced from {} to {}", festivalId, currentState, nextState);
        return updated;
    }
    
    /**
     * Add organizers to festival
     * @param festivalId the festival ID
     * @param userIds list of user IDs to add as organizers
     * @param adder the user adding organizers
     */
    public void addOrganizers(Long festivalId, List<Long> userIds, User adder) {
        log.info("Adding {} organizers to festival {} by user {}", 
                userIds.size(), festivalId, adder.getUsername());
        
        Festival festival = findById(festivalId);
        
        // Check if user is organizer
        if (!userService.hasRoleInFestival(adder.getUserId(), festivalId, UserRoleType.ORGANIZER)) {
            throw new UnauthorizedException(adder.getUsername(), "add organizers", 
                                          "ORGANIZER", "Festival " + festivalId);
        }
        
        for (Long userId : userIds) {
            User user = userService.findById(userId);
            userService.assignRole(user, festival, UserRoleType.ORGANIZER);
        }
    }
    
    /**
     * Add staff to festival
     * @param festivalId the festival ID
     * @param userIds list of user IDs to add as staff
     * @param adder the user adding staff
     */
    public void addStaff(Long festivalId, List<Long> userIds, User adder) {
        log.info("Adding {} staff to festival {} by user {}", 
                userIds.size(), festivalId, adder.getUsername());
        
        Festival festival = findById(festivalId);
        
        // Check if user is organizer
        if (!userService.hasRoleInFestival(adder.getUserId(), festivalId, UserRoleType.ORGANIZER)) {
            throw new UnauthorizedException(adder.getUsername(), "add staff", 
                                          "ORGANIZER", "Festival " + festivalId);
        }
        
        // Staff can only be added before SUBMISSION state ends
        if (festival.getState().ordinal() > FestivalState.SUBMISSION.ordinal()) {
            throw new InvalidStateException("Festival", festival.getState().toString(), 
                                          "CREATED or SUBMISSION", "add staff");
        }
        
        for (Long userId : userIds) {
            User user = userService.findById(userId);
            userService.assignRole(user, festival, UserRoleType.STAFF);
        }
    }
    
    /**
     * Find festival by ID
     * @param festivalId the festival ID
     * @return the festival
     */
    @Transactional(readOnly = true)
    public Festival findById(Long festivalId) {
        return festivalRepository.findById(festivalId)
            .orElseThrow(() -> new ResourceNotFoundException("Festival", "id", festivalId));
    }
    
    /**
     * Find festival by name
     * @param name the festival name
     * @return the festival
     */
    @Transactional(readOnly = true)
    public Festival findByName(String name) {
        return festivalRepository.findByName(name)
            .orElseThrow(() -> new ResourceNotFoundException("Festival", "name", name));
    }
    
    /**
     * Search festivals
     * @param name name search term
     * @param description description search term
     * @param venue venue search term
     * @param startDate start date filter
     * @param endDate end date filter
     * @param pageable pagination
     * @return page of festivals
     */
    @Transactional(readOnly = true)
    public Page<Festival> searchFestivals(String name, String description, String venue,
                                         LocalDate startDate, LocalDate endDate, Pageable pageable) {
        return festivalRepository.searchFestivals(name, description, venue, startDate, endDate, pageable);
    }
    
    /**
     * Find festivals by user role
     * @param userId the user ID
     * @param role the role
     * @return list of festivals
     */
    @Transactional(readOnly = true)
    public List<Festival> findByUserRole(Long userId, UserRoleType role) {
        return festivalRepository.findByUserRole(userId, role);
    }
    
    /**
     * Find upcoming festivals
     * @return list of upcoming festivals
     */
    @Transactional(readOnly = true)
    public List<Festival> findUpcomingFestivals() {
        return festivalRepository.findUpcomingFestivals(LocalDate.now());
    }
    
    /**
     * Find ongoing festivals
     * @return list of ongoing festivals
     */
    @Transactional(readOnly = true)
    public List<Festival> findOngoingFestivals() {
        return festivalRepository.findOngoingFestivals(LocalDate.now());
    }
}