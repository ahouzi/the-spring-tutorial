package org.springsource.examples.spring31.web;

import org.springframework.stereotype.Controller;
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

    public static final String USER_OBJECT_KEY = "signedInUser";

    private UserService userService;

    @Inject
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @RequestMapping(value = "/crm/users/signin", method = RequestMethod.POST)
    public String signin(@RequestParam("username") String user,
                         @RequestParam("pw") String pw,
                         HttpSession httpSession) throws Throwable {
        User u = this.userService.login(user, pw);
        assert u != null : "the user can't be null";
        httpSession.setAttribute(USER_OBJECT_KEY, u);
        return "profile";
    }
}
