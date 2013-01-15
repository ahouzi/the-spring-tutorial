package org.springsource.examples.spring31.web;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.springframework.social.connect.Connection;
import org.springframework.social.connect.ConnectionRepository;
import org.springframework.social.connect.UserProfile;
import org.springframework.social.connect.UsersConnectionRepository;
import org.springframework.social.connect.web.ProviderSignInAttempt;
import org.springframework.social.facebook.api.Facebook;
import org.springframework.social.facebook.api.FacebookProfile;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springsource.examples.spring31.services.User;
import org.springsource.examples.spring31.services.UserService;
import org.springsource.examples.spring31.web.security.UserSignInUtilities;

import javax.inject.Inject;
import javax.servlet.http.HttpSession;

/**
 * @author Josh Long
 */
@Controller
public class ViewController {

    private Facebook facebook;
    private UserService userService;
    private UserSignInUtilities userSignInUtilities;
    private UsersConnectionRepository usersConnectionRepository;

    @Inject
    public void setUsersConnectionRepository(UsersConnectionRepository usersConnectionRepository) {
        this.usersConnectionRepository = usersConnectionRepository;
    }

    @Inject
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @Inject
    public void setFacebook(Facebook facebook) {
        this.facebook = facebook;
    }


    @Inject
    public void setUserSignInUtilities(UserSignInUtilities userSignInUtilities) {
        this.userSignInUtilities = userSignInUtilities;
    }

    // todo since we no longer have the ConnectionSignup, we need to take the sign in attempt,
    // todo redirect to a signup page with most information pre-filled out.
    @RequestMapping(value = "/crm/signup.html")
    public String signup(HttpSession session) {
        String providerSignInAttemptSessionAttribute =ProviderSignInAttempt.class.getName() ;
        ProviderSignInAttempt signInAttempt = (ProviderSignInAttempt) session.getAttribute(providerSignInAttemptSessionAttribute);
        if (null != signInAttempt) {
            Connection<?> connection = signInAttempt.getConnection();
            UserProfile userProfile = connection.fetchUserProfile();

            User user;

            if ((user = userService.loginByUsername(userProfile.getUsername())) == null) {
                user = this.userService.createUser(userProfile.getUsername(), "", userProfile.getFirstName(), userProfile.getLastName(), true);
            }

            ConnectionRepository connectionRepository = usersConnectionRepository.createConnectionRepository(user.getUsername());
            connectionRepository.addConnection(connection);
            userSignInUtilities.signIn(userProfile.getUsername());

            session.removeAttribute(providerSignInAttemptSessionAttribute);

            return "redirect:/crm/profile.html";
        }
        return "signup";
    }

    @RequestMapping(value = "/test", method = RequestMethod.GET)
    @ResponseBody
    public FacebookProfile test() {
        FacebookProfile facebookProfile = facebook.userOperations().getUserProfile();
        System.out.println(ToStringBuilder.reflectionToString(facebookProfile));
        return facebookProfile;
    }

    @RequestMapping(value = "/crm/signin.html", method = RequestMethod.GET)
    public String showSignInPage(Model model, @RequestParam(value = "error", required = false, defaultValue = "false") String err) {

        boolean isInError = !(StringUtils.hasText(err) &&
                (err.toLowerCase().contains("false") || err.toLowerCase().contains("true"))) ||
                Boolean.parseBoolean(err.toLowerCase());

        model.addAttribute("cgClass", isInError ? "error" : "");
        model.addAttribute("error", isInError);
        model.addAttribute("errorMessage", err);
        return "signin";
    }


}
