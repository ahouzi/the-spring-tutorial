package org.springsource.cloudfoundry.cluster.cloudcontroller;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springsource.cloudfoundry.cluster.BaseRabbitMqClusterConfiguration;
import org.springsource.cloudfoundry.cluster.CloudController;

@Configuration
@ImportResource("/backoffice/cloud-controller-consumer.xml")
public class CloudControllerConfiguration extends BaseRabbitMqClusterConfiguration {
    @Bean
    public CloudController cloudController() {
        return new CloudController();
    }


}
