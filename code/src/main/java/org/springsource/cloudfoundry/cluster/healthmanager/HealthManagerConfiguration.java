package org.springsource.cloudfoundry.cluster.healthmanager;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springsource.cloudfoundry.cluster.BaseRabbitMqClusterConfiguration;
import org.springsource.cloudfoundry.cluster.HealthManager;

@Configuration
@ImportResource("/backoffice/health-manager-producer.xml")
public class HealthManagerConfiguration extends BaseRabbitMqClusterConfiguration {
    @Bean
    public HealthManager healthManager() {
        return new HealthManager();
    }

}
