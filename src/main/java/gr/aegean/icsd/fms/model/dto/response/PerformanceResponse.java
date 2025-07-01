package gr.aegean.icsd.fms.model.dto.response;

import gr.aegean.icsd.fms.model.enums.PerformanceState;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for performance response.
 * Different fields are included based on user role and performance state.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PerformanceResponse {
    
    // Basic information (visible to VISITORS for SCHEDULED performances only)
    private Long performanceId;
    private String name;
    private String genre;
    private LocalDateTime scheduledTime;
    private List<String> mainArtists; // Only names for VISITOR role
    
    // Additional information (visible to authenticated users)
    private Long festivalId;
    private String festivalName;
    private String description;
    private Integer duration;
    private PerformanceState state;
    
    // Detailed information (visible to ARTISTS of this performance)
    private String technicalRequirements;
    private String setlist;
    private String merchandiseItems;
    private String preferredTimes;
    private List<ArtistInfo> artists;
    private BigDecimal reviewScore;
    private String reviewComments;
    private String rejectionReason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Staff information (visible to assigned STAFF and ORGANIZERS)
    private StaffInfo assignedStaff;
    
    /**
     * Nested DTO for artist information
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ArtistInfo {
        private Long userId;
        private String username;
        private String fullName;
        private boolean isMainArtist;
    }
    
    /**
     * Nested DTO for staff information
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class StaffInfo {
        private Long userId;
        private String username;
        private String fullName;
    }
    
    /**
     * Create a basic response for visitors (only scheduled performances)
     */
    public static PerformanceResponse forVisitor(gr.aegean.icsd.fms.model.entity.Performance performance,
                                                List<String> mainArtists) {
        return PerformanceResponse.builder()
            .performanceId(performance.getPerformanceId())
            .name(performance.getName())
            .genre(performance.getGenre())
            .scheduledTime(performance.getScheduledTime())
            .mainArtists(mainArtists)
            .build();
    }
    
    /**
     * Create a response for artists
     */
    public static PerformanceResponse forArtist(gr.aegean.icsd.fms.model.entity.Performance performance,
                                               String festivalName,
                                               List<ArtistInfo> artists) {
        return PerformanceResponse.builder()
            .performanceId(performance.getPerformanceId())
            .name(performance.getName())
            .genre(performance.getGenre())
            .scheduledTime(performance.getScheduledTime())
            .festivalId(performance.getFestival().getFestivalId())
            .festivalName(festivalName)
            .description(performance.getDescription())
            .duration(performance.getDuration())
            .state(performance.getState())
            .technicalRequirements(performance.getTechnicalRequirements())
            .setlist(performance.getSetlist())
            .merchandiseItems(performance.getMerchandiseItems())
            .preferredTimes(performance.getPreferredTimes())
            .artists(artists)
            .reviewScore(performance.getReviewScore())
            .reviewComments(performance.getReviewComments())
            .rejectionReason(performance.getRejectionReason())
            .createdAt(performance.getCreatedAt())
            .updatedAt(performance.getUpdatedAt())
            .build();
    }
    
    /**
     * Create a full response for organizers and assigned staff
     */
    public static PerformanceResponse forOrganizerOrStaff(gr.aegean.icsd.fms.model.entity.Performance performance,
                                                          String festivalName,
                                                          List<ArtistInfo> artists,
                                                          StaffInfo staffInfo) {
        PerformanceResponse response = forArtist(performance, festivalName, artists);
        response.setAssignedStaff(staffInfo);
        return response;
    }
}