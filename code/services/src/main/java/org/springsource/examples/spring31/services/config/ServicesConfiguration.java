package org.springsource.examples.spring31.services.config;

import org.hibernate.ejb.HibernatePersistence;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.*;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.orm.jpa.JpaDialect;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaDialect;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springsource.examples.spring31.services.Customer;
import org.springsource.examples.spring31.services.UserService;

import javax.inject.Inject;


@PropertySource("/config.properties")
@EnableCaching
@EnableTransactionManagement
@ImportResource({"classpath:/services/process-profile-photo-client.xml"})
@ComponentScan(basePackageClasses = {DataSourceConfiguration.class, UserService.class})
@Configuration
public class ServicesConfiguration {

    @Inject
    private DataSourceConfiguration dataSourceConfiguration;

    private String queueName = "photos";

    @Bean // nb it's static because it's a BeanFactoryPostProcessor implementation.
    public static PropertySourcesPlaceholderConfigurer propertyPlaceholderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }

    @Bean
    public JpaDialect jpaDialect() {
        return new HibernateJpaDialect();
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean localContainerEntityManagerFactoryBean() throws Exception {
        LocalContainerEntityManagerFactoryBean entityManagerFactoryBean = new LocalContainerEntityManagerFactoryBean();
        entityManagerFactoryBean.setDataSource(dataSourceConfiguration.dataSource());
        entityManagerFactoryBean.setPackagesToScan(Customer.class.getPackage().getName());
        entityManagerFactoryBean.setPersistenceProvider(new HibernatePersistence());
        entityManagerFactoryBean.setJpaDialect(jpaDialect());
        entityManagerFactoryBean.setJpaPropertyMap(dataSourceConfiguration.contributeJpaEntityManagerProperties());
        return entityManagerFactoryBean;
    }

    @Bean
    public PlatformTransactionManager transactionManager() throws Exception {
        return new JpaTransactionManager(localContainerEntityManagerFactoryBean().getObject());
    }

    @Bean
    public MongoTemplate mongoTemplate() throws Exception {
        return new MongoTemplate(dataSourceConfiguration.mongoDbFactory());
    }


    @Bean
    public GridFsTemplate gridFsTemplate() throws Exception {
        return new GridFsTemplate(dataSourceConfiguration.mongoDbFactory(), this.mongoTemplate().getConverter());
    }

    @Bean
    public RabbitTemplate rabbitTemplate() throws Throwable {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(dataSourceConfiguration.rabbitMqConnectionFactory());
        rabbitTemplate.setMessageConverter(rabbitTemplateMessageConverter());
        return rabbitTemplate;
    }

    @Bean
    public MessageConverter rabbitTemplateMessageConverter() {
        return new JsonMessageConverter();
    }

    @Bean
    public AmqpAdmin amqpAdmin() throws Throwable {
        return new RabbitAdmin(dataSourceConfiguration.rabbitMqConnectionFactory());
    }

    @Bean
    public Queue photosQueue() throws Throwable {
        Queue q = new Queue(this.queueName);
        amqpAdmin().declareQueue(q);
        return q;
    }

    @Bean
    public DirectExchange photosExchange() throws Throwable {
        DirectExchange directExchange = new DirectExchange(queueName);
        this.amqpAdmin().declareExchange(directExchange);
        return directExchange;
    }

    @Bean
    public Binding marketDataBinding() throws Throwable {
        return BindingBuilder.bind(photosQueue()).to(photosExchange()).with(this.queueName);
    }


}
