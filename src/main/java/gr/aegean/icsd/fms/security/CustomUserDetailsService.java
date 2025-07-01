package gr.aegean.icsd.fms.security;

import gr.aegean.icsd.fms.model.entity.User;
import gr.aegean.icsd.fms.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Collections;

/**
 * Custom implementation of Spring Security's UserDetailsService.
 * Loads user-specific data for authentication.
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    
    private final UserRepository userRepository;
    
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
        
        return createUserDetails(user);
    }
    
    /**
     * Load user by ID
     * @param userId the user ID
     * @return UserDetails
     */
    @Transactional(readOnly = true)
    public UserDetails loadUserById(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + userId));
        
        return createUserDetails(user);
    }
    
    /**
     * Create UserDetails from User entity
     * @param user the user entity
     * @return UserDetails
     */
    private UserDetails createUserDetails(User user) {
        return new CustomUserPrincipal(
            user.getUserId(),
            user.getUsername(),
            user.getPassword(),
            user.getFullName(),
            getAuthorities()
        );
    }
    
    /**
     * Get authorities for user
     * Note: In this system, roles are festival-specific, so we return a basic ROLE_USER
     * @return collection of authorities
     */
    private Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
    }
    
    /**
     * Custom UserPrincipal implementation
     */
    public static class CustomUserPrincipal implements UserDetails {
        
        private final Long userId;
        private final String username;
        private final String password;
        private final String fullName;
        private final Collection<? extends GrantedAuthority> authorities;
        
        public CustomUserPrincipal(Long userId, String username, String password, 
                                  String fullName, Collection<? extends GrantedAuthority> authorities) {
            this.userId = userId;
            this.username = username;
            this.password = password;
            this.fullName = fullName;
            this.authorities = authorities;
        }
        
        public Long getUserId() {
            return userId;
        }
        
        public String getFullName() {
            return fullName;
        }
        
        @Override
        public Collection<? extends GrantedAuthority> getAuthorities() {
            return authorities;
        }
        
        @Override
        public String getPassword() {
            return password;
        }
        
        @Override
        public String getUsername() {
            return username;
        }
        
        @Override
        public boolean isAccountNonExpired() {
            return true;
        }
        
        @Override
        public boolean isAccountNonLocked() {
            return true;
        }
        
        @Override
        public boolean isCredentialsNonExpired() {
            return true;
        }
        
        @Override
        public boolean isEnabled() {
            return true;
        }
    }
}