package org.springsource.examples.spring31.services.config;

import org.hibernate.ejb.HibernatePersistence;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.orm.jpa.JpaDialect;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaDialect;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springsource.examples.spring31.services.Customer;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;


@PropertySource("/config.properties")
@EnableCaching
@EnableTransactionManagement
@Import({LocalDataSourceConfiguration.class, CloudFoundryDataSourceConfiguration.class})
@Configuration
public class ServicesConfiguration {

    // @Inject
    //   private DataSourceConfiguration dataSourceConfiguration;

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
    public LocalContainerEntityManagerFactoryBean localContainerEntityManagerFactoryBean(DataSource dataSource, JpaDialect jpaDialect, DataSourceConfiguration dsConfiguration) throws Exception {
        LocalContainerEntityManagerFactoryBean entityManagerFactoryBean = new LocalContainerEntityManagerFactoryBean();
        entityManagerFactoryBean.setDataSource(dataSource);
        entityManagerFactoryBean.setPackagesToScan(Customer.class.getPackage().getName());
        entityManagerFactoryBean.setPersistenceProvider(new HibernatePersistence());
        entityManagerFactoryBean.setJpaDialect(jpaDialect);
        entityManagerFactoryBean.setJpaPropertyMap(dsConfiguration.contributeJpaEntityManagerProperties());
        return entityManagerFactoryBean;
    }

    @Bean
    public PlatformTransactionManager transactionManager(EntityManagerFactory entityManagerFactory) throws Exception {
        return new JpaTransactionManager(entityManagerFactory);
    }

    @Bean
    public MongoTemplate mongoTemplate(MongoDbFactory mongoDbFactory) throws Exception {
        return new MongoTemplate(mongoDbFactory);
    }


    @Bean
    public GridFsTemplate gridFsTemplate(MongoDbFactory mongoDbFactory, MongoTemplate mongoTemplate) throws Exception {
        return new GridFsTemplate(mongoDbFactory, mongoTemplate.getConverter());
    }

/*
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter rabbitTemplateMessageConverter) throws Throwable {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(rabbitTemplateMessageConverter);
        return rabbitTemplate;
    }

    @Bean
    public MessageConverter rabbitTemplateMessageConverter() {
        return new JsonMessageConverter();
    }

    @Bean
    public AmqpAdmin amqpAdmin(ConnectionFactory connectionFactory) throws Throwable {
        return new RabbitAdmin(connectionFactory);
    }

    @Bean
    public Queue photosQueue(AmqpAdmin amqpAdmin) throws Throwable {
        Queue q = new Queue(this.queueName);
        amqpAdmin.declareQueue(q);
        return q;
    }

    @Bean
    public DirectExchange photosExchange(AmqpAdmin amqpAdmin) throws Throwable {
        DirectExchange directExchange = new DirectExchange(queueName);
        amqpAdmin.declareExchange(directExchange);
        return directExchange;
    }

    @Bean
    public Binding marketDataBinding(@Qualifier("photosQueue") Queue photosQueue, @Qualifier("photosExchange") DirectExchange photosExchange) throws Throwable {
        return BindingBuilder.bind(photosQueue).to(photosExchange).with(this.queueName);
    }
*/


}
