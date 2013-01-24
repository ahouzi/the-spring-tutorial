package org.springsource.examples.spring31.web.config;

import org.springframework.context.ApplicationContextInitializer;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;

/**
 * Simple class that sets up the web application environment. This configures the {@link AnnotationConfigWebApplicationContext application context}
 * based on which environment the application is running, enabling Spring {@link org.springframework.core.env.Environment environment} profiles
 * as appropriate. The two supported environments are:
 * <p/>
 * <OL>
 * <LI><CODE>cloud</CODE> - used when run in a cloud provider like <a href="http://www.cloudfoundry.com">CloudFoundry, the open-source platform-as-a-service (PaaS)</a>.</LI>
 * <LI><CODE>local</CODE> - used when run inside a local environment.  If no default profile names are explicitly set and no active profile names are explicitly set, this profile will be activated by default.</li>
 * </OL>
 *
 * @author Josh Long
 */
public class AnnotationConfigWebApplicationContextInitializer implements ApplicationContextInitializer<AnnotationConfigWebApplicationContext> {

    // todo restore this
    //private CloudEnvironment cloudEnvironment = new CloudEnvironment();

    @Override
    public void initialize(AnnotationConfigWebApplicationContext applicationContext) {
        applicationContext.register( WebMvcConfiguration.class );
        applicationContext.refresh();
    }
}