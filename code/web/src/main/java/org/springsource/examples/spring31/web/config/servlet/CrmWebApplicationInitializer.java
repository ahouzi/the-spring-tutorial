package org.springsource.examples.spring31.web.config.servlet;

import org.springframework.web.WebApplicationInitializer;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;
import org.springsource.examples.spring31.web.config.WebMvcConfiguration;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;

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

        //// Listener Registration
        AnnotationConfigWebApplicationContext ac = new AnnotationConfigWebApplicationContext();
        ac.setServletContext(servletContext);
        ac.register(WebMvcConfiguration.class);
        ac.refresh();
        servletContext.addListener(new ContextLoaderListener(ac));

        //// DispatcherServlet Registration
        DispatcherServlet dispatcherServlet = new DispatcherServlet();
        dispatcherServlet.setContextClass(AnnotationConfigWebApplicationContext.class);
        ServletRegistration.Dynamic spring = servletContext.addServlet(this.springServletName, dispatcherServlet);
        spring.addMapping(patternAll);
        spring.setAsyncSupported(true);

    }


}
