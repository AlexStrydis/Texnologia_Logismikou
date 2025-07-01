package gr.aegean.icsd.fms.model.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * Entity representing the relationship between performances and their artists.
 * This junction table tracks which users are artists (including band members)
 * for each performance, with one designated as the main artist (creator).
 */
@Entity
@Table(name = "performance_artists", 
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_performance_artist", 
            columnNames = {"performance_id", "user_id"})
    },
    indexes = {
        @Index(name = "idx_performance", columnList = "performance_id"),
        @Index(name = "idx_user", columnList = "user_id")
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PerformanceArtist {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "perf_artist_id")
    private Long perfArtistId;
    
    @NotNull(message = "Performance is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "performance_id", nullable = false)
    private Performance performance;
    
    @NotNull(message = "User is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(name = "is_main_artist", nullable = false)
    private boolean isMainArtist = false;
    
    @Column(name = "added_at", nullable = false, updatable = false)
    private LocalDateTime addedAt;
    
    // Lifecycle callbacks
    
    @PrePersist
    protected void onCreate() {
        this.addedAt = LocalDateTime.now();
        validateArtistRole();
    }
    
    @PreUpdate
    protected void onUpdate() {
        validateArtistRole();
    }
    
    // Business logic validation
    
    /**
     * Ensure that the user has ARTIST role in the festival
     * This should be enforced at service level when adding artists
     */
    private void validateArtistRole() {
        if (user == null || performance == null || performance.getFestival() == null) {
            return; // Let @NotNull validation handle this
        }
        
        // Note: This validation should ideally be done at service level
        // to avoid circular dependencies and ensure proper transaction handling
    }
    
    /**
     * Check if this artist can manage the performance
     * Main artist and band members can manage the performance
     * @return true if artist can manage
     */
    public boolean canManagePerformance() {
        return true; // All artists (main and band members) can manage
    }
    
    /**
     * Check if this is the creator of the performance
     * @return true if this is the main artist
     */
    public boolean isCreator() {
        return isMainArtist;
    }
    
    /**
     * Get the festival this performance belongs to
     * @return the festival
     */
    public Festival getFestival() {
        return performance != null ? performance.getFestival() : null;
    }
    
    /**
     * Static factory method to create main artist association
     * @param performance the performance
     * @param user the user who created the performance
     * @return new PerformanceArtist instance as main artist
     */
    public static PerformanceArtist createMainArtist(Performance performance, User user) {
        return PerformanceArtist.builder()
            .performance(performance)
            .user(user)
            .isMainArtist(true)
            .build();
    }
    
    /**
     * Static factory method to create band member association
     * @param performance the performance
     * @param user the user to add as band member
     * @return new PerformanceArtist instance as band member
     */
    public static PerformanceArtist createBandMember(Performance performance, User user) {
        return PerformanceArtist.builder()
            .performance(performance)
            .user(user)
            .isMainArtist(false)
            .build();
    }
}