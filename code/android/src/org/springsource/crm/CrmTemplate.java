package org.springsource.crm;

import org.springframework.social.oauth2.AbstractOAuth2ApiBinding;

public class CrmTemplate extends AbstractOAuth2ApiBinding implements Crm {
    private String apiUrlBase;
    private UserTemplate userTemplate;
    private CustomerTemplate customerTemplate;

    public CrmTemplate(String accessToken, String apiUrlBase) {
        super(accessToken);
        this.apiUrlBase = apiUrlBase;
        getRestTemplate().setErrorHandler(new CrmErrorHandler());
        this.userTemplate = new UserTemplate(getRestTemplate(), isAuthorized(), getApiUrlBase());
        this.customerTemplate = new CustomerTemplate(getRestTemplate(), isAuthorized(), getApiUrlBase());
    }

    private String getApiUrlBase() {
        return apiUrlBase;
    }

    @Override
    public UserTemplate userOperations() {
        return this.userTemplate;
    }

    @Override
    public CustomerTemplate customerOperations() {
        return this.customerTemplate;
    }
}
