package org.springsource.examples.spring31.web.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
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
@Configuration
public class SocialConfiguration {


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

    /**
     * when we sign in, the result should be a valid Spring Security context.
     */
    public static class SpringSecuritySignInAdapter implements SignInAdapter {

        private UserService userService;

        public SpringSecuritySignInAdapter(UserService userService) {
            this.userService = userService;
        }

        /**
         * local user id will invariably be the username from Facebook, e.g., 'starbuxman' or the email they signed up with.
         * <p/>
         * OK, so what else?
         */
        public String signIn(String localUserId, Connection<?> connection, NativeWebRequest request) {

            UserService.CrmUserDetails details = userService.loadUserByUsername(localUserId);
            if (null == details) throw new RuntimeException("could not login the user '" + localUserId + "'");
            SecurityContextHolder.getContext().setAuthentication(
                    new UsernamePasswordAuthenticationToken(details, details.getUser(), details.getAuthorities()));
            return null;
        }
    }

    /**
     * delegates to the {@link org.springsource.examples.spring31.services.UserService user service} object
     * to create a new user based on credentials coming back from Facebook
     */
    public static class CrmUserConnectionSignUp implements ConnectionSignUp {

        private UserService userService;

        public CrmUserConnectionSignUp(UserService userService) {
            this.userService = userService;
        }

        public String execute(Connection<?> connection) {
            UserProfile userProfile = connection.fetchUserProfile();
            long usrId = userService.createOrGet(userProfile.getUsername(), "password").getId();
            return userService.getUserById(usrId).getUsername();
        }
    }

    @Bean
    public ConnectionFactoryLocator connectionFactoryLocator() {
        ConnectionFactoryRegistry registry = new ConnectionFactoryRegistry();
        String clientId = environment.getProperty("facebook.clientId"),
                clientSecret = environment.getProperty("facebook.clientSecret");
        registry.addConnectionFactory(new FacebookConnectionFactory(clientId, clientSecret));
        return registry;
    }


    /**
     * Singleton data access object providing access to connections across all users.
     */
    @Bean
    public UsersConnectionRepository usersConnectionRepository() {
        JdbcUsersConnectionRepository repository = new JdbcUsersConnectionRepository(dataSource,
                connectionFactoryLocator(), Encryptors.noOpText());
        repository.setConnectionSignUp(new CrmUserConnectionSignUp(this.userService));

        return repository;
    }

    @Bean
    @Scope(value = "request", proxyMode = ScopedProxyMode.INTERFACES)
    public ConnectionRepository connectionRepository() {
        Authentication user = SecurityContextHolder.getContext().getAuthentication();
        Object principal = user.getPrincipal();
        assert principal instanceof UserService.CrmUserDetails : "the principal should be an instance of " + UserService.CrmUserDetails.class.getSimpleName();
        UserService.CrmUserDetails crmUserDetails = (UserService.CrmUserDetails) principal;
        return usersConnectionRepository().createConnectionRepository(crmUserDetails.getUser().getUsername());
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
        ProviderSignInController providerSignInController = new ProviderSignInController(connectionFactoryLocator(), usersConnectionRepository(),
                new SpringSecuritySignInAdapter(userService));
        providerSignInController.setSignInUrl("/crm/signin.html");
        return providerSignInController;
    }

}
