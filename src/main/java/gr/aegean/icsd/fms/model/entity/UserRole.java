package gr.aegean.icsd.fms.model.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * Entity representing the relationship between users and their roles in festivals.
 * This is a junction table that enforces the constraint that a user can have
 * only one role per festival.
 */
@Entity
@Table(name = "user_roles", 
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_user_festival_role", 
            columnNames = {"user_id", "festival_id"})
    },
    indexes = {
        @Index(name = "idx_user", columnList = "user_id"),
        @Index(name = "idx_festival", columnList = "festival_id"),
        @Index(name = "idx_role", columnList = "role")
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserRole {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_role_id")
    private Long userRoleId;
    
    @NotNull(message = "User is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @NotNull(message = "Festival is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "festival_id", nullable = false)
    private Festival festival;
    
    @NotNull(message = "Role is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private gr.aegean.icsd.fms.model.enums.UserRole role;
    
    @Column(name = "assigned_at", nullable = false, updatable = false)
    private LocalDateTime assignedAt;
    
    // Lifecycle callbacks
    
    @PrePersist
    protected void onCreate() {
        this.assignedAt = LocalDateTime.now();
    }
    
    // Business logic validation
    
    /**
     * Validate role constraints before persisting
     * @throws IllegalStateException if role constraints are violated
     */
    @PrePersist
    @PreUpdate
    public void validateRoleConstraints() {
        if (user == null || festival == null || role == null) {
            return; // Let @NotNull validation handle this
        }
        
        // Check if user already has a conflicting role in this festival
        // This would be better handled at service level with proper queries
        
        // ORGANIZER cannot have any other role in the same festival
        if (role == gr.aegean.icsd.fms.model.enums.UserRole.ORGANIZER) {
            // Ensure user doesn't have other roles in this festival
            boolean hasOtherRoles = user.getUserRoles().stream()
                .anyMatch(ur -> ur.getFestival().equals(this.festival) 
                    && !ur.equals(this) 
                    && ur.getRole() != gr.aegean.icsd.fms.model.enums.UserRole.ORGANIZER);
            
            if (hasOtherRoles) {
                throw new IllegalStateException(
                    "User cannot be ORGANIZER and have other roles in the same festival");
            }
        }
        
        // ARTIST and STAFF are mutually exclusive in the same festival
        if (role == gr.aegean.icsd.fms.model.enums.UserRole.ARTIST) {
            boolean isStaff = user.getUserRoles().stream()
                .anyMatch(ur -> ur.getFestival().equals(this.festival) 
                    && ur.getRole() == gr.aegean.icsd.fms.model.enums.UserRole.STAFF);
            
            if (isStaff) {
                throw new IllegalStateException(
                    "User cannot be both ARTIST and STAFF in the same festival");
            }
        }
        
        if (role == gr.aegean.icsd.fms.model.enums.UserRole.STAFF) {
            boolean isArtist = user.getUserRoles().stream()
                .anyMatch(ur -> ur.getFestival().equals(this.festival) 
                    && ur.getRole() == gr.aegean.icsd.fms.model.enums.UserRole.ARTIST);
            
            if (isArtist) {
                throw new IllegalStateException(
                    "User cannot be both STAFF and ARTIST in the same festival");
            }
        }
    }
    
    /**
     * Check if this role assignment is for a specific festival
     * @param festivalId the festival ID to check
     * @return true if this role is for the specified festival
     */
    public boolean isForFestival(Long festivalId) {
        return festival != null && festival.getFestivalId().equals(festivalId);
    }
    
    /**
     * Check if this role assignment is for a specific user
     * @param userId the user ID to check
     * @return true if this role is for the specified user
     */
    public boolean isForUser(Long userId) {
        return user != null && user.getUserId().equals(userId);
    }
}