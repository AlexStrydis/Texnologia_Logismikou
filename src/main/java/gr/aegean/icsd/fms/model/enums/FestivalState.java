package gr.aegean.icsd.fms.model.enums;

/**
 * Enumeration representing the different states a festival can be in.
 * The festival progresses through these states sequentially.
 */
public enum FestivalState {
    /**
     * Initial state when festival is created
     */
    CREATED,
    
    /**
     * Festival is accepting performance submissions
     */
    SUBMISSION,
    
    /**
     * Staff members are being assigned to performances
     */
    ASSIGNMENT,
    
    /**
     * Staff members are reviewing performances
     */
    REVIEW,
    
    /**
     * Organizers are scheduling performances
     */
    SCHEDULING,
    
    /**
     * Artists submit final versions of approved performances
     */
    FINAL_SUBMISSION,
    
    /**
     * Final decisions are being made on performances
     */
    DECISION,
    
    /**
     * Festival lineup is finalized and publicly announced
     */
    ANNOUNCED;
    
    /**
     * Check if transition to the next state is valid
     * @param nextState the state to transition to
     * @return true if transition is valid, false otherwise
     */
    public boolean canTransitionTo(FestivalState nextState) {
        if (this == ANNOUNCED) {
            return false; // ANNOUNCED is final state
        }
        
        return switch (this) {
            case CREATED -> nextState == SUBMISSION;
            case SUBMISSION -> nextState == ASSIGNMENT;
            case ASSIGNMENT -> nextState == REVIEW;
            case REVIEW -> nextState == SCHEDULING;
            case SCHEDULING -> nextState == FINAL_SUBMISSION;
            case FINAL_SUBMISSION -> nextState == DECISION;
            case DECISION -> nextState == ANNOUNCED;
            case ANNOUNCED -> false;
        };
    }
    
    /**
     * Get the next state in the sequence
     * @return the next state, or null if this is the final state
     */
    public FestivalState getNextState() {
        return switch (this) {
            case CREATED -> SUBMISSION;
            case SUBMISSION -> ASSIGNMENT;
            case ASSIGNMENT -> REVIEW;
            case REVIEW -> SCHEDULING;
            case SCHEDULING -> FINAL_SUBMISSION;
            case FINAL_SUBMISSION -> DECISION;
            case DECISION -> ANNOUNCED;
            case ANNOUNCED -> null;
        };
    }
}