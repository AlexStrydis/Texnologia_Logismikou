package gr.aegean.icsd.fms.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when a requested resource is not found.
 * This exception results in a 404 NOT FOUND HTTP response.
 */
@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class ResourceNotFoundException extends RuntimeException {
    
    private String resourceName;
    private String fieldName;
    private Object fieldValue;
    
    /**
     * Constructor with resource details
     * @param resourceName the name of the resource (e.g., "Festival", "Performance")
     * @param fieldName the field used for lookup (e.g., "id", "name")
     * @param fieldValue the value that was not found
     */
    public ResourceNotFoundException(String resourceName, String fieldName, Object fieldValue) {
        super(String.format("%s not found with %s : '%s'", resourceName, fieldName, fieldValue));
        this.resourceName = resourceName;
        this.fieldName = fieldName;
        this.fieldValue = fieldValue;
    }
    
    /**
     * Constructor with custom message
     * @param message the exception message
     */
    public ResourceNotFoundException(String message) {
        super(message);
    }
    
    /**
     * Constructor with message and cause
     * @param message the exception message
     * @param cause the underlying cause
     */
    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
    
    // Getters
    public String getResourceName() {
        return resourceName;
    }
    
    public String getFieldName() {
        return fieldName;
    }
    
    public Object getFieldValue() {
        return fieldValue;
    }
}