package org.springsource.examples.spring31.services.security;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import org.apache.commons.lang.StringUtils;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.common.exceptions.OAuth2Exception;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

/**
 * Implementation of Spring Security OAuth's {@link ClientDetailsService client details service}.
 * <p/>
 * This class tells Spring Security Oauth which resources this client
 * can access. There's not necessarily a one-to-one mapping between
 * a {@link ClientDetails client details instance } and a {@link UserDetails user details instance},
 * but it helps to simplify things: all users are also OAuth clients and have access to certain resources.
 *
 * @author Josh Long
 */
@Component
public class CrmClientDetailsService implements ClientDetailsService {

    private CrmUserDetailsService crmUserDetailsService;
    private String defaultOauth2GrantTypes = "authorization_code,implicit";
    private String defaultOauth2Scopes = "read,write";
    private String defaultOauth2Resource = "crm";

    @Inject
    public CrmClientDetailsService(CrmUserDetailsService us) {
        this.crmUserDetailsService = us;
    }

    @Override
    public ClientDetails loadClientByClientId(String clientId) throws OAuth2Exception {
        CrmUserDetails crmUserDetails = crmUserDetailsService.loadUserByUsername(clientId);
        return new CrmClientDetails(
                crmUserDetails.getUsername(),
                defaultOauth2Resource,
                defaultOauth2Scopes,
                defaultOauth2GrantTypes,
                authorities(crmUserDetails),
                null,
                crmUserDetails);
    }

    private String authorities(UserDetails userDetails) {
        return StringUtils.join(Collections2.transform(userDetails.getAuthorities(), new Function<GrantedAuthority, String>() {
            @Override
            public String apply(GrantedAuthority input) {
                return input.getAuthority();
            }
        }), ',');
    }

}
