package org.springsource.examples.spring31.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springsource.examples.spring31.services.User;
import org.springsource.examples.spring31.services.UserService;

import javax.inject.Inject;
import javax.servlet.http.HttpSession;

/**
 * @author Josh Long
 */
@Controller
public class ViewController {


    public static final String CRM_SIGNIN_PAGE = "/crm/signin.html";

    public static final String USER_OBJECT_KEY = "signedInUser";

    private UserService userService;

    @Inject
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @RequestMapping(value = CRM_SIGNIN_PAGE, method = RequestMethod.POST)
    public String signin(@RequestParam("username") String user,
                         @RequestParam("pw") String pw,
                         HttpSession httpSession) throws Throwable {
        User u = this.userService.login(user, pw);
        assert u != null : "the user can't be null";
        httpSession.setAttribute(USER_OBJECT_KEY, u);
        return "redirect:/crm/profile.html";
    }

    @RequestMapping(value = CRM_SIGNIN_PAGE, method = RequestMethod.GET)
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
