package org.springsource.examples.spring31.web.interceptors;

import org.apache.log4j.Logger;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.ModelMap;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.context.request.WebRequestInterceptor;
import org.springsource.examples.spring31.services.UserService;

import java.util.HashMap;
import java.util.Map;

/**
 * We need a place to store common attributes if they're available.
 *
 * @author Josh Long
 */
public class CrmHttpServletRequestEnrichingInterceptor implements WebRequestInterceptor {

    private Logger logger = Logger.getLogger(getClass());

    private String userIdAttribute = "userId";

    private String fullUrlAttribute = "fullUrl";

    public void setUserIdAttribute(String u) {
        this.userIdAttribute = u;
    }

    public void setFullUrlAttribute(String u) {
        this.fullUrlAttribute = u;
    }

    @Override
    public void postHandle(WebRequest req, ModelMap model) throws Exception {
//        Map<String, Object> kvs = buildAttributesToContributeToRequest((ServletWebRequest) req);
//        model.addAllAttributes(kvs);
    }

    private Map<String, Object> buildAttributesToContributeToRequest(ServletWebRequest swr) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        Map<String, Object> kvs = new HashMap<String, Object>();
        kvs.put(fullUrlAttribute, buildFullUrlAttribute(swr));

        // not authenticated? principal is an AnonymousPrincipal
        if (null == authentication || !(authentication.getPrincipal() instanceof UserService.CrmUserDetails))
            return kvs;

        UserService.CrmUserDetails userDetails = (UserService.CrmUserDetails) authentication.getPrincipal();
        kvs.put(userIdAttribute, buildUserIdAttribute(userDetails));
        return kvs;
    }

    @Override
    public void preHandle(WebRequest req) throws Exception {
        logger.debug("web request is of type " + req.getClass().getSimpleName());
        ServletWebRequest swr = (ServletWebRequest) req;

        Map<String, Object> kvs = buildAttributesToContributeToRequest(swr);
        for (String k : kvs.keySet())
            swr.setAttribute(k, kvs.get(k), RequestAttributes.SCOPE_REQUEST);
    }

    @Override
    public void afterCompletion(WebRequest request, Exception ex) throws Exception {
    }

    private long buildUserIdAttribute(UserService.CrmUserDetails userDetails) {
        return userDetails.getUser().getId();
    }

    private String buildFullUrlAttribute(ServletWebRequest swr) {
        String servletPath = swr.getRequest().getServletPath();
        String allUrl = swr.getRequest().getRequestURL().toString().trim();
        return allUrl.substring(0, allUrl.length() - servletPath.length());
    }
}
