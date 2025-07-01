package gr.aegean.icsd.fms.model.dto.request;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import javax.validation.constraints.*;

/**
 * DTO for submitting a performance for review.
 * All fields must be complete for submission.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubmitPerformanceRequest {
    
    @NotBlank(message = "Technical requirements are required for submission")
    @Size(min = 20, message = "Technical requirements must be at least 20 characters")
    private String technicalRequirements;
    
    @NotBlank(message = "Setlist is required for submission")
    private String setlist; // JSON array of songs
    
    @NotBlank(message = "Merchandise items are required for submission")
    private String merchandiseItems; // JSON array
    
    @NotBlank(message = "Preferred times are required for submission")
    private String preferredTimes; // JSON object with rehearsal and performance slots
    
    /**
     * Validate that JSON fields are properly formatted
     * Note: In a real implementation, we would parse and validate JSON structure
     */
    @AssertTrue(message = "Setlist must be a valid JSON array")
    public boolean isValidSetlist() {
        if (setlist == null || setlist.trim().isEmpty()) {
            return false;
        }
        // Basic check for JSON array format
        return setlist.trim().startsWith("[") && setlist.trim().endsWith("]");
    }
    
    @AssertTrue(message = "Merchandise items must be a valid JSON array")
    public boolean isValidMerchandiseItems() {
        if (merchandiseItems == null || merchandiseItems.trim().isEmpty()) {
            return false;
        }
        return merchandiseItems.trim().startsWith("[") && merchandiseItems.trim().endsWith("]");
    }
    
    @AssertTrue(message = "Preferred times must be a valid JSON object")
    public boolean isValidPreferredTimes() {
        if (preferredTimes == null || preferredTimes.trim().isEmpty()) {
            return false;
        }
        return preferredTimes.trim().startsWith("{") && preferredTimes.trim().endsWith("}");
    }
}