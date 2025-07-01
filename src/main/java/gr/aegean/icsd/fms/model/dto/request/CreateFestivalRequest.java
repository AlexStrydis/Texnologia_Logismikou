package gr.aegean.icsd.fms.model.dto.request;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import javax.validation.constraints.*;
import java.time.LocalDate;

/**
 * DTO for creating a new festival.
 * Contains all required information for festival creation.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateFestivalRequest {
    
    @NotBlank(message = "Festival name is required")
    @Size(min = 3, max = 100, message = "Festival name must be between 3 and 100 characters")
    private String name;
    
    @NotBlank(message = "Description is required")
    @Size(min = 10, message = "Description must be at least 10 characters")
    private String description;
    
    @NotNull(message = "Start date is required")
    @Future(message = "Start date must be in the future")
    private LocalDate startDate;
    
    @NotNull(message = "End date is required")
    @Future(message = "End date must be in the future")
    private LocalDate endDate;
    
    @NotBlank(message = "Venue is required")
    @Size(max = 200, message = "Venue must not exceed 200 characters")
    private String venue;
    
    // Optional fields
    private String venueLayout; // JSON string
    private String budgetInfo; // JSON string
    private String vendorManagement; // JSON string
    
    /**
     * Custom validation to ensure end date is after start date
     */
    @AssertTrue(message = "End date must be after or equal to start date")
    public boolean isValidDateRange() {
        if (startDate == null || endDate == null) {
            return true; // Let @NotNull handle null validation
        }
        return !endDate.isBefore(startDate);
    }
}