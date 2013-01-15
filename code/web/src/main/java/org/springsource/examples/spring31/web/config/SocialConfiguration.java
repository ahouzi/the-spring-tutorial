package org.springsource.examples.spring31.web.config;

import org.apache.commons.lang.StringUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.social.config.annotation.EnableJdbcConnectionRepository;
import org.springframework.social.config.xml.SpringSecurityAuthenticationNameUserIdSource;
import org.springframework.social.config.xml.UserIdSource;
import org.springframework.social.connect.*;
import org.springframework.social.connect.web.ProviderSignInController;
import org.springframework.social.connect.web.SignInAdapter;
import org.springframework.social.facebook.config.annotation.EnableFacebook;
import org.springframework.web.context.request.NativeWebRequest;
import org.springsource.examples.spring31.services.User;
import org.springsource.examples.spring31.services.UserService;

/**
 * Configuration for Spring Social so that users may sign in
 * using a service provider like Facebook, or Twitter, or Weibo.
 *
 * @author Josh Long
 */
@Configuration
@EnableJdbcConnectionRepository
@EnableFacebook(appId = "${facebook.clientId}", appSecret = "${facebook.clientSecret}")
public class SocialConfiguration {

    @Bean
    public UserIdSource userIdSource() {
        return new SpringSecurityAuthenticationNameUserIdSource();
    }
    @Bean
    public ProviderSignInController providerSignInController(UsersConnectionRepository usersConnectionRepository, UserService userService, ConnectionFactoryLocator connectionFactoryLocator) {
        ProviderSignInController providerSignInController = new ProviderSignInController(connectionFactoryLocator, usersConnectionRepository,
                new SpringSecuritySignInAdapter(userService));
        providerSignInController.setSignInUrl("/crm/signin.html");
        providerSignInController.setPostSignInUrl("/crm/customers.html");
        providerSignInController.setSignUpUrl("/crm/signup.html");
        return providerSignInController;
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
         * <CODE>null</CODE> is OK, it signals to Spring Social's
         * {@link ProviderSignInController provider sign in controller}
         * to use the default URL.
         */
        public String signIn(String localUserId, Connection<?> connection, NativeWebRequest request) {
            UserService.CrmUserDetails details = userService.loadUserByUsername(localUserId);
            assert details != null : "the " + UserService.CrmUserDetails.class.getSimpleName() + " can't be null";
            UsernamePasswordAuthenticationToken toAuthenticate = new UsernamePasswordAuthenticationToken(
                    details, StringUtils.isEmpty(details.getPassword()) ? null : details.getPassword(), details.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(toAuthenticate);
            return null; // null is ok it signals to Spring Social's {@link ProviderSignInController} to use the default URL
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

        /**
         * if this account is created by connecting to facebook, then we'll assign a default password
         * and force the user to choose a password on the signup page.
         *
         * @param connection the Spring Social connection object with information about the object from the social provider.
         * @return the username
         */
        public String execute(Connection<?> connection) {
            UserProfile userProfile = connection.fetchUserProfile();
            User u = userService.loginByUsername(userProfile.getUsername());
            if (null == u) {
                u = userService.createUser(userProfile.getUsername(), "", userProfile.getFirstName(), userProfile.getLastName(), true);
            }
            return u.getUsername();
        }
    }
/*



    @Bean
    @Scope(proxyMode = ScopedProxyMode.NO )
    public UsersConnectionRepository usersConnectionRepository(DataSource dataSource, UserService userService, ConnectionFactoryLocator connectionFactoryLocator) {
        JdbcUsersConnectionRepository repository = new JdbcUsersConnectionRepository(dataSource, connectionFactoryLocator, Encryptors.noOpText());
        repository.setConnectionSignUp(new CrmUserConnectionSignUp(userService));
        return repository;
    }



   */


}
