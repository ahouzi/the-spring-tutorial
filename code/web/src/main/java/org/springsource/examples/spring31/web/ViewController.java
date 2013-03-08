package org.springsource.examples.spring31.web;

import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
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
                result.reject("login.invalid", "The email and password did not match any known records. Please attempt your signin again.");
            }
        }
        return SIGNIN;
    }

    @ModelAttribute
    public SignInAttempt signInAttempt(@RequestParam String username, @RequestParam String password) {
        return new SignInAttempt(username, password);
    }

    public static class SignInAttempt {
        @NotEmpty
        private String password;

        @Email
        @NotEmpty
        private String username;

        public SignInAttempt(String username, String password) {
            this.username = username;
            this.password = password;
        }

        public String getUsername() {
            return username;
        }

        public String getPassword() {
            return password;
        }
    }
}