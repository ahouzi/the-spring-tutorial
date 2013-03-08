package org.springsource.examples.spring31.web;

import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotEmpty;
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
public class ViewController {
    public static final String SIGNIN = "signin";
    public static final String CRM_SIGNIN_PAGE = "/crm/" + SIGNIN + ".html";
    public static final String USER_OBJECT_KEY = "user";
    private UserService userService;

    @Inject
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @RequestMapping(value = CRM_SIGNIN_PAGE, method = RequestMethod.GET)
    public String showSignInPage() {
        return SIGNIN;
    }

    @RequestMapping(value = CRM_SIGNIN_PAGE, method = RequestMethod.POST)
    public String signin(@ModelAttribute("signinAttempt") @Valid SignInAttempt signInAttempt, BindingResult result, Model model) throws Throwable {
        if (!result.hasErrors()) {
            User user = this.userService.login(signInAttempt.getUsername(), signInAttempt.getPassword());
            if (user != null) {
                model.addAttribute(USER_OBJECT_KEY, user);
                return "redirect:/crm/profile.html";
            } else {
                result.reject("login.invalid", "The email and password did not match any known records. Please attempt your signin again.");
            }
        }
        return SIGNIN;
    }

    public static class SignInAttempt {
        @NotEmpty
        private String password;
        @Email
        @NotEmpty
        private String username;

        public SignInAttempt() {
        }

        public SignInAttempt(String username, String password) {
            this.username = username;
            this.password = password;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }
}