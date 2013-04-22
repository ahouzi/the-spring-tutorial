package org.springsource.examples.spring31.web;

import org.apache.commons.io.IOUtils;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.social.connect.Connection;
import org.springframework.social.connect.ConnectionRepository;
import org.springframework.social.connect.UserProfile;
import org.springframework.social.connect.UsersConnectionRepository;
import org.springframework.social.connect.web.ProviderSignInAttempt;
import org.springframework.social.facebook.api.Facebook;
import org.springframework.social.facebook.api.FacebookProfile;
import org.springframework.social.facebook.api.impl.FacebookTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springsource.examples.spring31.services.User;
import org.springsource.examples.spring31.services.UserService;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.StringWriter;

@Controller
@RequestMapping(value = "/crm/signup.html")
public class SignUpController {

    @Inject
    private UserService userService;
    @Inject
    private UsersConnectionRepository usersConnectionRepository;

    public abstract static class JsonUtils {

        private static ObjectMapper objectMapper = new ObjectMapper();
        private static JsonFactory jsonFactory = objectMapper.getJsonFactory();

        public static String encodeAsJson(Object o) throws Throwable {
            StringWriter stringWriter = null;
            JsonGenerator generator = null;
            try {
                stringWriter = new StringWriter();
                generator = jsonFactory.createJsonGenerator(stringWriter);
                objectMapper.writeValue(generator, o);
                stringWriter.flush();
                return stringWriter.getBuffer().toString();
            } finally {
                if (null != generator)
                    generator.close();
                IOUtils.closeQuietly(stringWriter);
            }
        }
    }

    @RequestMapping(method = RequestMethod.GET)
    public String signup(HttpServletRequest httpServletRequest, Model model) throws Throwable {
        HttpSession httpSession = httpServletRequest.getSession(false);
        if (httpSession != null) {
            ProviderSignInAttempt signInAttempt = (ProviderSignInAttempt) httpSession.getAttribute(ProviderSignInAttempt.SESSION_ATTRIBUTE);
            if (null != signInAttempt) {
                Connection<?> connection = signInAttempt.getConnection();
                UserProfile userProfile = connection.fetchUserProfile();

                User user;

                if ((user = userService.loginByUsername(userProfile.getUsername())) == null) {
                    user = this.userService.createUser(userProfile.getUsername(), "", userProfile.getFirstName(), userProfile.getLastName(), true);
                }

                ConnectionRepository connectionRepository = usersConnectionRepository.createConnectionRepository(user.getUsername());
                connectionRepository.addConnection(connection);
                userService.establishSpringSecurityLogin(userProfile.getUsername());

                httpSession.removeAttribute(ProviderSignInAttempt.SESSION_ATTRIBUTE);

                return "redirect:/crm/profile.html";
            }
        }
        return "signup";
    }


}
