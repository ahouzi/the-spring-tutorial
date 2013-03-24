package org.springsource.examples.spring31.services.config;

import com.mongodb.Mongo;
import com.mongodb.WriteConcern;
import org.apache.commons.dbcp.BasicDataSource;
import org.hibernate.dialect.H2Dialect;
import org.hibernate.dialect.PostgreSQLDialect;
import org.hibernate.ejb.HibernatePersistence;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.env.Environment;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoDbFactory;
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
import java.sql.Driver;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;


@PropertySource("/config.properties")
@EnableCaching
@EnableTransactionManagement
@Configuration
public class ServicesConfiguration {

    private boolean resetDatabaseOnReset = false;
    private String queueName = "photos";


    @Bean(destroyMethod = "close")
    public DataSource dataSource(Environment environment) throws Exception {
        String user = environment.getProperty("dataSource.user"),
                pw = environment.getProperty("dataSource.password"),
                host = environment.getProperty("dataSource.host");
        int port = Integer.parseInt(environment.getProperty("dataSource.port"));
        String db = environment.getProperty("dataSource.db");
        Class<Driver> driverClass = environment.getPropertyAsClass("dataSource.driverClassName", Driver.class);
        BasicDataSource basicDataSource = new BasicDataSource();
        basicDataSource.setDriverClassName(driverClass.getName());
        basicDataSource.setPassword(pw);
        String url = String.format(environment.getProperty("dataSource.url" ), host, port, db);
        basicDataSource.setUrl(url);
        basicDataSource.setUsername(user);
        basicDataSource.setInitialSize(5);
        basicDataSource.setMaxActive(10);

        return basicDataSource;
    }

    @Bean
    public CacheManager cacheManager() throws Exception {
        SimpleCacheManager scm = new SimpleCacheManager();
        Collection<Cache> caches = new ArrayList<Cache>();
        for (String cacheName : "customers,users".split(",")) {
            Cache cache = new ConcurrentMapCache(cacheName);
            caches.add(cache);
        }
        scm.setCaches(caches);
        return scm;
    }

    public Map<String, String> contributeJpaEntityManagerProperties() {
        Map<String, String> p = new HashMap<String, String>();
        p.put(org.hibernate.cfg.Environment.HBM2DDL_AUTO, resetDatabaseOnReset ? "create" : "create-update");
        p.put(org.hibernate.cfg.Environment.DIALECT, H2Dialect.class.getName());
        p.put(org.hibernate.cfg.Environment.SHOW_SQL, "true");

        return p;
    }

    @Bean
    public MongoDbFactory mongoDbFactory(Environment environment) throws Exception {
        String dbName = environment.getProperty("mongo.fsbucket");
        String host = environment.getProperty("mongo.host");
        Mongo mongo = new Mongo(host);
        SimpleMongoDbFactory simpleMongoDbFactory = new SimpleMongoDbFactory(mongo, dbName);
        simpleMongoDbFactory.setWriteConcern(WriteConcern.FSYNC_SAFE);
        return simpleMongoDbFactory;
    }


    @Bean // nb it's static because it's a BeanFactoryPostProcessor implementation.
    public static PropertySourcesPlaceholderConfigurer propertyPlaceholderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }

    @Bean
    public JpaDialect jpaDialect() {
        return new HibernateJpaDialect();
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean localContainerEntityManagerFactoryBean(  DataSource dataSource, JpaDialect jpaDialect) throws Exception {
        LocalContainerEntityManagerFactoryBean entityManagerFactoryBean = new LocalContainerEntityManagerFactoryBean();
        entityManagerFactoryBean.setDataSource(dataSource);
        entityManagerFactoryBean.setPackagesToScan(Customer.class.getPackage().getName());
        entityManagerFactoryBean.setPersistenceProvider(new HibernatePersistence());
        entityManagerFactoryBean.setJpaDialect(jpaDialect);
        entityManagerFactoryBean.setJpaPropertyMap(contributeJpaEntityManagerProperties());
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


}
