package org.springsource.examples.spring31.services.config;

import org.cloudfoundry.runtime.env.RedisServiceInfo;
import org.cloudfoundry.runtime.service.keyvalue.RedisServiceCreator;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.cache.CacheManager;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.data.redis.connection.RedisConnectionFactory;

import javax.sql.DataSource;
import java.util.Map;

/**
 * This interface extracts the things that change from one environment to another - the
 * properties used to configure a Hibernate 4 {@link org.hibernate.SessionFactory session factory} and
 * a {@link DataSource datasource} - into a separate hierarchy so that implementations may be
 * <em>activated</em> based on which profile is active.
 *
 * @author Josh Long
 */
public interface DataSourceConfiguration {
    CacheManager cacheManager() throws Exception ;
    ConnectionFactory rabbitMqConnectionFactory() throws Exception;
    MongoDbFactory mongoDbFactory() throws Exception;
    DataSource dataSource() throws Exception;
    Map<String, String> contributeJpaEntityManagerProperties() throws Exception;
}