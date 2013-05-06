package org.springsource.crm.utils;

import org.springframework.social.support.URIBuilder;
import org.springframework.util.*;

import java.net.URI;

public class PathUtils {
    private static final LinkedMultiValueMap<String, String> EMPTY_PARAMETERS = new LinkedMultiValueMap<String, String>();
    private String apiUrlBase;

    public PathUtils(String apiUrlBase) {
        this.apiUrlBase = apiUrlBase;
    }

    private String normalizeAddress(String path) {
        String baseUrl = this.apiUrlBase;
        String slash = "/";
        if (!baseUrl.endsWith(slash))
            baseUrl = baseUrl + slash;
        if (path.startsWith(slash))
            path = path.substring(1);
        return baseUrl + path;

    }

    public URI buildUri(String path) {
        return buildUri(path, EMPTY_PARAMETERS);
    }

    public URI buildUri(String path, String parameterName, String parameterValue) {
        MultiValueMap<String, String> parameters = new LinkedMultiValueMap<String, String>();
        parameters.set(parameterName, parameterValue);
        return buildUri(path, parameters);
    }

    public URI buildUri(String path, MultiValueMap<String, String> parameters) {
        String address = normalizeAddress(path);
        return URIBuilder.fromUri(address).queryParams(parameters).build();
    }
}
