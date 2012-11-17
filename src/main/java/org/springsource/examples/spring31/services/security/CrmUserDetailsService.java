package org.springsource.examples.spring31.services.security;

import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springsource.examples.spring31.services.User;
import org.springsource.examples.spring31.services.UserService;

import javax.inject.Inject;

/**
 * Implementation of the Spring Security {@link UserDetailsService user details service contract}
 * delegating to the {@link UserService user service instance}.
 *
 * @author Josh Long
 */
@Component
public class CrmUserDetailsService implements UserDetailsService {

    private UserService userService;

    @Override
    public CrmUserDetails loadUserByUsername(String s) throws UsernameNotFoundException {
        User user = userService.loginByUsername(s);
        return new CrmUserDetails(  user );
    }

    @Inject
    public CrmUserDetailsService( UserService userService) {
        this.userService = userService;
    }

}
