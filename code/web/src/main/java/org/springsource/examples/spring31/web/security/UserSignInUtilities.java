package org.springsource.examples.spring31.web.security;

import org.apache.commons.lang.StringUtils;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springsource.examples.spring31.services.UserService;

/**
 * Extract the recipe for 'signing' a user into the web application into a common place
 *
 * @author Josh Long
 */
public class UserSignInUtilities {

    private UserService userService = null;

    public UserSignInUtilities(UserService userService) {
        this.userService = userService;
    }

    public String signIn(String localUserId) {
        UserService.CrmUserDetails details = userService.loadUserByUsername(localUserId);
        assert details != null : "the " + UserService.CrmUserDetails.class.getSimpleName() + " can't be null";
        UsernamePasswordAuthenticationToken toAuthenticate = new UsernamePasswordAuthenticationToken( details, StringUtils.isEmpty(details.getPassword()) ? null : details.getPassword(), details.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(toAuthenticate);
        return null;

    }
}
