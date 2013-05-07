package org.springsource.examples.spring31.web.oauth;

import org.springframework.security.oauth2.provider.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.inject.Inject;
import java.util.Map;

/**
 * Controller for retrieving the model for and displaying the confirmation page for access to a protected resource.
 *
 * @author Josh Long
 */
@Deprecated //@Controller
@SessionAttributes("authorizationRequest")
public class AccessConfirmationController {

    private ClientDetailsService clientDetailsService;

    @RequestMapping("/oauth/confirm_access")
    public ModelAndView getAccessConfirmation(Map<String, Object> model) throws Exception {
        AuthorizationRequest clientAuth = (AuthorizationRequest) model.remove("authorizationRequest");
        ClientDetails client = clientDetailsService.loadClientByClientId(clientAuth.getClientId());
        model.put("auth_request", clientAuth);
        model.put("client", client);
        return new ModelAndView("access-confirmation", model);
    }

    @RequestMapping("/oauth/error")
    public String handleError(Map<String, Object> model) throws Exception {
        model.put("message", "There was a problem with the OAuth2 protocol");
        return "oauthError";
    }

    @Inject
    public void setClientDetailsService(ClientDetailsService clientDetailsService) {
        this.clientDetailsService = clientDetailsService;
    }

}