package org.springsource.crm;

import org.springframework.social.MissingAuthorizationException;
import org.springframework.web.client.RestTemplate;
import org.springsource.crm.utils.PathUtils;

/**
 * base class for building clients that work with the CRM API.
 *
 * @author Josh Long
 */
public class AbstractCrmOperations {

    private final boolean isAuthorized;
    private final String apiUrlBase;
    private RestTemplate restTemplate;
    private PathUtils pathUtils;

    public AbstractCrmOperations(RestTemplate restTemplate, boolean isAuthorized, String apiUrlBase) {
        this.isAuthorized = isAuthorized;
        this.restTemplate = restTemplate;
        this.apiUrlBase = apiUrlBase;
        this.pathUtils = new PathUtils(this.apiUrlBase);
    }

    protected RestTemplate getRestTemplate() {
        return this.restTemplate;
    }

    protected void requireAuthorization() {
        if (!isAuthorized) {
            throw new MissingAuthorizationException();
        }
    }

    protected PathUtils getPathUtils() {
        return this.pathUtils;
    }

    protected String getApiUrlBase() {
        return apiUrlBase;
    }

}
