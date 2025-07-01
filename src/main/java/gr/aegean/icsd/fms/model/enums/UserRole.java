package gr.aegean.icsd.fms.model.enums;

/**
 * Enumeration representing the different roles a user can have in the system.
 * Note: VISITOR is a pseudo-role for non-authenticated or users without specific festival roles.
 * All other roles are festival-specific.
 */
public enum UserRole {
    /**
     * Anonymous or authenticated user with read-only access
     */
    VISITOR,
    
    /**
     * User who has created or is a band member of a performance
     * This role is specific to a festival
     */
    ARTIST,
    
    /**
     * User assigned to review performances in a festival
     * This role is specific to a festival
     */
    STAFF,
    
    /**
     * User who created or manages a festival
     * This role is specific to a festival
     */
    ORGANIZER;
    
    /**
     * Check if this role requires authentication
     * @return true if authentication is required
     */
    public boolean requiresAuthentication() {
        return this != VISITOR;
    }
    
    /**
     * Check if this role can be combined with another role in the same festival
     * @param other the other role to check
     * @return true if roles can be combined
     */
    public boolean canCombineWith(UserRole other) {
        // ORGANIZER cannot have any other role in the same festival
        if (this == ORGANIZER || other == ORGANIZER) {
            return false;
        }
        
        // ARTIST and STAFF are mutually exclusive in the same festival
        if ((this == ARTIST && other == STAFF) || (this == STAFF && other == ARTIST)) {
            return false;
        }
        
        // VISITOR can be combined with anything
        return true;
    }
}