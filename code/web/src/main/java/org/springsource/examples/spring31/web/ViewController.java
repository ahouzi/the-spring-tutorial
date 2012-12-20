package org.springsource.examples.spring31.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author Josh Long
 */
@Controller
public class ViewController {


    @RequestMapping(value = "/crm/signin.html", method = RequestMethod.GET)
    public String showSignInPage(Model model, @RequestParam(value = "error", required = false, defaultValue = "false") String err

    ) {

        boolean isInError = !(StringUtils.hasText(err) &&
                (err.toLowerCase().contains("false") || err.toLowerCase().contains("true"))) || Boolean.parseBoolean(err.toLowerCase());

        model.addAttribute("cgClass", isInError ? "error" : "");
        model.addAttribute("error", isInError);
        model.addAttribute("errorMessage", err);
        return "signin";
    }


}
