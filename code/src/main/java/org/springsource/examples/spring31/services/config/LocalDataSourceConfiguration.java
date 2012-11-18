package org.springsource.examples.spring31.services.config;


import org.apache.commons.dbcp.BasicDataSource;
import org.hibernate.dialect.H2Dialect;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;

import javax.inject.Inject;
import javax.sql.DataSource;
import java.sql.Driver;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * {@link Configuration class } that builds up local resources, consulting a properteis assumed to be
 * in the {@link org.springframework.core.env.PropertySource property sources chain}:
 * <p/>
 * <OL>
 * <LI>
 * <code>ds.driverClass</code> the driver class for the RDBMS database.
 * </LI>
 * <LI>
 * <code>ds.url</code> the JDBC connection URL for the RDBMS database
 * </LI>
 * <LI>
 * <code>ds.password</code> the password for the RDBMS database
 * </LI>
 * <LI>
 * <code>ds.user</code> the user name for the RDBMS database
 * </LI>
 * </OL>
 *
 * @author Josh Long
 */
@Configuration
@Profile("default")
public class LocalDataSourceConfiguration implements DataSourceConfiguration {

    private Environment environment;

    @Inject
    public LocalDataSourceConfiguration(Environment environment) {
        this.environment = environment;
    }

    @Bean
    public DataSource dataSource() throws Exception {

        String user = environment.getProperty("ds.user"),
                pw = environment.getProperty("ds.password"),
                url = environment.getProperty("ds.url");
        Class<Driver> driverClass = environment.getPropertyAsClass("ds.driverClass", Driver.class);
        BasicDataSource basicDataSource = new BasicDataSource();
        basicDataSource.setDriverClassName(driverClass.getName());
        basicDataSource.setPassword(pw);
        basicDataSource.setUrl(url);
        basicDataSource.setUsername(user);
        basicDataSource.setInitialSize(5);
        basicDataSource.setMaxActive(10);
        return basicDataSource;
    }

    @Bean
    public CacheManager cacheManager() throws Exception {
        SimpleCacheManager scm = new SimpleCacheManager();
        Cache cache = new ConcurrentMapCache("customers");
        scm.setCaches(Arrays.asList(cache));
        return scm;
    }

    public Map<String, String> contributeJpaEntityManagerProperties() {
        Map<String, String> p = new HashMap<String, String>();
        p.put(org.hibernate.cfg.Environment.HBM2DDL_AUTO, "create");
        p.put(org.hibernate.cfg.Environment.HBM2DDL_IMPORT_FILES, "import_h2.sql");
        p.put(org.hibernate.cfg.Environment.DIALECT, H2Dialect.class.getName());
        p.put(org.hibernate.cfg.Environment.SHOW_SQL, "true");
        return p;
    }
}
