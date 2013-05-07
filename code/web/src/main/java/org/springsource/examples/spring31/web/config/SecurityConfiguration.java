package org.springsource.examples.spring31.web.config;

import org.springframework.context.annotation.*;
import org.springframework.security.access.AccessDecisionVoter;
import org.springframework.security.access.vote.*;
import org.springframework.security.crypto.encrypt.*;
import org.springframework.security.crypto.password.*;
import org.springframework.security.oauth2.provider.vote.ScopeVoter;

import java.util.*;


/**
 * class for all the security aware parts of the system like form-based login and OAuth
 *
 * @author Josh long
 */
@Configuration
@ImportResource({"classpath:/security.xml"})
public class SecurityConfiguration {

    private String applicationName = "crm";

    @Bean
    public UnanimousBased accessDecisionManager() {
        List<AccessDecisionVoter> decisionVoters = new ArrayList<AccessDecisionVoter>();
        decisionVoters.add(new ScopeVoter());
        decisionVoters.add(new RoleVoter());
        decisionVoters.add(new AuthenticatedVoter());
        return new UnanimousBased(decisionVoters);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return NoOpPasswordEncoder.getInstance();
    }

    @Bean
    public TextEncryptor textEncryptor() {
        return Encryptors.noOpText();
    }
}



