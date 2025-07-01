package gr.aegean.icsd.fms.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when a user attempts an operation without proper authorization.
 * This exception results in a 403 FORBIDDEN HTTP response.
 */
@ResponseStatus(value = HttpStatus.FORBIDDEN)
public class UnauthorizedException extends RuntimeException {
    
    private String username;
    private String operation;
    private String requiredRole;
    private String resource;
    
    /**
     * Constructor with detailed authorization information
     * @param username the username attempting the operation
     * @param operation the operation being attempted
     * @param requiredRole the role required for the operation
     * @param resource the resource being accessed
     */
    public UnauthorizedException(String username, String operation, String requiredRole, String resource) {
        super(String.format("User '%s' is not authorized to %s. Required role: %s for resource: %s", 
                           username, operation, requiredRole, resource));
        this.username = username;
        this.operation = operation;
        this.requiredRole = requiredRole;
        this.resource = resource;
    }
    
    /**
     * Constructor for missing role
     * @param username the username
     * @param requiredRole the required role
     */
    public UnauthorizedException(String username, String requiredRole) {
        super(String.format("User '%s' does not have required role: %s", username, requiredRole));
        this.username = username;
        this.requiredRole = requiredRole;
    }
    
    /**
     * Constructor with custom message
     * @param message the exception message
     */
    public UnauthorizedException(String message) {
        super(message);
    }
    
    /**
     * Constructor with message and cause
     * @param message the exception message
     * @param cause the underlying cause
     */
    public UnauthorizedException(String message, Throwable cause) {
        super(message, cause);
    }
    
    // Getters
    public String getUsername() {
        return username;
    }
    
    public String getOperation() {
        return operation;
    }
    
    public String getRequiredRole() {
        return requiredRole;
    }
    
    public String getResource() {
        return resource;
    }
}