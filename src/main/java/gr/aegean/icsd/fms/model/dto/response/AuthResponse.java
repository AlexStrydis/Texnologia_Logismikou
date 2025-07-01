package gr.aegean.icsd.fms.model.dto.response;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.util.List;

/**
 * DTO for authentication response.
 * Contains JWT token and user information.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {
    
    private String token;
    private String type = "Bearer";
    private Long userId;
    private String username;
    private String fullName;
    private List<FestivalRole> festivalRoles;
    
    /**
     * Nested DTO for festival-specific roles
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class FestivalRole {
        private Long festivalId;
        private String festivalName;
        private String role;
    }
}