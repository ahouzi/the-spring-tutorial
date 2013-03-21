package org.springsource.examples.spring31.web.config.servlet;

import org.springframework.security.web.session.HttpSessionEventPublisher;
import org.springframework.web.WebApplicationInitializer;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.filter.DelegatingFilterProxy;
import org.springframework.web.filter.HiddenHttpMethodFilter;
import org.springframework.web.multipart.support.MultipartFilter;
import org.springframework.web.servlet.DispatcherServlet;
import org.springsource.examples.spring31.web.config.WebMvcConfiguration;

import javax.servlet.*;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;
import java.util.Date;

/**
 * Simple replacement for <CODE>web.xml</CODE> that is constructed entirely in Java code.
 *
 * @author Josh Long
 */
@SuppressWarnings("unused")
public class CrmWebApplicationInitializer implements WebApplicationInitializer {

    private String patternAll = "/";

    private String springServletName = "spring";

    @Override
    public void onStartup(ServletContext servletContext) throws ServletException {

        registerFilter(servletContext, "springSecurityFilterChain", new DelegatingFilterProxy());
        registerFilter(servletContext, "hiddenHttpMethodFilter", new HiddenHttpMethodFilter());
        registerFilter(servletContext, "multipartFilter", new MultipartFilter());

        servletContext.addListener(new HttpSessionEventPublisher());
        servletContext.addListener(new ContextLoaderListener(buildWebApplicationContext(servletContext, WebMvcConfiguration.class)));


        DispatcherServlet dispatcherServlet = new DispatcherServlet();
        dispatcherServlet.setContextClass(AnnotationConfigWebApplicationContext.class);
        ServletRegistration.Dynamic spring = servletContext.addServlet(this.springServletName, dispatcherServlet);
        spring.addMapping(patternAll);
        spring.setAsyncSupported(true);

    }

    public class SessionAttributeDisplayingHttpSessionListener implements HttpSessionListener {

        // Notification that a new session was created
        public void sessionCreated(HttpSessionEvent event) {
            HttpSession session = event.getSession();

            System.out.println("New session created  : " + session.getId());
            System.out.println("Session creation time: " + new Date(session.getCreationTime()));
        }

        // Notification that a session was invalidated
        public void sessionDestroyed(HttpSessionEvent event) {
            HttpSession session = event.getSession();

            System.out.println("Session destroyed  : " + session.getId());
        }
    }

    protected void registerFilter(ServletContext servletContext, String name, Filter filter) {
        FilterRegistration.Dynamic filterRegistration = servletContext.addFilter(name, filter);
        filterRegistration.setAsyncSupported(true);
        filterRegistration.addMappingForUrlPatterns(null, true, this.patternAll);
        filterRegistration.addMappingForServletNames(null, true, this.springServletName);
    }

    protected WebApplicationContext buildWebApplicationContext(ServletContext servletContext, Class... configClasses) {
        AnnotationConfigWebApplicationContext ac = new AnnotationConfigWebApplicationContext();
        ac.setServletContext(servletContext);
        ac.register(configClasses);
        ac.refresh();
        return ac;
    }


}
