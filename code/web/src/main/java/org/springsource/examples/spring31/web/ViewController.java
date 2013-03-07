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

/**
 * @author Josh Long
 */
@Controller
@SessionAttributes(ViewController.USER_OBJECT_KEY)
public class ViewController {

    public static final String CRM_SIGNIN_PAGE = "/crm/signin.html";

    public static final String USER_OBJECT_KEY = "signedInUser";

    private UserService userService;

    @Inject
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @RequestMapping(value = CRM_SIGNIN_PAGE, method = RequestMethod.POST)
    public String signin(@ModelAttribute @Valid SignInAttempt signInAttempt, BindingResult result, Model model) throws Throwable {

        if (result.hasErrors())
            return "signin";

        User user = this.userService.login(signInAttempt.getUsername(), signInAttempt.getPassword());
        model.addAttribute(USER_OBJECT_KEY, user);
        return "redirect:/crm/profile.html";
    }

    @RequestMapping(value = CRM_SIGNIN_PAGE, method = RequestMethod.GET)
    public String showSignInPage() {
        return "signin";
    }


 /*

   The example works with out this part because it uses convention. But, if you wanted to construct a more model
   object based not only on the request parameters and the session attributes and cookies, you might do better
   to extract that chore to a separate @ModelAttribute object like this.

    @ModelAttribute
    public SignInAttempt attemptFromRequest(@RequestParam(value = "username", required = false) String user,
                                            @RequestParam(value = "password", required = false) String pw) throws Throwable {

        boolean hasData = StringUtils.hasText(user) && StringUtils.hasText(pw);
        return hasData ? new SignInAttempt(user, pw) : new SignInAttempt();
    }
*/

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
