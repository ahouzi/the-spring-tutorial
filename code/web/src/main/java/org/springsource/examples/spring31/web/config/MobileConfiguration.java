package org.springsource.examples.spring31.web.config;

import org.apache.commons.logging.*;
import org.springframework.context.annotation.Configuration;
import org.springframework.mobile.device.*;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.*;

import java.util.List;

/**
 * Configuration class to enable Spring Mobile features.
 *
 * the proposed scheme is:
 *  - make it possible to setup a request attribute which can be used in all of the tiles templates
 *    ${pageRoot}
 *
 * @author Josh Long
 */
@Configuration
public class MobileConfiguration  extends WebMvcConfigurerAdapter {

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
        argumentResolvers.add(new DeviceHandlerMethodArgumentResolver());
        log.debug("adding " + DeviceHandlerMethodArgumentResolver.class.getName());
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new DeviceResolverHandlerInterceptor());
        log.debug("adding " + DeviceHandlerMethodArgumentResolver.class.getName());
    }

    private Log log = LogFactory.getLog(getClass());
}
