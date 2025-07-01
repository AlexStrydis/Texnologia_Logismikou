package gr.aegean.icsd.fms.model.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for updating performance information. All fields are optional.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdatePerformanceRequest {
    private String name;
    private String description;
    private String genre;
    private Integer duration;
    private String technicalRequirements;
    private String setlist;
    private String merchandiseItems;
    private String preferredTimes;

    /**
     * Check if at least one field is provided.
     */
    public boolean hasUpdates() {
        return name != null || description != null || genre != null || duration != null
                || technicalRequirements != null || setlist != null
                || merchandiseItems != null || preferredTimes != null;
    }
}
