package org.springsource.examples.spring31.web;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.social.connect.Connection;
import org.springframework.social.connect.ConnectionRepository;
import org.springframework.social.facebook.api.Facebook;
import org.springframework.social.facebook.api.FacebookProfile;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springsource.examples.spring31.services.UserService;
import org.springsource.examples.spring31.web.config.SocialConfiguration;

import javax.inject.Inject;

/**
 * @author Josh Long
 */
@Controller
public class ViewController {

    @Inject
    private SocialConfiguration social;

    /**
     * todo
     * - support a checkbox to 'get profile image from facebook'
     * - redirect the user to a login screen with the newly created user name already filled in on successfull account creation
     * - finish the Customer display screen
     *
     * @return
     */

    public ConnectionRepository connectionRepository() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        UserDetails crmUserDetails = (UserDetails) principal;
        return social.usersConnectionRepository().createConnectionRepository(crmUserDetails.getUsername());
    }

    @RequestMapping(value = "/test", method = RequestMethod.GET)
    @ResponseBody
    public FacebookProfile test() {
        Connection<Facebook> co = connectionRepository().findPrimaryConnection(Facebook.class);
        Facebook facebook = co.getApi();
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
