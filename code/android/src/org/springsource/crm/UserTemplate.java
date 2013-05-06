package org.springsource.crm;

import org.apache.commons.logging.*;
import org.codehaus.jackson.JsonNode;
import org.springframework.http.ResponseEntity;
import org.springframework.util.*;
import org.springframework.web.client.RestTemplate;

import java.util.*;

/**
 * client for calls made against the API.
 *
 * @author Josh Long
 */
public class UserTemplate extends AbstractCrmOperations implements UserOperations {
    private final static String API_SELF = "/api/self";
    private final static String API_USERS_COLLECTION = "/api/users";
    private final static String API_USERS_COLLECTION_USER = API_USERS_COLLECTION + "/{id}";
    private Log log = LogFactory.getLog(getClass());

    public UserTemplate(RestTemplate restTemplate, boolean isAuthorized, String apiUrlBase) {
        super(restTemplate, isAuthorized, apiUrlBase);
    }

    @Override
    public User getUserProfile() {
        return this.self();
    }

    @Override
    public User self() {
        return this.fromResponseToUser(API_SELF);
    }

    @Override
    public User byId(long userId) {
        Map<String, String> stringStringMap = Collections.singletonMap("id", Long.toString(userId));
        return this.fromResponseToUser(API_USERS_COLLECTION_USER, stringStringMap);
    }

    private User userFromJsonNode(JsonNode rootElementForUserJson) {
        return null;
    }

    private User fromResponseToUser(String url, Map<String, String>... parms) {
        MultiValueMap<String, String> aggregator = new LinkedMultiValueMap<String, String>();

        for (Map<String, String> m : parms)
            for (Map.Entry<String, String> e : m.entrySet())
                aggregator.set(e.getKey(), e.getValue());

        String restUrlForSelfApiEndpoint = this.getPathUtils().buildUri(url, aggregator).toString();
        ResponseEntity<JsonNode> jsonNode = this.getRestTemplate().getForEntity(restUrlForSelfApiEndpoint, JsonNode.class);
        return userFromJsonNode(jsonNode.getBody());
    }

}
