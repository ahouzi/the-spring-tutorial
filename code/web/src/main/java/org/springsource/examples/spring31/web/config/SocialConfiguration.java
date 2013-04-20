package org.springsource.examples.spring31.web.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.social.UserIdSource;
import org.springframework.social.config.annotation.EnableJdbcConnectionRepository;
import org.springframework.social.connect.Connection;
import org.springframework.social.connect.ConnectionFactoryLocator;
import org.springframework.social.connect.UsersConnectionRepository;
import org.springframework.social.connect.web.ProviderSignInController;
import org.springframework.social.connect.web.SignInAdapter;
import org.springframework.social.facebook.config.annotation.EnableFacebook;
import org.springframework.web.context.request.NativeWebRequest;
import org.springsource.examples.spring31.web.security.UserSignInUtilities;

@Configuration
@EnableJdbcConnectionRepository
@EnableFacebook(appId = "${facebook.clientId}", appSecret = "${facebook.clientSecret}")
public class SocialConfiguration {

    @Bean
    public UserIdSource userIdSource() {
        return new UserIdSource() {
            @Override
            public String getUserId() {
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                return authentication.getName();
            }
        };
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
        ProviderSignInController psic = new ProviderSignInController( connectionFactoryLocator, usersConnectionRepository, signInAdapter);
        psic.setSignInUrl("/crm/signin.html");
        psic.setPostSignInUrl("/crm/customers.html");
        psic.setSignUpUrl("/crm/signup.html");
        return psic;
    }
}