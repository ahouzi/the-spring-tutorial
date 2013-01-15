package org.springsource.examples.spring31.web.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.social.config.annotation.EnableJdbcConnectionRepository;
import org.springframework.social.config.xml.SpringSecurityAuthenticationNameUserIdSource;
import org.springframework.social.config.xml.UserIdSource;
import org.springframework.social.connect.Connection;
import org.springframework.social.connect.ConnectionFactoryLocator;
import org.springframework.social.connect.UsersConnectionRepository;
import org.springframework.social.connect.web.ProviderSignInController;
import org.springframework.social.connect.web.SignInAdapter;
import org.springframework.social.facebook.config.annotation.EnableFacebook;
import org.springframework.web.context.request.NativeWebRequest;
import org.springsource.examples.spring31.web.security.UserSignInUtilities;

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
    public ProviderSignInController providerSignInController(
            final UsersConnectionRepository usersConnectionRepository,
            final UserSignInUtilities signInUtilities,
            final ConnectionFactoryLocator connectionFactoryLocator) {

        SignInAdapter signInAdapter = new SignInAdapter() {
            @Override
            public String signIn(String userId, Connection<?> connection, NativeWebRequest request) {
                return signInUtilities.signIn(userId);
            }
        };
        ProviderSignInController providerSignInController = new ProviderSignInController(
                connectionFactoryLocator, usersConnectionRepository, signInAdapter);
        providerSignInController.setSignInUrl("/crm/signin.html");
        providerSignInController.setPostSignInUrl("/crm/customers.html");
        providerSignInController.setSignUpUrl("/crm/signup.html");
        return providerSignInController;
    }
}
