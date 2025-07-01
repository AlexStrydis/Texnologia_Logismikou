package gr.aegean.icsd.fms.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when an operation is attempted in an invalid state.
 * For example, trying to submit a performance when the festival is not accepting submissions.
 * This exception results in a 400 BAD REQUEST HTTP response.
 */
@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class InvalidStateException extends RuntimeException {
    
    private String entity;
    private String currentState;
    private String requiredState;
    private String operation;
    
    /**
     * Constructor with detailed state information
     * @param entity the entity type (e.g., "Festival", "Performance")
     * @param currentState the current state of the entity
     * @param requiredState the required state for the operation
     * @param operation the operation being attempted
     */
    public InvalidStateException(String entity, String currentState, String requiredState, String operation) {
        super(String.format("Cannot %s: %s is in %s state, but must be in %s state", 
                           operation, entity, currentState, requiredState));
        this.entity = entity;
        this.currentState = currentState;
        this.requiredState = requiredState;
        this.operation = operation;
    }
    
    /**
     * Constructor for invalid state transition
     * @param entity the entity type
     * @param currentState the current state
     * @param targetState the invalid target state
     */
    public InvalidStateException(String entity, String currentState, String targetState) {
        super(String.format("Invalid state transition for %s: cannot transition from %s to %s", 
                           entity, currentState, targetState));
        this.entity = entity;
        this.currentState = currentState;
        this.requiredState = targetState;
    }
    
    /**
     * Constructor with custom message
     * @param message the exception message
     */
    public InvalidStateException(String message) {
        super(message);
    }
    
    /**
     * Constructor with message and cause
     * @param message the exception message
     * @param cause the underlying cause
     */
    public InvalidStateException(String message, Throwable cause) {
        super(message, cause);
    }
    
    // Getters
    public String getEntity() {
        return entity;
    }
    
    public String getCurrentState() {
        return currentState;
    }
    
    public String getRequiredState() {
        return requiredState;
    }
    
    public String getOperation() {
        return operation;
    }
}