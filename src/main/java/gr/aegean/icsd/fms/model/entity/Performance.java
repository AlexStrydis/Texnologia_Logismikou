package gr.aegean.icsd.fms.model.entity;

import gr.aegean.icsd.fms.model.enums.PerformanceState;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.ToString;

import javax.persistence.*;
import javax.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Entity representing a musical performance in a festival.
 * Performances go through various states from creation to scheduling.
 */
@Entity
@Table(name = "performances", 
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_performance_name_per_festival", 
            columnNames = {"festival_id", "name"})
    },
    indexes = {
        @Index(name = "idx_festival", columnList = "festival_id"),
        @Index(name = "idx_state", columnList = "state"),
        @Index(name = "idx_genre", columnList = "genre"),
        @Index(name = "idx_performance_search", columnList = "name, genre")
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"festival", "performanceArtists", "assignedStaff"})
@ToString(exclude = {"festival", "performanceArtists", "assignedStaff"})
public class Performance {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "performance_id")
    private Long performanceId;
    
    @NotNull(message = "Festival is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "festival_id", nullable = false)
    private Festival festival;
    
    @NotBlank(message = "Performance name is required")
    @Size(min = 2, max = 100, message = "Performance name must be between 2 and 100 characters")
    @Column(name = "name", nullable = false, length = 100)
    private String name;
    
    @NotBlank(message = "Description is required")
    @Column(name = "description", columnDefinition = "TEXT", nullable = false)
    private String description;
    
    @NotBlank(message = "Genre is required")
    @Size(max = 50, message = "Genre must not exceed 50 characters")
    @Column(name = "genre", nullable = false, length = 50)
    private String genre;
    
    @NotNull(message = "Duration is required")
    @Min(value = 15, message = "Performance must be at least 15 minutes")
    @Max(value = 300, message = "Performance cannot exceed 300 minutes")
    @Column(name = "duration", nullable = false)
    private Integer duration; // Duration in minutes
    
    @Enumerated(EnumType.STRING)
    @Column(name = "state", nullable = false)
    private PerformanceState state = PerformanceState.CREATED;
    
    @Column(name = "technical_requirements", columnDefinition = "TEXT")
    private String technicalRequirements;
    
    @Column(name = "setlist", columnDefinition = "JSON")
    private String setlist; // JSON array of songs/pieces
    
    @Column(name = "merchandise_items", columnDefinition = "JSON")
    private String merchandiseItems; // JSON array of merchandise
    
    @Column(name = "preferred_times", columnDefinition = "JSON")
    private String preferredTimes; // JSON object with rehearsal and performance time preferences
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_staff_id")
    private User assignedStaff;
    
    @DecimalMin(value = "0.0", message = "Review score cannot be negative")
    @DecimalMax(value = "10.0", message = "Review score cannot exceed 10")
    @Column(name = "review_score", precision = 3, scale = 2)
    private BigDecimal reviewScore;
    
    @Column(name = "review_comments", columnDefinition = "TEXT")
    private String reviewComments;
    
    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    private String rejectionReason;
    
    @Column(name = "scheduled_time")
    private LocalDateTime scheduledTime;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Relationships
    
    /**
     * Artists associated with this performance
     */
    @OneToMany(mappedBy = "performance", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<PerformanceArtist> performanceArtists = new HashSet<>();
    
    // Lifecycle callbacks
    
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.state == null) {
            this.state = PerformanceState.CREATED;
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
    
    // Business logic methods
    
    /**
     * Check if all required fields for submission are filled
     * @return true if all required fields are non-empty
     */
    public boolean isReadyForSubmission() {
        return name != null && !name.trim().isEmpty() &&
               description != null && !description.trim().isEmpty() &&
               genre != null && !genre.trim().isEmpty() &&
               duration != null &&
               technicalRequirements != null && !technicalRequirements.trim().isEmpty() &&
               setlist != null && !setlist.trim().isEmpty() &&
               merchandiseItems != null && !merchandiseItems.trim().isEmpty() &&
               preferredTimes != null && !preferredTimes.trim().isEmpty() &&
               !performanceArtists.isEmpty(); // At least one artist
    }
    
    /**
     * Submit the performance for review
     * @throws IllegalStateException if not ready or festival not accepting submissions
     */
    public void submit() {
        if (this.state != PerformanceState.CREATED) {
            throw new IllegalStateException("Performance can only be submitted from CREATED state");
        }
        if (!festival.isAcceptingSubmissions()) {
            throw new IllegalStateException("Festival is not accepting submissions");
        }
        if (!isReadyForSubmission()) {
            throw new IllegalStateException("Performance is not ready for submission - missing required fields");
        }
        this.state = PerformanceState.SUBMITTED;
    }
    
    /**
     * Check if performance can be edited
     * @return true if in CREATED state
     */
    public boolean isEditable() {
        return this.state.isEditable();
    }
    
    /**
     * Check if performance can be withdrawn
     * @return true if withdrawal is allowed
     */
    public boolean canBeWithdrawn() {
        return this.state.canBeWithdrawn();
    }
    
    /**
     * Check if performance is in a final state
     * @return true if REJECTED or SCHEDULED
     */
    public boolean isInFinalState() {
        return this.state.isFinalState();
    }
    
    /**
     * Get the main artist (creator) of the performance
     * @return the main artist or null if not found
     */
    public User getMainArtist() {
        return performanceArtists.stream()
            .filter(PerformanceArtist::isMainArtist)
            .map(PerformanceArtist::getUser)
            .findFirst()
            .orElse(null);
    }
    
    /**
     * Get all artists (including band members)
     * @return set of all artists
     */
    public Set<User> getAllArtists() {
        Set<User> artists = new HashSet<>();
        for (PerformanceArtist pa : performanceArtists) {
            artists.add(pa.getUser());
        }
        return artists;
    }
    
    /**
     * Check if a user is an artist in this performance
     * @param user the user to check
     * @return true if user is an artist
     */
    public boolean hasArtist(User user) {
        return performanceArtists.stream()
            .anyMatch(pa -> pa.getUser().equals(user));
    }
}