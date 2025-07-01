package gr.aegean.icsd.fms.repository;

import gr.aegean.icsd.fms.model.entity.Performance;
import gr.aegean.icsd.fms.model.enums.PerformanceState;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Performance entity operations.
 * Provides CRUD operations and custom queries for performance management.
 */
@Repository
public interface PerformanceRepository extends JpaRepository<Performance, Long> {
    
    /**
     * Find performances by festival
     * @param festivalId the festival ID
     * @return list of performances in the festival
     */
    List<Performance> findByFestivalFestivalId(Long festivalId);
    
    /**
     * Find performances by festival and state
     * @param festivalId the festival ID
     * @param state the performance state
     * @return list of performances
     */
    List<Performance> findByFestivalFestivalIdAndState(Long festivalId, PerformanceState state);
    
    /**
     * Check if a performance name exists in a festival
     * @param festivalId the festival ID
     * @param name the performance name
     * @return true if name exists in the festival
     */
    boolean existsByFestivalFestivalIdAndName(Long festivalId, String name);
    
    /**
     * Find performance by name within a festival
     * @param festivalId the festival ID
     * @param name the performance name
     * @return Optional containing the performance if found
     */
    Optional<Performance> findByFestivalFestivalIdAndName(Long festivalId, String name);
    
    /**
     * Find performances by assigned staff member
     * @param staffId the staff user ID
     * @return list of performances assigned to the staff member
     */
    List<Performance> findByAssignedStaffUserId(Long staffId);
    
    /**
     * Find performances by artist
     * @param userId the artist user ID
     * @return list of performances where user is an artist
     */
    @Query("SELECT DISTINCT pa.performance FROM PerformanceArtist pa " +
           "WHERE pa.user.userId = :userId " +
           "ORDER BY pa.performance.festival.startDate DESC, pa.performance.name")
    List<Performance> findByArtist(@Param("userId") Long userId);
    
    /**
     * Search performances by multiple criteria
     * @param festivalId the festival ID (optional)
     * @param name name search term (optional)
     * @param artists artists search term (optional)
     * @param genre genre search term (optional)
     * @param pageable pagination information
     * @return page of matching performances
     */
    @Query("SELECT DISTINCT p FROM Performance p " +
           "LEFT JOIN p.performanceArtists pa " +
           "LEFT JOIN pa.user u " +
           "WHERE (:festivalId IS NULL OR p.festival.festivalId = :festivalId) " +
           "AND (:name IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%'))) " +
           "AND (:artists IS NULL OR LOWER(u.fullName) LIKE LOWER(CONCAT('%', :artists, '%'))) " +
           "AND (:genre IS NULL OR LOWER(p.genre) LIKE LOWER(CONCAT('%', :genre, '%'))) " +
           "ORDER BY p.genre, p.name")
    Page<Performance> searchPerformances(@Param("festivalId") Long festivalId,
                                         @Param("name") String name,
                                         @Param("artists") String artists,
                                         @Param("genre") String genre,
                                         Pageable pageable);
    
    /**
     * Find scheduled performances for a festival (final lineup)
     * @param festivalId the festival ID
     * @return list of scheduled performances ordered by scheduled time
     */
    @Query("SELECT p FROM Performance p " +
           "WHERE p.festival.festivalId = :festivalId " +
           "AND p.state = 'SCHEDULED' " +
           "ORDER BY p.scheduledTime, p.name")
    List<Performance> findScheduledPerformances(@Param("festivalId") Long festivalId);
    
    /**
     * Find performances needing review in a festival
     * @param festivalId the festival ID
     * @return list of submitted performances without review
     */
    @Query("SELECT p FROM Performance p " +
           "WHERE p.festival.festivalId = :festivalId " +
           "AND p.state = 'SUBMITTED' " +
           "AND p.reviewScore IS NULL")
    List<Performance> findPerformancesNeedingReview(@Param("festivalId") Long festivalId);
    
    /**
     * Find performances by state in a festival
     * @param festivalId the festival ID
     * @param states list of states to filter by
     * @return list of performances
     */
    @Query("SELECT p FROM Performance p " +
           "WHERE p.festival.festivalId = :festivalId " +
           "AND p.state IN :states " +
           "ORDER BY p.name")
    List<Performance> findByFestivalAndStates(@Param("festivalId") Long festivalId,
                                              @Param("states") List<PerformanceState> states);
    
    /**
     * Get performance with all details loaded
     * @param performanceId the performance ID
     * @return Optional containing the performance with loaded relationships
     */
    @Query("SELECT p FROM Performance p " +
           "LEFT JOIN FETCH p.festival f " +
           "LEFT JOIN FETCH p.performanceArtists pa " +
           "LEFT JOIN FETCH pa.user " +
           "LEFT JOIN FETCH p.assignedStaff " +
           "WHERE p.performanceId = :performanceId")
    Optional<Performance> findByIdWithDetails(@Param("performanceId") Long performanceId);
    
    /**
     * Count performances by genre in a festival
     * @param festivalId the festival ID
     * @return list of genre and count pairs
     */
    @Query("SELECT p.genre, COUNT(p) FROM Performance p " +
           "WHERE p.festival.festivalId = :festivalId " +
           "GROUP BY p.genre " +
           "ORDER BY COUNT(p) DESC")
    List<Object[]> countByGenreInFestival(@Param("festivalId") Long festivalId);
    
    /**
     * Find performances that were approved but not submitted for final review
     * @param festivalId the festival ID
     * @return list of approved performances
     */
    @Query("SELECT p FROM Performance p " +
           "WHERE p.festival.festivalId = :festivalId " +
           "AND p.state = 'APPROVED' " +
           "AND p.festival.state = 'DECISION'")
    List<Performance> findApprovedNotFinalized(@Param("festivalId") Long festivalId);
}