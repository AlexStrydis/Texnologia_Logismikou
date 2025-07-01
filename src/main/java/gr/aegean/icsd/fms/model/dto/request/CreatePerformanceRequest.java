package gr.aegean.icsd.fms.model.dto.request;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import javax.validation.constraints.*;

/**
 * DTO for creating a new performance.
 * Contains initial required information for performance creation.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreatePerformanceRequest {
    
    @NotNull(message = "Festival ID is required")
    private Long festivalId;
    
    @NotBlank(message = "Performance name is required")
    @Size(min = 2, max = 100, message = "Performance name must be between 2 and 100 characters")
    private String name;
    
    @NotBlank(message = "Description is required")
    @Size(min = 10, message = "Description must be at least 10 characters")
    private String description;
    
    @NotBlank(message = "Genre is required")
    @Size(max = 50, message = "Genre must not exceed 50 characters")
    private String genre;
    
    @NotNull(message = "Duration is required")
    @Min(value = 15, message = "Performance must be at least 15 minutes")
    @Max(value = 300, message = "Performance cannot exceed 300 minutes")
    private Integer duration; // Duration in minutes
    
    // Optional fields for initial creation
    private String technicalRequirements;
    private String setlist; // JSON array of songs
    private String merchandiseItems; // JSON array
    private String preferredTimes; // JSON object
}