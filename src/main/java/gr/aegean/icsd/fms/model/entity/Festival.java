package gr.aegean.icsd.fms.model.entity;

import gr.aegean.icsd.fms.model.enums.FestivalState;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.ToString;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Entity representing a music festival in the system.
 * Festivals progress through various states from creation to announcement.
 */
@Entity
@Table(name = "festivals", indexes = {
    @Index(name = "idx_name", columnList = "name", unique = true),
    @Index(name = "idx_state", columnList = "state"),
    @Index(name = "idx_dates", columnList = "start_date, end_date")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"performances", "userRoles"})
@ToString(exclude = {"performances", "userRoles"})
public class Festival {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "festival_id")
    private Long festivalId;
    
    @NotBlank(message = "Festival name is required")
    @Size(min = 3, max = 100, message = "Festival name must be between 3 and 100 characters")
    @Column(name = "name", unique = true, nullable = false, length = 100)
    private String name;
    
    @NotBlank(message = "Description is required")
    @Column(name = "description", columnDefinition = "TEXT", nullable = false)
    private String description;
    
    @NotNull(message = "Start date is required")
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;
    
    @NotNull(message = "End date is required")
    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;
    
    @NotBlank(message = "Venue is required")
    @Size(max = 200, message = "Venue must not exceed 200 characters")
    @Column(name = "venue", nullable = false, length = 200)
    private String venue;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "state", nullable = false)
    private FestivalState state = FestivalState.CREATED;
    
    @Column(name = "venue_layout", columnDefinition = "JSON")
    private String venueLayout; // JSON string containing venue layout information
    
    @Column(name = "budget_info", columnDefinition = "JSON")
    private String budgetInfo; // JSON string containing budget information
    
    @Column(name = "vendor_management", columnDefinition = "JSON")
    private String vendorManagement; // JSON string containing vendor information
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Relationships
    
    /**
     * Performances submitted to this festival
     */
    @OneToMany(mappedBy = "festival", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Performance> performances = new HashSet<>();
    
    /**
     * User roles associated with this festival (organizers, staff, artists)
     */
    @OneToMany(mappedBy = "festival", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<UserRole> userRoles = new HashSet<>();
    
    // Lifecycle callbacks
    
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.state == null) {
            this.state = FestivalState.CREATED;
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
    
    // Business logic methods
    
    /**
     * Check if the festival can transition to a new state
     * @param newState the desired new state
     * @return true if transition is valid
     */
    public boolean canTransitionTo(FestivalState newState) {
        return this.state.canTransitionTo(newState);
    }
    
    /**
     * Advance the festival to the next state
     * @throws IllegalStateException if the festival is already in final state
     */
    public void advanceState() {
        FestivalState nextState = this.state.getNextState();
        if (nextState == null) {
            throw new IllegalStateException("Festival is already in final state: " + this.state);
        }
        this.state = nextState;
    }
    
    /**
     * Check if the festival accepts performance submissions
     * @return true if in SUBMISSION state
     */
    public boolean isAcceptingSubmissions() {
        return this.state == FestivalState.SUBMISSION;
    }
    
    /**
     * Check if the festival is in a state where performances can be reviewed
     * @return true if in REVIEW state
     */
    public boolean isInReviewPhase() {
        return this.state == FestivalState.REVIEW;
    }
    
    /**
     * Check if the festival is finalized
     * @return true if in ANNOUNCED state
     */
    public boolean isFinalized() {
        return this.state == FestivalState.ANNOUNCED;
    }
    
    /**
     * Get all users with a specific role in this festival
     * @param role the role to filter by
     * @return set of users with the specified role
     */
    public Set<User> getUsersByRole(gr.aegean.icsd.fms.model.enums.UserRole role) {
        Set<User> users = new HashSet<>();
        for (UserRole userRole : userRoles) {
            if (userRole.getRole() == role) {
                users.add(userRole.getUser());
            }
        }
        return users;
    }
    
    /**
     * Validate that end date is after start date
     * @return true if dates are valid
     */
    @AssertTrue(message = "End date must be after start date")
    private boolean isValidDateRange() {
        if (startDate == null || endDate == null) {
            return true; // Let @NotNull handle null validation
        }
        return endDate.isAfter(startDate) || endDate.isEqual(startDate);
    }
}