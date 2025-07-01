package gr.aegean.icsd.fms.repository;

import gr.aegean.icsd.fms.model.entity.Festival;
import gr.aegean.icsd.fms.model.enums.FestivalState;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Festival entity operations.
 * Provides CRUD operations and custom queries for festival management.
 */
@Repository
public interface FestivalRepository extends JpaRepository<Festival, Long> {
    
    /**
     * Find a festival by its name
     * @param name the festival name
     * @return Optional containing the festival if found
     */
    Optional<Festival> findByName(String name);
    
    /**
     * Check if a festival name already exists
     * @param name the festival name to check
     * @return true if name exists
     */
    boolean existsByName(String name);
    
    /**
     * Find festivals by state
     * @param state the festival state
     * @return list of festivals in the specified state
     */
    List<Festival> findByState(FestivalState state);
    
    /**
     * Find festivals by date range
     * @param startDate the start of the date range
     * @param endDate the end of the date range
     * @return list of festivals within the date range
     */
    @Query("SELECT f FROM Festival f " +
           "WHERE f.startDate >= :startDate AND f.endDate <= :endDate " +
           "ORDER BY f.startDate, f.name")
    List<Festival> findByDateRange(@Param("startDate") LocalDate startDate,
                                   @Param("endDate") LocalDate endDate);
    
    /**
     * Find upcoming festivals (not yet started)
     * @param currentDate the current date
     * @return list of upcoming festivals
     */
    @Query("SELECT f FROM Festival f " +
           "WHERE f.startDate > :currentDate " +
           "ORDER BY f.startDate, f.name")
    List<Festival> findUpcomingFestivals(@Param("currentDate") LocalDate currentDate);
    
    /**
     * Find ongoing festivals
     * @param currentDate the current date
     * @return list of ongoing festivals
     */
    @Query("SELECT f FROM Festival f " +
           "WHERE f.startDate <= :currentDate AND f.endDate >= :currentDate " +
           "ORDER BY f.startDate, f.name")
    List<Festival> findOngoingFestivals(@Param("currentDate") LocalDate currentDate);
    
    /**
     * Search festivals by multiple criteria
     * @param name name search term (optional)
     * @param description description search term (optional)
     * @param venue venue search term (optional)
     * @param startDate start date filter (optional)
     * @param endDate end date filter (optional)
     * @param pageable pagination information
     * @return page of matching festivals
     */
    @Query("SELECT DISTINCT f FROM Festival f " +
           "WHERE (:name IS NULL OR LOWER(f.name) LIKE LOWER(CONCAT('%', :name, '%'))) " +
           "AND (:description IS NULL OR LOWER(f.description) LIKE LOWER(CONCAT('%', :description, '%'))) " +
           "AND (:venue IS NULL OR LOWER(f.venue) LIKE LOWER(CONCAT('%', :venue, '%'))) " +
           "AND (:startDate IS NULL OR f.startDate >= :startDate) " +
           "AND (:endDate IS NULL OR f.endDate <= :endDate) " +
           "ORDER BY f.startDate, f.name")
    Page<Festival> searchFestivals(@Param("name") String name,
                                   @Param("description") String description,
                                   @Param("venue") String venue,
                                   @Param("startDate") LocalDate startDate,
                                   @Param("endDate") LocalDate endDate,
                                   Pageable pageable);
    
    /**
     * Find festivals where user has a specific role
     * @param userId the user ID
     * @param role the role
     * @return list of festivals
     */
    @Query("SELECT DISTINCT ur.festival FROM UserRole ur " +
           "WHERE ur.user.userId = :userId " +
           "AND ur.role = :role " +
           "ORDER BY ur.festival.startDate, ur.festival.name")
    List<Festival> findByUserRole(@Param("userId") Long userId,
                                  @Param("role") gr.aegean.icsd.fms.model.enums.UserRoleType role);
    
    /**
     * Find festivals where user is an organizer
     * @param userId the user ID
     * @return list of festivals
     */
    @Query("SELECT DISTINCT ur.festival FROM UserRole ur " +
           "WHERE ur.user.userId = :userId " +
           "AND ur.role = 'ORGANIZER' " +
           "ORDER BY ur.festival.startDate, ur.festival.name")
    List<Festival> findByOrganizer(@Param("userId") Long userId);
    
    /**
     * Count performances in a festival by state
     * @param festivalId the festival ID
     * @param state the performance state
     * @return count of performances
     */
    @Query("SELECT COUNT(p) FROM Performance p " +
           "WHERE p.festival.festivalId = :festivalId " +
           "AND p.state = :state")
    long countPerformancesByState(@Param("festivalId") Long festivalId,
                                  @Param("state") gr.aegean.icsd.fms.model.enums.PerformanceState state);
    
    /**
     * Get festival with all relationships loaded (for detail view)
     * @param festivalId the festival ID
     * @return Optional containing the festival with loaded relationships
     */
    @Query("SELECT f FROM Festival f " +
           "LEFT JOIN FETCH f.performances " +
           "LEFT JOIN FETCH f.userRoles " +
           "WHERE f.festivalId = :festivalId")
    Optional<Festival> findByIdWithDetails(@Param("festivalId") Long festivalId);
    
    /**
     * Find festivals that need state advancement
     * For example, festivals in SUBMISSION state past their submission deadline
     * @param currentDate the current date
     * @return list of festivals needing state update
     */
    @Query("SELECT f FROM Festival f " +
           "WHERE f.state = 'SUBMISSION' " +
           "AND f.startDate <= :currentDate")
    List<Festival> findFestivalsNeedingStateAdvancement(@Param("currentDate") LocalDate currentDate);
}