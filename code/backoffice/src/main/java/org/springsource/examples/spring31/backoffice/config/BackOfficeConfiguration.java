package org.springsource.examples.spring31.backoffice.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportResource;
import org.springsource.examples.spring31.backoffice.PhotoTransformationService;
import org.springsource.examples.spring31.services.config.ServicesConfiguration;

/**
 * Handles processing of all incoming processing requests
 *
 * @author Josh Long
 */
@Configuration
@Import(ServicesConfiguration.class)
@ComponentScan(basePackageClasses = PhotoTransformationService.class)
@ImportResource({"/backoffice/process-profile-photos-service.xml"})
public class BackOfficeConfiguration {
}

