package gr.aegean.icsd.fms.model.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.ToString;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Entity representing a user in the system.
 * Users can have different roles in different festivals.
 */
@Entity
@Table(name = "users", indexes = {
    @Index(name = "idx_username", columnList = "username", unique = true)
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"userRoles", "performanceArtists", "assignedPerformances"})
@ToString(exclude = {"password", "userRoles", "performanceArtists", "assignedPerformances"})
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;
    
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    @Column(name = "username", unique = true, nullable = false, length = 50)
    private String username;
    
    @NotBlank(message = "Password is required")
    @Column(name = "password", nullable = false)
    private String password; // This will be BCrypt encrypted
    
    @NotBlank(message = "Full name is required")
    @Size(min = 2, max = 100, message = "Full name must be between 2 and 100 characters")
    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    // Relationships
    
    /**
     * Roles this user has in various festivals
     */
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<UserRole> userRoles = new HashSet<>();
    
    /**
     * Performances where this user is listed as an artist
     */
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<PerformanceArtist> performanceArtists = new HashSet<>();
    
    /**
     * Performances assigned to this user as staff for review
     */
    @OneToMany(mappedBy = "assignedStaff", fetch = FetchType.LAZY)
    private Set<Performance> assignedPerformances = new HashSet<>();
    
    // Lifecycle callbacks
    
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
    
    // Helper methods
    
    /**
     * Check if user has a specific role in a festival
     * @param role the role to check
     * @param festivalId the festival ID
     * @return true if user has the role in the festival
     */
    public boolean hasRoleInFestival(gr.aegean.icsd.fms.model.enums.UserRole role, Long festivalId) {
        return userRoles.stream()
            .anyMatch(ur -> ur.getFestival().getFestivalId().equals(festivalId) 
                && ur.getRole() == role);
    }
    
    /**
     * Check if user is authenticated (not a visitor)
     * @return true if user has any non-visitor role
     */
    public boolean isAuthenticated() {
        return userRoles.stream()
            .anyMatch(ur -> ur.getRole() != gr.aegean.icsd.fms.model.enums.UserRole.VISITOR);
    }
}