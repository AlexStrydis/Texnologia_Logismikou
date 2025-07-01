package gr.aegean.icsd.fms.repository;

import gr.aegean.icsd.fms.model.entity.PerformanceArtist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for PerformanceArtist entity operations.
 * Manages the relationship between performances and their artists.
 */
@Repository
public interface PerformanceArtistRepository extends JpaRepository<PerformanceArtist, Long> {
    
    /**
     * Find all artists for a performance
     * @param performanceId the performance ID
     * @return list of performance artists
     */
    List<PerformanceArtist> findByPerformancePerformanceId(Long performanceId);
    
    /**
     * Find all performances for an artist
     * @param userId the user ID
     * @return list of performance artists
     */
    List<PerformanceArtist> findByUserUserId(Long userId);
    
    /**
     * Find the main artist of a performance
     * @param performanceId the performance ID
     * @return Optional containing the main artist relationship
     */
    Optional<PerformanceArtist> findByPerformancePerformanceIdAndIsMainArtistTrue(Long performanceId);
    
    /**
     * Check if a user is an artist in a performance
     * @param performanceId the performance ID
     * @param userId the user ID
     * @return true if user is an artist
     */
    boolean existsByPerformancePerformanceIdAndUserUserId(Long performanceId, Long userId);
    
    /**
     * Find performance-artist relationship
     * @param performanceId the performance ID
     * @param userId the user ID
     * @return Optional containing the relationship if found
     */
    Optional<PerformanceArtist> findByPerformancePerformanceIdAndUserUserId(Long performanceId, Long userId);
    
    /**
     * Count artists in a performance
     * @param performanceId the performance ID
     * @return number of artists
     */
    long countByPerformancePerformanceId(Long performanceId);
    
    /**
     * Find band members (non-main artists) of a performance
     * @param performanceId the performance ID
     * @return list of band members
     */
    @Query("SELECT pa FROM PerformanceArtist pa " +
           "WHERE pa.performance.performanceId = :performanceId " +
           "AND pa.isMainArtist = false " +
           "ORDER BY pa.addedAt")
    List<PerformanceArtist> findBandMembers(@Param("performanceId") Long performanceId);
    
    /**
     * Find performances where user is the main artist
     * @param userId the user ID
     * @return list of performances created by the user
     */
    @Query("SELECT pa FROM PerformanceArtist pa " +
           "WHERE pa.user.userId = :userId " +
           "AND pa.isMainArtist = true " +
           "ORDER BY pa.performance.createdAt DESC")
    List<PerformanceArtist> findPerformancesCreatedByUser(@Param("userId") Long userId);
    
    /**
     * Delete all artists from a performance
     * Used when deleting a performance
     * @param performanceId the performance ID
     */
    void deleteByPerformancePerformanceId(Long performanceId);
    
    /**
     * Find performances in a festival where user is an artist
     * @param userId the user ID
     * @param festivalId the festival ID
     * @return list of performance artist relationships
     */
    @Query("SELECT pa FROM PerformanceArtist pa " +
           "WHERE pa.user.userId = :userId " +
           "AND pa.performance.festival.festivalId = :festivalId " +
           "ORDER BY pa.performance.name")
    List<PerformanceArtist> findByUserAndFestival(@Param("userId") Long userId,
                                                   @Param("festivalId") Long festivalId);
    
    /**
     * Check if user is main artist of any performance in a festival
     * @param userId the user ID
     * @param festivalId the festival ID
     * @return true if user is a main artist
     */
    @Query("SELECT COUNT(pa) > 0 FROM PerformanceArtist pa " +
           "WHERE pa.user.userId = :userId " +
           "AND pa.performance.festival.festivalId = :festivalId " +
           "AND pa.isMainArtist = true")
    boolean isMainArtistInFestival(@Param("userId") Long userId,
                                   @Param("festivalId") Long festivalId);
}