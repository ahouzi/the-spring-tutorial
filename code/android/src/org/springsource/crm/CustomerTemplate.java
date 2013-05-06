package org.springsource.crm;

import org.springframework.web.client.RestTemplate;

public class CustomerTemplate extends AbstractCrmOperations {
    public CustomerTemplate(RestTemplate restTemplate, boolean isAuthorized, String apiUrlBase) {
        super(restTemplate, isAuthorized, apiUrlBase);
    }

}
