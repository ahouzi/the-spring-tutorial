package org.springsource.examples.spring31.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springsource.examples.spring31.services.User;
import org.springsource.examples.spring31.services.UserService;

import javax.inject.Inject;
import javax.validation.Valid;

@Controller
@SessionAttributes(ViewController.USER_OBJECT_KEY)
@RequestMapping("/crm/" + ViewController.SIGNIN + ".html")
public class ViewController {
    public static final String SIGNIN = "signin";
    public static final String USER_OBJECT_KEY = "user";
    private UserService userService;

    @Inject
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @RequestMapping(method = RequestMethod.GET)
    public String showSignInPage() {
        return SIGNIN;
    }

    @RequestMapping(method = RequestMethod.POST)
    public String signin(@ModelAttribute @Valid SignInAttempt signInAttempt, BindingResult result, Model model) throws Throwable {
        if (!result.hasErrors()) {
            User user = this.userService.login(signInAttempt.getUsername(), signInAttempt.getPassword());
            if (user != null) {
                model.addAttribute(USER_OBJECT_KEY, user);
                return "redirect:/crm/profile.html";
            } else {
                result.reject("login.invalid");
            }
        }
        return SIGNIN;
    }
}