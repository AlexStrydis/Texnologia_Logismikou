package gr.aegean.icsd.fms.repository;

import gr.aegean.icsd.fms.model.entity.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for UserRole entity operations.
 * Manages the relationship between users and their roles in festivals.
 */
@Repository
public interface UserRoleRepository extends JpaRepository<UserRole, Long> {
    
    /**
     * Find user role by user and festival
     * @param userId the user ID
     * @param festivalId the festival ID
     * @return Optional containing the user role if found
     */
    @Query("SELECT ur FROM UserRole ur " +
           "WHERE ur.user.userId = :userId " +
           "AND ur.festival.festivalId = :festivalId")
    Optional<UserRole> findByUserAndFestival(@Param("userId") Long userId,
                                             @Param("festivalId") Long festivalId);
    
    /**
     * Find all roles for a user
     * @param userId the user ID
     * @return list of user roles
     */
    List<UserRole> findByUserUserId(Long userId);
    
    /**
     * Find all roles in a festival
     * @param festivalId the festival ID
     * @return list of user roles
     */
    List<UserRole> findByFestivalFestivalId(Long festivalId);
    
    /**
     * Find all users with a specific role in a festival
     * @param festivalId the festival ID
     * @param role the role
     * @return list of user roles
     */
    List<UserRole> findByFestivalFestivalIdAndRole(Long festivalId, 
                                                    gr.aegean.icsd.fms.model.enums.UserRoleType role);
    
    /**
     * Check if a user has a specific role in a festival
     * @param userId the user ID
     * @param festivalId the festival ID
     * @param role the role to check
     * @return true if user has the role
     */
    @Query("SELECT COUNT(ur) > 0 FROM UserRole ur " +
           "WHERE ur.user.userId = :userId " +
           "AND ur.festival.festivalId = :festivalId " +
           "AND ur.role = :role")
    boolean existsByUserAndFestivalAndRole(@Param("userId") Long userId,
                                           @Param("festivalId") Long festivalId,
                                           @Param("role") gr.aegean.icsd.fms.model.enums.UserRoleType role);
    
    /**
     * Check if a user has any role in a festival
     * @param userId the user ID
     * @param festivalId the festival ID
     * @return true if user has any role
     */
    boolean existsByUserUserIdAndFestivalFestivalId(Long userId, Long festivalId);
    
    /**
     * Count users with a specific role in a festival
     * @param festivalId the festival ID
     * @param role the role
     * @return count of users
     */
    @Query("SELECT COUNT(DISTINCT ur.user) FROM UserRole ur " +
           "WHERE ur.festival.festivalId = :festivalId " +
           "AND ur.role = :role")
    long countByFestivalAndRole(@Param("festivalId") Long festivalId,
                                @Param("role") gr.aegean.icsd.fms.model.enums.UserRoleType role);
    
    /**
     * Delete all roles for a user in a festival
     * Used when changing user roles
     * @param userId the user ID
     * @param festivalId the festival ID
     */
    void deleteByUserUserIdAndFestivalFestivalId(Long userId, Long festivalId);
    
    /**
     * Find organizers of a festival
     * @param festivalId the festival ID
     * @return list of organizer user roles
     */
    @Query("SELECT ur FROM UserRole ur " +
           "WHERE ur.festival.festivalId = :festivalId " +
           "AND ur.role = 'ORGANIZER' " +
           "ORDER BY ur.assignedAt")
    List<UserRole> findOrganizersByFestival(@Param("festivalId") Long festivalId);
    
    /**
     * Find staff members of a festival
     * @param festivalId the festival ID
     * @return list of staff user roles
     */
    @Query("SELECT ur FROM UserRole ur " +
           "WHERE ur.festival.festivalId = :festivalId " +
           "AND ur.role = 'STAFF' " +
           "ORDER BY ur.user.fullName")
    List<UserRole> findStaffByFestival(@Param("festivalId") Long festivalId);
    
    /**
     * Find festivals where user has conflicting roles
     * Used for validation
     * @param userId the user ID
     * @return list of festival IDs with multiple roles
     */
    @Query("SELECT ur.festival.festivalId FROM UserRole ur " +
           "WHERE ur.user.userId = :userId " +
           "GROUP BY ur.festival.festivalId " +
           "HAVING COUNT(DISTINCT ur.role) > 1")
    List<Long> findFestivalsWithMultipleRoles(@Param("userId") Long userId);
}