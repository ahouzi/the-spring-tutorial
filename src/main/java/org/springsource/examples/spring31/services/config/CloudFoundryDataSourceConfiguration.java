package org.springsource.examples.spring31.services.config;

import org.cloudfoundry.runtime.env.AbstractServiceInfo;
import org.cloudfoundry.runtime.env.CloudEnvironment;
import org.cloudfoundry.runtime.env.RdbmsServiceInfo;
import org.cloudfoundry.runtime.env.RedisServiceInfo;
import org.cloudfoundry.runtime.service.AbstractServiceCreator;
import org.cloudfoundry.runtime.service.keyvalue.RedisServiceCreator;
import org.cloudfoundry.runtime.service.relational.RdbmsServiceCreator;
import org.hibernate.cfg.Environment;
import org.hibernate.dialect.PostgreSQLDialect;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Spring {@link Configuration configuration } class that implements {@link DataSourceConfiguration the data source configuration contract for our application}
 * so that we can inject it, and - through the magic of Spring profiles - defer to the right implementation for the right environment at runtime.
 * <p/>
 * This implementation works in any  <a href  ="http://www.cloudfoundry.com">Cloud Foundry</a> environment, and requires
 * that there be an RDBMS bound (I've tested the code against PostgreSQL, though MySQL should work just fine), and a Redis instance bound.
 * The names of the services bound are unimportant, in this case, so long as there is only one instance bound.
 *
 * @author Josh Long
 */
@Configuration
@Profile("cloud")
public class CloudFoundryDataSourceConfiguration implements DataSourceConfiguration {

    public static final String DEFAULT_REDIS_SERVICE_NAME = "cloudfoundryCrmRedis";
    public static final String DEFAULT_POSTGRESQL_SERVICE_NAME = "cloudfoundeyCrmPostgresql";


    // thread safe and long lived
    private CloudEnvironment cloudEnvironment = new CloudEnvironment();

    @Bean
    public DataSource dataSource() throws Exception {
        return lookupCloudFoundryService(DEFAULT_POSTGRESQL_SERVICE_NAME, RdbmsServiceInfo.class, RdbmsServiceCreator.class);
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate() throws Exception {
        RedisConnectionFactory connectionFactory = lookupCloudFoundryService(DEFAULT_REDIS_SERVICE_NAME, RedisServiceInfo.class, RedisServiceCreator.class);
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<String, Object>();
        redisTemplate.setConnectionFactory(connectionFactory);
        return redisTemplate;
    }

    @Bean
    public CacheManager cacheManager() throws Exception {
        return new RedisCacheManager(redisTemplate());
    }

    public Map<String, String> contributeJpaEntityManagerProperties() {
        Map<String, String> propertiesToAddToJpaEntityManager = new HashMap<String, String>();
        propertiesToAddToJpaEntityManager.put(Environment.HBM2DDL_AUTO, "create");
        propertiesToAddToJpaEntityManager.put(Environment.HBM2DDL_IMPORT_FILES, "import_psql.sql");
        propertiesToAddToJpaEntityManager.put(Environment.DIALECT, PostgreSQLDialect.class.getName());
        propertiesToAddToJpaEntityManager.put(Environment.SHOW_SQL, "true");
        return propertiesToAddToJpaEntityManager;
    }

    /**
     * Looks up a service bound to the Cloud Foundry environment through the use of the <code>vmc bind-service</code> or
     * <code>vmc push</code> command line tools.
     * <p/>
     * It looks up a service by a {@link String } name and {@link SI service info type}. If no name is specified, it looks up
     * all services bound by {@link SI service info type}. It returns the
     * first instance retrieved, assuming there are 0 or more instances bound.
     * <p/>
     * If both of these fail, it will fail.
     * <p/>
     * Finally, it creates an instance of the {@link DS service type required} and returns it.
     *
     * @param name             the name to lookup the service by (optional)
     * @param serviceInfoClass the {@link AbstractServiceInfo service info} class
     * @param dataSourceClass  the service requested
     * @param <SI>             the service info class
     * @param <SC>             the service creator class
     * @param <DS>             the class of the final, returned service requested
     * @return the service instance
     */
    private <SI extends AbstractServiceInfo, SC extends AbstractServiceCreator<DS, SI>, DS> DS lookupCloudFoundryService(String name, Class<SI> serviceInfoClass, Class<SC> dataSourceClass) {
        SI serviceInfo = null;
        if (StringUtils.hasText(name)) {
            SI abstractServiceInfo = cloudEnvironment.getServiceInfo(name, serviceInfoClass);
            if (null != abstractServiceInfo) {
                serviceInfo = abstractServiceInfo;
            } else {
                Collection<SI> tCollection = cloudEnvironment.getServiceInfos(serviceInfoClass);
                if (tCollection.size() > 0) {
                    serviceInfo = tCollection.iterator().next();
                }
            }
        }
        if (null == serviceInfo) {
            throw new RuntimeException("couldn'serviceInfo find a CloudFoundry service bound of type " +
                    serviceInfoClass.getName() + (StringUtils.hasText(name) ? " or of name '" + name + "'" : ""));
        }
        try {
            SC serviceCreator = dataSourceClass.newInstance();
            return serviceCreator.createService(serviceInfo);
        } catch (Exception e) {
            throw new RuntimeException("couldn'serviceInfo instantiate the instance of " + dataSourceClass.getName());
        }
    }

}

