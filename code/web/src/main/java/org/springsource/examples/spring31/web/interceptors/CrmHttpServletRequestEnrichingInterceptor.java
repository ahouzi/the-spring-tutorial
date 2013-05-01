package org.springsource.examples.spring31.web.interceptors;

import org.springframework.mobile.device.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.ModelMap;
import org.springframework.web.context.request.*;
import org.springsource.examples.spring31.services.UserService;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * We need a place to store common attributes if they're available.
 *
 * @author Josh Long
 */
public class CrmHttpServletRequestEnrichingInterceptor implements WebRequestInterceptor {

    private String usernameAttribute = "username";
    private String userIdAttribute = "userId";
    private String clientPlatformAttribute = "clientPlatform";
    private String fullUrlAttribute = "fullUrl";

    public void setFullUrlAttribute(String u) {
        this.fullUrlAttribute = u;
    }

    @Override
    public void preHandle(WebRequest req) throws Exception {
        ServletWebRequest swr = (ServletWebRequest) req;
        Map<String, Object> kvs = buildAttributesToContributeToRequest(swr);
        for (String k : kvs.keySet())
            swr.setAttribute(k, kvs.get(k), RequestAttributes.SCOPE_REQUEST);
    }

    @Override
    public void postHandle(WebRequest request, ModelMap model) throws Exception {
    }

    @Override
    public void afterCompletion(WebRequest request, Exception ex) throws Exception {
    }

    private Map<String, Object> buildAttributesToContributeToRequest(ServletWebRequest swr) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();


        Map<String, Object> kvs = new HashMap<String, Object>();
        kvs.put(this.fullUrlAttribute, buildFullUrlAttribute(swr));

        // let's see if the 'username' attribute is available
        HttpServletRequest httpServletRequest = swr.getNativeRequest(HttpServletRequest.class);


        if (null != this.clientPlatformAttribute)
            kvs.put(this.clientPlatformAttribute, deriveCurrentClientPlatform(httpServletRequest))   ;

        String usernameValue = httpServletRequest.getParameter(this.usernameAttribute);

        if (null != this.usernameAttribute)
            kvs.put(this.usernameAttribute, usernameValue);

        // not authenticated? principal is an AnonymousPrincipal
        if (null == authentication || !(authentication.getPrincipal() instanceof UserService.CrmUserDetails))
            return kvs;

        UserService.CrmUserDetails userDetails = (UserService.CrmUserDetails) authentication.getPrincipal();
        kvs.put(userIdAttribute, buildUserIdAttribute(userDetails));

        return kvs;
    }

    private String deriveCurrentClientPlatform(HttpServletRequest httpServletRequest) {
        Device device = DeviceUtils.getCurrentDevice(httpServletRequest);
        String platform = "desktop";
        if (device.isMobile() || device.isTablet())
            platform = "mobile";
        return platform;
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
