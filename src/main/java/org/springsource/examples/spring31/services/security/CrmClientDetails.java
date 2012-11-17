package org.springsource.examples.spring31.services.security;

import org.springframework.security.oauth2.provider.BaseClientDetails;
import org.springframework.security.oauth2.provider.ClientDetails;


/**
 * Implementation of Spring Security OAuth's
 * {@link org.springframework.security.oauth2.provider.ClientDetails ClientDetails}
 * contract.
 *
 * @author Josh Long
 */
public class CrmClientDetails extends BaseClientDetails {

    private final  String defaultGrantTypes = "authorization_code,implicit";

    private CrmUserDetails userDetails;

    public CrmClientDetails(CrmUserDetails userDetails) {
        this.userDetails = userDetails;
    }

    public CrmClientDetails(ClientDetails prototype, CrmUserDetails userDetails) {
        super(prototype);
        this.userDetails = userDetails;
    }

    public CrmClientDetails(String clientId, String resourceIds, String scopes, String grantTypes, String authorities, CrmUserDetails userDetails) {
        super(clientId, resourceIds, scopes, grantTypes, authorities);
        this.userDetails = userDetails;
    }

    public CrmClientDetails(String clientId, String resourceIds, String scopes, String grantTypes, String authorities, String redirectUris, CrmUserDetails userDetails) {
        super(clientId, resourceIds, scopes, grantTypes, authorities, redirectUris);
        this.userDetails = userDetails;
    }
}
