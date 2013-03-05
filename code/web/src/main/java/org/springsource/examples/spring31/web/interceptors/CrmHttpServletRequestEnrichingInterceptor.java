package org.springsource.examples.spring31.web.interceptors;

import org.springframework.ui.ModelMap;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.context.request.WebRequestInterceptor;
import org.springsource.examples.spring31.services.User;
import org.springsource.examples.spring31.web.ViewController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

/**
 * We need a place to store common attributes if they're available.
 *
 * @author Josh Long
 */
public class CrmHttpServletRequestEnrichingInterceptor implements WebRequestInterceptor {

    private String usernameAttribute = "username";

    private String userIdAttribute = "userId";

    @Override
    public void postHandle(WebRequest req, ModelMap model) throws Exception {
    }

    @Override
    public void preHandle(WebRequest req) throws Exception {
        ServletWebRequest swr = (ServletWebRequest) req;
        Map<String, Object> stringObjectHashMap = new HashMap<String, Object>();
        HttpServletRequest httpServletRequest = swr.getNativeRequest(HttpServletRequest.class);
        HttpSession session = httpServletRequest.getSession(false);
        if (null != session) {
            User user = (User) session.getAttribute(ViewController.USER_OBJECT_KEY);
            if (null != user) {
                String usernameValue = httpServletRequest.getParameter(this.usernameAttribute);
                if (null != usernameAttribute)
                    stringObjectHashMap.put(this.usernameAttribute, usernameValue);
                stringObjectHashMap.put(userIdAttribute, user.getId());
            }
        }
        for (String k : stringObjectHashMap.keySet())
            swr.setAttribute(k, stringObjectHashMap.get(k), RequestAttributes.SCOPE_REQUEST);
    }

    @Override
    public void afterCompletion(WebRequest request, Exception ex) throws Exception {
    }

}
