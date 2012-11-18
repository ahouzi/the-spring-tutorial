package org.springsource.examples.spring31.services.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springsource.examples.spring31.services.User;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Implementation of Spring Security's {@link org.springframework.security.core.userdetails.UserDetails UserDetails} contract
 *
 * @author Josh Long
 */
public class CrmUserDetails implements UserDetails {

    // mostly for use in the Oauth stuff
    public static final String ROLE_READ = "read";
    public static final String ROLE_WRITE = "write";

    /**
     * Roles for the user
     */
    private Set<String> roles = new HashSet<String>();
    private Set<GrantedAuthority> grantedAuthorities = new HashSet<GrantedAuthority>();

    /**
     * Regular CRM user
     */
    private User user;

    public CrmUserDetails(User user) {
        assert user != null : "the provided user reference can't be null";
        this.user = user;


        // setup roles
        roles.add(ROLE_READ);
        roles.add(ROLE_WRITE);

        // setup granted authorities
        for (String r : this.roles) {
            grantedAuthorities.add(new SimpleGrantedAuthority(r));
        }
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return this.grantedAuthorities;
    }

    @Override
    public String getPassword() {
        return this.user.getPassword();
    }

    @Override
    public String getUsername() {
        return this.user.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return isEnabled();
    }

    @Override
    public boolean isAccountNonLocked() {
        return isEnabled();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return isEnabled();
    }

    @Override
    public boolean isEnabled() {
        return user.isEnabled();
    }

    public User getUser() {
        return this.user;
    }
}