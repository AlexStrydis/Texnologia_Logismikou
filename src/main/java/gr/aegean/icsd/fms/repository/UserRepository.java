package gr.aegean.icsd.fms.repository;

import gr.aegean.icsd.fms.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;

/**
 * Repository interface for User entity operations.
 * Extends JpaRepository to provide basic CRUD operations
 * and defines custom query methods for user-specific operations.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    /**
     * Find a user by username
     * @param username the username to search for
     * @return Optional containing the user if found
     */
    Optional<User> findByUsername(String username);
    
    /**
     * Check if a username already exists
     * @param username the username to check
     * @return true if username exists
     */
    boolean existsByUsername(String username);
    
    /**
     * Find users by their role in a specific festival
     * @param festivalId the festival ID
     * @param role the role to filter by
     * @return set of users with the specified role in the festival
     */
    @Query("SELECT DISTINCT ur.user FROM UserRole ur " +
           "WHERE ur.festival.festivalId = :festivalId " +
           "AND ur.role = :role")
    Set<User> findByRoleInFestival(@Param("festivalId") Long festivalId, 
                                   @Param("role") gr.aegean.icsd.fms.model.enums.UserRoleType role);
    
    /**
     * Find all staff members of a festival
     * @param festivalId the festival ID
     * @return set of staff members
     */
    @Query("SELECT DISTINCT ur.user FROM UserRole ur " +
           "WHERE ur.festival.festivalId = :festivalId " +
           "AND ur.role = 'STAFF'")
    Set<User> findStaffByFestival(@Param("festivalId") Long festivalId);
    
    /**
     * Find all organizers of a festival
     * @param festivalId the festival ID
     * @return set of organizers
     */
    @Query("SELECT DISTINCT ur.user FROM UserRole ur " +
           "WHERE ur.festival.festivalId = :festivalId " +
           "AND ur.role = 'ORGANIZER'")
    Set<User> findOrganizersByFestival(@Param("festivalId") Long festivalId);
    
    /**
     * Find users whose full name contains the search term (case-insensitive)
     * @param searchTerm the term to search for
     * @return set of matching users
     */
    @Query("SELECT u FROM User u " +
           "WHERE LOWER(u.fullName) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Set<User> findByFullNameContaining(@Param("searchTerm") String searchTerm);
    
    /**
     * Check if a user has a specific role in a festival
     * @param userId the user ID
     * @param festivalId the festival ID
     * @param role the role to check
     * @return true if user has the role in the festival
     */
    @Query("SELECT COUNT(ur) > 0 FROM UserRole ur " +
           "WHERE ur.user.userId = :userId " +
           "AND ur.festival.festivalId = :festivalId " +
           "AND ur.role = :role")
    boolean hasRoleInFestival(@Param("userId") Long userId,
                              @Param("festivalId") Long festivalId,
                              @Param("role") gr.aegean.icsd.fms.model.enums.UserRoleType role);
    
    /**
     * Find artists of a specific performance
     * @param performanceId the performance ID
     * @return set of artists
     */
    @Query("SELECT DISTINCT pa.user FROM PerformanceArtist pa " +
           "WHERE pa.performance.performanceId = :performanceId")
    Set<User> findArtistsByPerformance(@Param("performanceId") Long performanceId);
    
    /**
     * Find the main artist (creator) of a performance
     * @param performanceId the performance ID
     * @return Optional containing the main artist if found
     */
    @Query("SELECT pa.user FROM PerformanceArtist pa " +
           "WHERE pa.performance.performanceId = :performanceId " +
           "AND pa.isMainArtist = true")
    Optional<User> findMainArtistByPerformance(@Param("performanceId") Long performanceId);
    
    /**
     * Find all festivals where user has any role
     * @param userId the user ID
     * @return set of festival IDs
     */
    @Query("SELECT DISTINCT ur.festival.festivalId FROM UserRole ur " +
           "WHERE ur.user.userId = :userId")
    Set<Long> findFestivalIdsByUser(@Param("userId") Long userId);
}