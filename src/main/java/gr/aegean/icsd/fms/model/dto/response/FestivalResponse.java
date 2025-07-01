package gr.aegean.icsd.fms.model.dto.response;

import gr.aegean.icsd.fms.model.enums.FestivalState;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for festival response.
 * Different fields are included based on user role.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FestivalResponse {
    
    // Basic information (visible to all)
    private Long festivalId;
    private String name;
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;
    private String venue;
    private List<String> organizerNames; // Only names for VISITOR role
    
    // Additional information (visible to authenticated users with roles)
    private FestivalState state;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Detailed information (visible to ORGANIZERS)
    private String venueLayout;
    private String budgetInfo;
    private String vendorManagement;
    private List<UserInfo> organizers;
    private List<UserInfo> staff;
    
    // Statistics (visible to ORGANIZERS)
    private Integer totalPerformances;
    private Integer scheduledPerformances;
    private Integer pendingReviews;
    
    /**
     * Nested DTO for user information
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UserInfo {
        private Long userId;
        private String username;
        private String fullName;
        private String role;
    }
    
    /**
     * Create a basic response for visitors
     */
    public static FestivalResponse forVisitor(gr.aegean.icsd.fms.model.entity.Festival festival, 
                                             List<String> organizerNames) {
        return FestivalResponse.builder()
            .festivalId(festival.getFestivalId())
            .name(festival.getName())
            .description(festival.getDescription())
            .startDate(festival.getStartDate())
            .endDate(festival.getEndDate())
            .venue(festival.getVenue())
            .organizerNames(organizerNames)
            .build();
    }
    
    /**
     * Create a detailed response for organizers
     */
    public static FestivalResponse forOrganizer(gr.aegean.icsd.fms.model.entity.Festival festival,
                                               List<UserInfo> organizers,
                                               List<UserInfo> staff,
                                               Integer totalPerformances,
                                               Integer scheduledPerformances,
                                               Integer pendingReviews) {
        return FestivalResponse.builder()
            .festivalId(festival.getFestivalId())
            .name(festival.getName())
            .description(festival.getDescription())
            .startDate(festival.getStartDate())
            .endDate(festival.getEndDate())
            .venue(festival.getVenue())
            .state(festival.getState())
            .createdAt(festival.getCreatedAt())
            .updatedAt(festival.getUpdatedAt())
            .venueLayout(festival.getVenueLayout())
            .budgetInfo(festival.getBudgetInfo())
            .vendorManagement(festival.getVendorManagement())
            .organizers(organizers)
            .staff(staff)
            .totalPerformances(totalPerformances)
            .scheduledPerformances(scheduledPerformances)
            .pendingReviews(pendingReviews)
            .build();
    }
}