package org.springsource.examples.spring31.web;

import org.springframework.social.connect.*;
import org.springframework.social.connect.web.ProviderSignInAttempt;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springsource.examples.spring31.services.*;

import javax.inject.Inject;
import javax.servlet.http.*;

@Controller
@RequestMapping(value = "/crm/signup.html")
public class SignUpController {

    @Inject
    private UserService userService;
    @Inject
    private UsersConnectionRepository usersConnectionRepository;


    @RequestMapping(method = RequestMethod.GET)
    public String signup(HttpServletRequest httpServletRequest, Model model) throws Throwable {
        HttpSession httpSession = httpServletRequest.getSession(false);
        if (httpSession != null) {
            ProviderSignInAttempt signInAttempt = (ProviderSignInAttempt) httpSession.getAttribute(ProviderSignInAttempt.SESSION_ATTRIBUTE);
            if (null != signInAttempt) {
                Connection<?> connection = signInAttempt.getConnection();
                UserProfile userProfile = connection.fetchUserProfile();

                User user;

                if ((user = userService.loginByUsername(userProfile.getUsername())) == null) {
                    user = this.userService.createUser(userProfile.getUsername(), "", userProfile.getFirstName(), userProfile.getLastName(), true);
                }

                ConnectionRepository connectionRepository = usersConnectionRepository.createConnectionRepository(user.getUsername());
                connectionRepository.addConnection(connection);
                userService.establishSpringSecurityLogin(userProfile.getUsername());

                httpSession.removeAttribute(ProviderSignInAttempt.SESSION_ATTRIBUTE);

                return "redirect:/crm/profile.html";
            }
        }
        return "signup";
    }


}
