package org.springsource.examples.spring31.services.config;

import org.hibernate.ejb.HibernatePersistence;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springsource.examples.spring31.services.Customer;
import org.springsource.examples.spring31.services.CustomerService;

import javax.inject.Inject;


@Configuration
@PropertySource("/config.properties")
@EnableCaching
@EnableTransactionManagement
@ComponentScan(basePackageClasses = {CustomerService.class})
public class ServicesConfiguration {

    private DataSourceConfiguration dataSourceConfiguration;

    @Inject
    public ServicesConfiguration(DataSourceConfiguration dataSourceConfiguration) {
        this.dataSourceConfiguration = dataSourceConfiguration;
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean localContainerEntityManagerFactoryBean() throws Exception {
        LocalContainerEntityManagerFactoryBean entityManagerFactoryBean = new LocalContainerEntityManagerFactoryBean();
        entityManagerFactoryBean.setDataSource(dataSourceConfiguration.dataSource());
        entityManagerFactoryBean.setPackagesToScan(Customer.class.getPackage().getName());
        entityManagerFactoryBean.setPersistenceProvider(new HibernatePersistence());
        entityManagerFactoryBean.setJpaPropertyMap(dataSourceConfiguration.contributeJpaEntityManagerProperties());
        return entityManagerFactoryBean;
    }

    @Bean
    public PlatformTransactionManager transactionManager() throws Exception {
        return new JpaTransactionManager(localContainerEntityManagerFactoryBean().getObject());
    }
}
