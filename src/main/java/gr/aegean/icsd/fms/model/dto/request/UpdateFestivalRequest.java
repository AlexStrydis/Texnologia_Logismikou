package gr.aegean.icsd.fms.model.dto.request;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import javax.validation.constraints.*;
import java.time.LocalDate;

/**
 * DTO for updating festival information.
 * All fields are optional - only provided fields will be updated.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateFestivalRequest {
    
    @Size(min = 3, max = 100, message = "Festival name must be between 3 and 100 characters")
    private String name;
    
    @Size(min = 10, message = "Description must be at least 10 characters")
    private String description;
    
    @Future(message = "Start date must be in the future")
    private LocalDate startDate;
    
    @Future(message = "End date must be in the future")
    private LocalDate endDate;
    
    @Size(max = 200, message = "Venue must not exceed 200 characters")
    private String venue;
    
    // Optional fields
    private String venueLayout; // JSON string
    private String budgetInfo; // JSON string
    private String vendorManagement; // JSON string
    
    /**
     * Custom validation to ensure end date is after start date when both are provided
     */
    @AssertTrue(message = "End date must be after or equal to start date")
    public boolean isValidDateRange() {
        if (startDate == null || endDate == null) {
            return true; // Only validate when both dates are provided
        }
        return !endDate.isBefore(startDate);
    }
    
    /**
     * Check if any field is provided for update
     */
    public boolean hasUpdates() {
        return name != null || description != null || startDate != null || 
               endDate != null || venue != null || venueLayout != null || 
               budgetInfo != null || vendorManagement != null;
    }
}