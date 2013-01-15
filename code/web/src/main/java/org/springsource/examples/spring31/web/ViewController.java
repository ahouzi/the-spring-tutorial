package org.springsource.examples.spring31.web;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.social.connect.Connection;
import org.springframework.social.connect.ConnectionRepository;
import org.springframework.social.connect.UsersConnectionRepository;
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

    /**
     * todo
     * - support a checkbox to 'get profile image from facebook'
     * - redirect the user to a login screen with the newly created user name already filled in on successfull account creation
     * - finish the Customer display screen
     *
     * @return
     */

    private Facebook facebook ;

    @Inject
    public void setFacebook(Facebook facebook ){
        this.facebook = facebook ;
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
