package gr.aegean.icsd.fms.service;

import gr.aegean.icsd.fms.exception.ResourceNotFoundException;
import gr.aegean.icsd.fms.model.entity.User;
import gr.aegean.icsd.fms.model.entity.UserRole;
import gr.aegean.icsd.fms.model.enums.UserRole as Role;
import gr.aegean.icsd.fms.repository.UserRepository;
import gr.aegean.icsd.fms.repository.UserRoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Service class for user management operations.
 */
@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class UserService {
    
    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final PasswordEncoder passwordEncoder;
    
    /**
     * Find user by ID
     * @param userId the user ID
     * @return the user
     * @throws ResourceNotFoundException if user not found
     */
    public User findById(Long userId) {
        return userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
    }
    
    /**
     * Find user by username
     * @param username the username
     * @return the user
     * @throws ResourceNotFoundException if user not found
     */
    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
            .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));
    }
    
    /**
     * Create a new user
     * @param username the username
     * @param password the plain text password
     * @param fullName the user's full name
     * @return the created user
     */
    public User createUser(String username, String password, String fullName) {
        log.info("Creating new user with username: {}", username);
        
        // Check if username already exists
        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Username already exists: " + username);
        }
        
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setFullName(fullName);
        
        return userRepository.save(user);
    }
    
    /**
     * Check if user has a specific role in a festival
     * @param userId the user ID
     * @param festivalId the festival ID
     * @param role the role to check
     * @return true if user has the role
     */
    @Transactional(readOnly = true)
    public boolean hasRoleInFestival(Long userId, Long festivalId, Role role) {
        return userRoleRepository.existsByUserAndFestivalAndRole(userId, festivalId, role);
    }
    
    /**
     * Assign a role to a user in a festival
     * @param user the user
     * @param festival the festival
     * @param role the role to assign
     * @return the created user role
     */
    public UserRole assignRole(User user, gr.aegean.icsd.fms.model.entity.Festival festival, Role role) {
        log.info("Assigning role {} to user {} in festival {}", 
                role, user.getUsername(), festival.getName());
        
        // Check if user already has a role in this festival
        Optional<UserRole> existingRole = userRoleRepository.findByUserAndFestival(
            user.getUserId(), festival.getFestivalId());
        
        if (existingRole.isPresent()) {
            UserRole existing = existingRole.get();
            if (existing.getRole() == role) {
                return existing; // Already has this role
            }
            
            // Check role compatibility
            if (!canChangeRole(existing.getRole(), role)) {
                throw new IllegalStateException(
                    String.format("Cannot change role from %s to %s", existing.getRole(), role));
            }
            
            // Update existing role
            existing.setRole(role);
            return userRoleRepository.save(existing);
        }
        
        // Create new role assignment
        UserRole userRole = UserRole.builder()
            .user(user)
            .festival(festival)
            .role(role)
            .build();
        
        return userRoleRepository.save(userRole);
    }
    
    /**
     * Check if role change is allowed
     * @param currentRole the current role
     * @param newRole the new role
     * @return true if change is allowed
     */
    private boolean canChangeRole(Role currentRole, Role newRole) {
        // ORGANIZER cannot change to other roles
        if (currentRole == Role.ORGANIZER) {
            return false;
        }
        
        // Cannot change between ARTIST and STAFF
        if ((currentRole == Role.ARTIST && newRole == Role.STAFF) ||
            (currentRole == Role.STAFF && newRole == Role.ARTIST)) {
            return false;
        }
        
        return true;
    }
    
    /**
     * Get all roles for a user
     * @param userId the user ID
     * @return list of user roles
     */
    @Transactional(readOnly = true)
    public List<UserRole> getUserRoles(Long userId) {
        return userRoleRepository.findByUserUserId(userId);
    }
    
    /**
     * Find users by role in festival
     * @param festivalId the festival ID
     * @param role the role
     * @return set of users
     */
    @Transactional(readOnly = true)
    public Set<User> findUsersByRoleInFestival(Long festivalId, Role role) {
        return userRepository.findByRoleInFestival(festivalId, role);
    }
    
    /**
     * Find staff members of a festival
     * @param festivalId the festival ID
     * @return list of staff user roles
     */
    @Transactional(readOnly = true)
    public List<UserRole> findStaffByFestival(Long festivalId) {
        return userRoleRepository.findStaffByFestival(festivalId);
    }
    
    /**
     * Find organizers of a festival
     * @param festivalId the festival ID
     * @return list of organizer user roles
     */
    @Transactional(readOnly = true)
    public List<UserRole> findOrganizersByFestival(Long festivalId) {
        return userRoleRepository.findOrganizersByFestival(festivalId);
    }
    
    /**
     * Check if username exists
     * @param username the username
     * @return true if exists
     */
    @Transactional(readOnly = true)
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }
    
    /**
     * Validate user credentials
     * @param username the username
     * @param password the plain text password
     * @return the user if credentials are valid
     * @throws IllegalArgumentException if credentials are invalid
     */
    public User validateCredentials(String username, String password) {
        User user = findByUsername(username);
        
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new IllegalArgumentException("Invalid credentials");
        }
        
        return user;
    }
}