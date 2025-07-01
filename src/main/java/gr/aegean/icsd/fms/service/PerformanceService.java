package gr.aegean.icsd.fms.service;

import gr.aegean.icsd.fms.exception.InvalidStateException;
import gr.aegean.icsd.fms.exception.ResourceNotFoundException;
import gr.aegean.icsd.fms.exception.UnauthorizedException;
import gr.aegean.icsd.fms.model.dto.request.CreatePerformanceRequest;
import gr.aegean.icsd.fms.model.dto.request.SubmitPerformanceRequest;
import gr.aegean.icsd.fms.model.entity.*;
import gr.aegean.icsd.fms.model.enums.FestivalState;
import gr.aegean.icsd.fms.model.enums.PerformanceState;
import gr.aegean.icsd.fms.model.enums.UserRole as Role;
import gr.aegean.icsd.fms.repository.PerformanceArtistRepository;
import gr.aegean.icsd.fms.repository.PerformanceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Service class for performance management operations.
 */
@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class PerformanceService {
    
    private final PerformanceRepository performanceRepository;
    private final PerformanceArtistRepository performanceArtistRepository;
    private final FestivalService festivalService;
    private final UserService userService;
    
    /**
     * Create a new performance
     * @param request the creation request
     * @param creator the user creating the performance
     * @return the created performance
     */
    public Performance createPerformance(CreatePerformanceRequest request, User creator) {
        log.info("Creating performance '{}' for festival {} by user {}", 
                request.getName(), request.getFestivalId(), creator.getUsername());
        
        Festival festival = festivalService.findById(request.getFestivalId());
        
        // Check if festival is accepting submissions
        if (!festival.isAcceptingSubmissions()) {
            throw new InvalidStateException("Festival", festival.getState().toString(), 
                                          "SUBMISSION", "create performance");
        }
        
        // Check if performance name is unique in festival
        if (performanceRepository.existsByFestivalFestivalIdAndName(
                request.getFestivalId(), request.getName())) {
            throw new IllegalArgumentException("Performance name already exists in this festival: " + 
                                             request.getName());
        }
        
        // Create performance
        Performance performance = new Performance();
        performance.setFestival(festival);
        performance.setName(request.getName());
        performance.setDescription(request.getDescription());
        performance.setGenre(request.getGenre());
        performance.setDuration(request.getDuration());
        performance.setTechnicalRequirements(request.getTechnicalRequirements());
        performance.setSetlist(request.getSetlist());
        performance.setMerchandiseItems(request.getMerchandiseItems());
        performance.setPreferredTimes(request.getPreferredTimes());
        performance.setState(PerformanceState.CREATED);
        
        Performance savedPerformance = performanceRepository.save(performance);
        
        // Assign creator as main artist
        PerformanceArtist mainArtist = PerformanceArtist.createMainArtist(savedPerformance, creator);
        performanceArtistRepository.save(mainArtist);
        
        // Assign ARTIST role to creator in this festival
        userService.assignRole(creator, festival, Role.ARTIST);
        
        log.info("Performance '{}' created with ID {}", savedPerformance.getName(), 
                savedPerformance.getPerformanceId());
        return savedPerformance;
    }
    
    /**
     * Update performance information
     * @param performanceId the performance ID
     * @param updates the update data
     * @param updater the user performing the update
     * @return the updated performance
     */
    public Performance updatePerformance(Long performanceId, CreatePerformanceRequest updates, 
                                       User updater) {
        log.info("Updating performance {} by user {}", performanceId, updater.getUsername());
        
        Performance performance = findById(performanceId);
        
        // Check if user is an artist of this performance
        if (!performanceArtistRepository.existsByPerformancePerformanceIdAndUserUserId(
                performanceId, updater.getUserId())) {
            throw new UnauthorizedException(updater.getUsername(), "update performance", 
                                          "ARTIST of performance", "Performance " + performanceId);
        }
        
        // Check if performance can be edited
        if (!performance.isEditable()) {
            throw new InvalidStateException("Performance", performance.getState().toString(), 
                                          "CREATED", "update");
        }
        
        // Update fields if provided
        if (updates.getName() != null) {
            // Check if new name is unique in festival
            if (!performance.getName().equals(updates.getName()) && 
                performanceRepository.existsByFestivalFestivalIdAndName(
                    performance.getFestival().getFestivalId(), updates.getName())) {
                throw new IllegalArgumentException("Performance name already exists in this festival: " + 
                                                 updates.getName());
            }
            performance.setName(updates.getName());
        }
        
        if (updates.getDescription() != null) {
            performance.setDescription(updates.getDescription());
        }
        
        if (updates.getGenre() != null) {
            performance.setGenre(updates.getGenre());
        }
        
        if (updates.getDuration() != null) {
            performance.setDuration(updates.getDuration());
        }
        
        if (updates.getTechnicalRequirements() != null) {
            performance.setTechnicalRequirements(updates.getTechnicalRequirements());
        }
        
        if (updates.getSetlist() != null) {
            performance.setSetlist(updates.getSetlist());
        }
        
        if (updates.getMerchandiseItems() != null) {
            performance.setMerchandiseItems(updates.getMerchandiseItems());
        }
        
        if (updates.getPreferredTimes() != null) {
            performance.setPreferredTimes(updates.getPreferredTimes());
        }
        
        return performanceRepository.save(performance);
    }
    
    /**
     * Add band member to performance
     * @param performanceId the performance ID
     * @param userId the user ID to add
     * @param adder the user adding the band member
     */
    public void addBandMember(Long performanceId, Long userId, User adder) {
        log.info("Adding band member {} to performance {} by user {}", 
                userId, performanceId, adder.getUsername());
        
        Performance performance = findById(performanceId);
        
        // Check if adder is main artist
        PerformanceArtist mainArtist = performanceArtistRepository
            .findByPerformancePerformanceIdAndIsMainArtistTrue(performanceId)
            .orElseThrow(() -> new IllegalStateException("No main artist found for performance"));
        
        if (!mainArtist.getUser().equals(adder)) {
            throw new UnauthorizedException(adder.getUsername(), "add band member", 
                                          "main artist", "Performance " + performanceId);
        }
        
        // Check if performance can be edited
        if (!performance.isEditable()) {
            throw new InvalidStateException("Performance", performance.getState().toString(), 
                                          "CREATED", "add band member");
        }
        
        User bandMember = userService.findById(userId);
        
        // Check if user is already an artist
        if (performanceArtistRepository.existsByPerformancePerformanceIdAndUserUserId(
                performanceId, userId)) {
            log.info("User {} is already an artist in performance {}", userId, performanceId);
            return;
        }
        
        // Add as band member
        PerformanceArtist artist = PerformanceArtist.createBandMember(performance, bandMember);
        performanceArtistRepository.save(artist);
        
        // Assign ARTIST role to band member in this festival
        userService.assignRole(bandMember, performance.getFestival(), Role.ARTIST);
    }
    
    /**
     * Submit performance for review
     * @param performanceId the performance ID
     * @param submission the submission data
     * @param submitter the user submitting
     * @return the submitted performance
     */
    public Performance submitPerformance(Long performanceId, SubmitPerformanceRequest submission, 
                                       User submitter) {
        log.info("Submitting performance {} by user {}", performanceId, submitter.getUsername());
        
        Performance performance = findById(performanceId);
        
        // Check if user is an artist of this performance
        if (!performanceArtistRepository.existsByPerformancePerformanceIdAndUserUserId(
                performanceId, submitter.getUserId())) {
            throw new UnauthorizedException(submitter.getUsername(), "submit performance", 
                                          "ARTIST of performance", "Performance " + performanceId);
        }
        
        // Check if performance is in correct state
        if (performance.getState() != PerformanceState.CREATED) {
            throw new InvalidStateException("Performance", performance.getState().toString(), 
                                          "CREATED", "submit");
        }
        
        // Check if festival is accepting submissions
        if (!performance.getFestival().isAcceptingSubmissions()) {
            throw new InvalidStateException("Festival", performance.getFestival().getState().toString(), 
                                          "SUBMISSION", "submit performance");
        }
        
        // Update submission fields
        performance.setTechnicalRequirements(submission.getTechnicalRequirements());
        performance.setSetlist(submission.getSetlist());
        performance.setMerchandiseItems(submission.getMerchandiseItems());
        performance.setPreferredTimes(submission.getPreferredTimes());
        
        // Validate all required fields are complete
        if (!performance.isReadyForSubmission()) {
            throw new IllegalStateException("Performance is not ready for submission - missing required fields");
        }
        
        // Submit performance
        performance.setState(PerformanceState.SUBMITTED);
        
        return performanceRepository.save(performance);
    }
    
    /**
     * Withdraw performance
     * @param performanceId the performance ID
     * @param withdrawer the user withdrawing
     */
    public void withdrawPerformance(Long performanceId, User withdrawer) {
        log.info("Withdrawing performance {} by user {}", performanceId, withdrawer.getUsername());
        
        Performance performance = findById(performanceId);
        
        // Check if user is an artist of this performance
        if (!performanceArtistRepository.existsByPerformancePerformanceIdAndUserUserId(
                performanceId, withdrawer.getUserId())) {
            throw new UnauthorizedException(withdrawer.getUsername(), "withdraw performance", 
                                          "ARTIST of performance", "Performance " + performanceId);
        }
        
        // Check if performance can be withdrawn
        if (!performance.canBeWithdrawn()) {
            throw new InvalidStateException("Performance", performance.getState().toString(), 
                                          "CREATED", "withdraw");
        }
        
        performanceRepository.delete(performance);
        log.info("Performance {} withdrawn", performanceId);
    }
    
    /**
     * Assign staff to performance
     * @param performanceId the performance ID
     * @param staffId the staff user ID
     * @param assigner the user assigning staff
     * @return the updated performance
     */
    public Performance assignStaff(Long performanceId, Long staffId, User assigner) {
        log.info("Assigning staff {} to performance {} by user {}", 
                staffId, performanceId, assigner.getUsername());
        
        Performance performance = findById(performanceId);
        Festival festival = performance.getFestival();
        
        // Check if user is organizer
        if (!userService.hasRoleInFestival(assigner.getUserId(), festival.getFestivalId(), 
                                          Role.ORGANIZER)) {
            throw new UnauthorizedException(assigner.getUsername(), "assign staff", 
                                          "ORGANIZER", "Festival " + festival.getFestivalId());
        }
        
        // Check if festival is in ASSIGNMENT state
        if (festival.getState() != FestivalState.ASSIGNMENT) {
            throw new InvalidStateException("Festival", festival.getState().toString(), 
                                          "ASSIGNMENT", "assign staff");
        }
        
        User staff = userService.findById(staffId);
        
        // Check if user is staff in this festival
        if (!userService.hasRoleInFestival(staffId, festival.getFestivalId(), Role.STAFF)) {
            throw new IllegalArgumentException("User is not a staff member of this festival");
        }
        
        performance.setAssignedStaff(staff);
        
        return performanceRepository.save(performance);
    }
    
    /**
     * Review performance
     * @param performanceId the performance ID
     * @param score the review score
     * @param comments the review comments
     * @param reviewer the user reviewing
     * @return the reviewed performance
     */
    public Performance reviewPerformance(Long performanceId, BigDecimal score, String comments, 
                                       User reviewer) {
        log.info("Reviewing performance {} by user {}", performanceId, reviewer.getUsername());
        
        Performance performance = findById(performanceId);
        Festival festival = performance.getFestival();
        
        // Check if reviewer is the assigned staff
        if (!performance.getAssignedStaff().equals(reviewer)) {
            throw new UnauthorizedException(reviewer.getUsername(), "review performance", 
                                          "assigned STAFF", "Performance " + performanceId);
        }
        
        // Check if festival is in REVIEW state
        if (!festival.isInReviewPhase()) {
            throw new InvalidStateException("Festival", festival.getState().toString(), 
                                          "REVIEW", "review performance");
        }
        
        // Check if performance is in correct state
        if (performance.getState() != PerformanceState.SUBMITTED) {
            throw new InvalidStateException("Performance", performance.getState().toString(), 
                                          "SUBMITTED", "review");
        }
        
        performance.setReviewScore(score);
        performance.setReviewComments(comments);
        performance.setState(PerformanceState.REVIEWED);
        
        return performanceRepository.save(performance);
    }
    
    /**
     * Approve performance
     * @param performanceId the performance ID
     * @param approver the user approving
     * @return the approved performance
     */
    public Performance approvePerformance(Long performanceId, User approver) {
        log.info("Approving performance {} by user {}", performanceId, approver.getUsername());
        
        Performance performance = findById(performanceId);
        Festival festival = performance.getFestival();
        
        // Check if user is organizer
        if (!userService.hasRoleInFestival(approver.getUserId(), festival.getFestivalId(), 
                                          Role.ORGANIZER)) {
            throw new UnauthorizedException(approver.getUsername(), "approve performance", 
                                          "ORGANIZER", "Festival " + festival.getFestivalId());
        }
        
        // Check if festival is in SCHEDULING state
        if (festival.getState() != FestivalState.SCHEDULING) {
            throw new InvalidStateException("Festival", festival.getState().toString(), 
                                          "SCHEDULING", "approve performance");
        }
        
        // Check if performance is in correct state
        if (performance.getState() != PerformanceState.REVIEWED) {
            throw new InvalidStateException("Performance", performance.getState().toString(), 
                                          "REVIEWED", "approve");
        }
        
        performance.setState(PerformanceState.APPROVED);
        
        return performanceRepository.save(performance);
    }
    
    /**
     * Reject performance
     * @param performanceId the performance ID
     * @param reason the rejection reason
     * @param rejecter the user rejecting
     * @return the rejected performance
     */
    public Performance rejectPerformance(Long performanceId, String reason, User rejecter) {
        log.info("Rejecting performance {} by user {}", performanceId, rejecter.getUsername());
        
        Performance performance = findById(performanceId);
        Festival festival = performance.getFestival();
        
        // Check if user is organizer
        if (!userService.hasRoleInFestival(rejecter.getUserId(), festival.getFestivalId(), 
                                          Role.ORGANIZER)) {
            throw new UnauthorizedException(rejecter.getUsername(), "reject performance", 
                                          "ORGANIZER", "Festival " + festival.getFestivalId());
        }
        
        // Check festival state and performance state
        boolean canReject = false;
        if (festival.getState() == FestivalState.SCHEDULING && 
            performance.getState() == PerformanceState.REVIEWED) {
            canReject = true;
        } else if (festival.getState() == FestivalState.DECISION && 
                   performance.getState() == PerformanceState.APPROVED) {
            canReject = true;
        }
        
        if (!canReject) {
            throw new InvalidStateException("Cannot reject performance in current state");
        }
        
        performance.setState(PerformanceState.REJECTED);
        performance.setRejectionReason(reason);
        
        return performanceRepository.save(performance);
    }
    
    /**
     * Submit final version of performance
     * @param performanceId the performance ID
     * @param finalData the final submission data
     * @param submitter the user submitting
     * @return the updated performance
     */
    public Performance submitFinalPerformance(Long performanceId, SubmitPerformanceRequest finalData, 
                                            User submitter) {
        log.info("Submitting final version of performance {} by user {}", 
                performanceId, submitter.getUsername());
        
        Performance performance = findById(performanceId);
        Festival festival = performance.getFestival();
        
        // Check if user is an artist of this performance
        if (!performanceArtistRepository.existsByPerformancePerformanceIdAndUserUserId(
                performanceId, submitter.getUserId())) {
            throw new UnauthorizedException(submitter.getUsername(), "submit final performance", 
                                          "ARTIST of performance", "Performance " + performanceId);
        }
        
        // Check if festival is in FINAL_SUBMISSION state
        if (festival.getState() != FestivalState.FINAL_SUBMISSION) {
            throw new InvalidStateException("Festival", festival.getState().toString(), 
                                          "FINAL_SUBMISSION", "submit final performance");
        }
        
        // Check if performance is approved
        if (performance.getState() != PerformanceState.APPROVED) {
            throw new InvalidStateException("Performance", performance.getState().toString(), 
                                          "APPROVED", "submit final version");
        }
        
        // Update final data
        performance.setTechnicalRequirements(finalData.getTechnicalRequirements());
        performance.setSetlist(finalData.getSetlist());
        performance.setMerchandiseItems(finalData.getMerchandiseItems());
        performance.setPreferredTimes(finalData.getPreferredTimes());
        
        // Mark as ready for final decision
        // State remains APPROVED but updated timestamp shows final submission
        
        return performanceRepository.save(performance);
    }
    
    /**
     * Accept performance (schedule it)
     * @param performanceId the performance ID
     * @param scheduledTime the scheduled time
     * @param acceptor the user accepting
     * @return the scheduled performance
     */
    public Performance acceptPerformance(Long performanceId, LocalDateTime scheduledTime, 
                                       User acceptor) {
        log.info("Accepting performance {} by user {}", performanceId, acceptor.getUsername());
        
        Performance performance = findById(performanceId);
        Festival festival = performance.getFestival();
        
        // Check if user is organizer
        if (!userService.hasRoleInFestival(acceptor.getUserId(), festival.getFestivalId(), 
                                          Role.ORGANIZER)) {
            throw new UnauthorizedException(acceptor.getUsername(), "accept performance", 
                                          "ORGANIZER", "Festival " + festival.getFestivalId());
        }
        
        // Check if festival is in DECISION state
        if (festival.getState() != FestivalState.DECISION) {
            throw new InvalidStateException("Festival", festival.getState().toString(), 
                                          "DECISION", "accept performance");
        }
        
        // Check if performance is approved
        if (performance.getState() != PerformanceState.APPROVED) {
            throw new InvalidStateException("Performance", performance.getState().toString(), 
                                          "APPROVED", "accept");
        }
        
        performance.setState(PerformanceState.SCHEDULED);
        performance.setScheduledTime(scheduledTime);
        
        return performanceRepository.save(performance);
    }
    
    /**
     * Find performance by ID
     * @param performanceId the performance ID
     * @return the performance
     */
    @Transactional(readOnly = true)
    public Performance findById(Long performanceId) {
        return performanceRepository.findById(performanceId)
            .orElseThrow(() -> new ResourceNotFoundException("Performance", "id", performanceId));
    }
    
    /**
     * Find performances by festival
     * @param festivalId the festival ID
     * @return list of performances
     */
    @Transactional(readOnly = true)
    public List<Performance> findByFestival(Long festivalId) {
        return performanceRepository.findByFestivalFestivalId(festivalId);
    }
    
    /**
     * Find performances by artist
     * @param userId the artist user ID
     * @return list of performances
     */
    @Transactional(readOnly = true)
    public List<Performance> findByArtist(Long userId) {
        return performanceRepository.findByArtist(userId);
    }
    
    /**
     * Search performances
     * @param festivalId the festival ID (optional)
     * @param name name search term
     * @param artists artists search term
     * @param genre genre search term
     * @param pageable pagination
     * @return page of performances
     */
    @Transactional(readOnly = true)
    public Page<Performance> searchPerformances(Long festivalId, String name, String artists,
                                              String genre, Pageable pageable) {
        return performanceRepository.searchPerformances(festivalId, name, artists, genre, pageable);
    }
    
    /**
     * Find scheduled performances for a festival
     * @param festivalId the festival ID
     * @return list of scheduled performances
     */
    @Transactional(readOnly = true)
    public List<Performance> findScheduledPerformances(Long festivalId) {
        return performanceRepository.findScheduledPerformances(festivalId);
    }
    
    /**
     * Auto-reject approved performances not finalized in DECISION state
     * @param festivalId the festival ID
     */
    public void autoRejectNotFinalized(Long festivalId) {
        log.info("Auto-rejecting not finalized performances for festival {}", festivalId);
        
        List<Performance> toReject = performanceRepository.findApprovedNotFinalized(festivalId);
        
        for (Performance performance : toReject) {
            performance.setState(PerformanceState.REJECTED);
            performance.setRejectionReason("Failed to submit final performance details");
            performanceRepository.save(performance);
        }
        
        log.info("Auto-rejected {} performances", toReject.size());
    }
}