package org.springsource.examples.spring31.web.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.encrypt.Encryptors;
import org.springframework.social.connect.*;
import org.springframework.social.connect.jdbc.JdbcUsersConnectionRepository;
import org.springframework.social.connect.support.ConnectionFactoryRegistry;
import org.springframework.social.connect.web.ProviderSignInController;
import org.springframework.social.connect.web.SignInAdapter;
import org.springframework.social.facebook.api.Facebook;
import org.springframework.social.facebook.connect.FacebookConnectionFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springsource.examples.spring31.services.UserService;

import javax.inject.Inject;
import javax.sql.DataSource;

/**
 * Configuration for Spring Social so that users may sign in
 * using a service provider like Facebook, or Twitter, or Weibo.
 *
 * @author Josh Long
 */
public class SocialConfiguration {


    class SpringSecuritySignInAdapter implements SignInAdapter {
        public String signIn(String localUserId, Connection<?> connection, NativeWebRequest request) {
            SecurityContextHolder.getContext().setAuthentication(
                    new UsernamePasswordAuthenticationToken(localUserId, null, null));
            return null;
        }
    }


    private Environment environment;

    private UserService userService;

    private DataSource dataSource;

    @Inject
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    @Inject
    public void setUserService(UserService u) {
        this.userService = u;
    }

    @Inject
    public void setDataSource(DataSource d) {
        this.dataSource = d;
    }

    //     * When a new provider is added to the app, register its {@link org.springframework.social.connect.ConnectionFactory} here.
//     * @see FacebookConnectionFactory
//
    @Bean
    public ConnectionFactoryLocator connectionFactoryLocator() {
        ConnectionFactoryRegistry registry = new ConnectionFactoryRegistry();
        registry.addConnectionFactory(
                new FacebookConnectionFactory(
                        environment.getProperty("facebook.clientId"),
                        environment.getProperty("facebook.clientSecret")));
        return registry;
    }

    /**
     * Singleton data access object providing access to connections across all users.
     */
    @Bean
    public UsersConnectionRepository usersConnectionRepository() {
        JdbcUsersConnectionRepository repository = new JdbcUsersConnectionRepository(dataSource,
                connectionFactoryLocator(), Encryptors.noOpText());
        repository.setConnectionSignUp(new AccountConnectionSignUp(this.userService));
        return repository;
    }


    /**
     * delegates to the {@link org.springsource.examples.spring31.services.UserService user service} object
     * to create a new user
     */
    static class AccountConnectionSignUp implements ConnectionSignUp {
        private UserService userService;

        public AccountConnectionSignUp(UserService userService) {
            this.userService = userService;
        }
/*
        private final AccountRepository accountRepository;

        public AccountConnectionSignUp(AccountRepository accountRepository) {
            this.accountRepository = accountRepository;
        }*/

        public String execute(Connection<?> connection) {
            UserProfile userProfile = connection.fetchUserProfile();

            org.springsource.examples.spring31.services.User usr =
                    userService.createOrGet(userProfile.getEmail(), "password");
            return usr.getEmail();

            //Long.toString( usr.getId() );
            /* UserProfile profile = connection.fetchUserProfile();
           Account account = new Account(profile.getUsername(), profile.getFirstName(), profile.getLastName());
           accountRepository.createAccount(account);
           return account.getUsername();*/
        }

    }


    // Request-scoped data access object providing access to the current user's connections.
// todo
//    @Bean
//    @Scope(value = "request", proxyMode = ScopedProxyMode.INTERFACES)
//    public ConnectionRepository connectionRepository() {
//        User user = SecurityContext.getCurrentUser();
//        return usersConnectionRepository().createConnectionRepository(user.getId());
//    }

    @Bean
    @Scope(value = "request", proxyMode = ScopedProxyMode.INTERFACES)
    public ConnectionRepository connectionRepository() {
        throw new RuntimeException("this isn't setup yet!!");
    }

    //
//     * A proxy to a request-scoped object representing the current user's primary Facebook account.
//     * @throws org.springframework.social.connect.NotConnectedException if the user is not connected to facebook.
//
    @Bean
    @Scope(value = "request", proxyMode = ScopedProxyMode.INTERFACES)
    public Facebook facebook() {
        return connectionRepository().getPrimaryConnection(Facebook.class).getApi();
    }

    //
//       The Spring MVC Controller that allows users to sign-in with their provider accounts.
//
    @Bean
    public ProviderSignInController providerSignInController() {
        return new ProviderSignInController(connectionFactoryLocator(), usersConnectionRepository(),
                new SpringSecuritySignInAdapter());
    }

}
