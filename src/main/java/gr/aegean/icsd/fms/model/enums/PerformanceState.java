package gr.aegean.icsd.fms.model.enums;

/**
 * Enumeration representing the different states a performance can be in.
 */
public enum PerformanceState {
    /**
     * Initial state when performance is created
     */
    CREATED,
    
    /**
     * Performance has been submitted for review
     */
    SUBMITTED,
    
    /**
     * Performance has been reviewed by staff
     */
    REVIEWED,
    
    /**
     * Performance has been rejected (final state)
     */
    REJECTED,
    
    /**
     * Performance has been approved
     */
    APPROVED,
    
    /**
     * Performance has been scheduled in the festival (final state)
     */
    SCHEDULED;
    
    /**
     * Check if this is a final state
     * @return true if this is a final state
     */
    public boolean isFinalState() {
        return this == REJECTED || this == SCHEDULED;
    }
    
    /**
     * Check if performance can be edited in this state
     * @return true if editable
     */
    public boolean isEditable() {
        return this == CREATED;
    }
    
    /**
     * Check if performance can be withdrawn in this state
     * @return true if withdrawal is allowed
     */
    public boolean canBeWithdrawn() {
        return this == CREATED;
    }
}