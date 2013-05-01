package org.springsource.examples.spring31.web;

import org.apache.commons.logging.*;
import org.springframework.http.MediaType;
import org.springframework.social.connect.*;
import org.springframework.social.connect.web.ProviderSignInAttempt;
import org.springframework.social.facebook.api.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springsource.examples.spring31.services.*;
import org.springsource.examples.spring31.web.util.ImageUtils;

import javax.inject.Inject;
import javax.servlet.http.*;

@Controller
public class SignUpController {


    private static final String testUrl = "/crm/test.html";
    @Inject
    private UserService userService;
    @Inject
    private UsersConnectionRepository usersConnectionRepository;
    @Inject
    private Facebook facebook;
    private Log log = LogFactory.getLog(getClass());

    @RequestMapping(value = "/crm/signup.html", method = RequestMethod.GET)
    public String signup(HttpServletRequest httpServletRequest) throws Throwable {
        HttpSession httpSession = httpServletRequest.getSession(false);
        if (httpSession != null) {
            ProviderSignInAttempt signInAttempt = (ProviderSignInAttempt) httpSession.getAttribute(ProviderSignInAttempt.SESSION_ATTRIBUTE);
            if (null != signInAttempt) {
                Connection<?> connection = signInAttempt.getConnection();
                UserProfile userProfile = connection.fetchUserProfile();

                User importedUser = importDataFromFacebook(signInAttempt, userProfile);

                ConnectionRepository connectionRepository = usersConnectionRepository.createConnectionRepository(importedUser.getUsername());
                connectionRepository.addConnection(connection);

                httpSession.removeAttribute(ProviderSignInAttempt.SESSION_ATTRIBUTE);
                return "redirect:/crm/profile.html";
            }
        }
        return "signup";
    }

    // attempts to load the profile
    // information from facebook, including profile image
    protected User importDataFromFacebook(ProviderSignInAttempt providerSignInAttempt, UserProfile userProfile) throws Throwable {
        User user;
        Connection<?> facebookConnection = providerSignInAttempt.getConnection();
        Facebook facebookApiFromConnection = (Facebook) facebookConnection.getApi();
        String username = userProfile.getUsername();
        if ((user = userService.loginByUsername(username)) == null) {
            user = userService.createUser(username, "", userProfile.getFirstName(), userProfile.getLastName(), true);
            UserOperations userOperations = facebookApiFromConnection.userOperations();
            byte[] imageBytes = userOperations.getUserProfileImage(ImageType.LARGE);
            String pathForImage = facebookConnection.getImageUrl();
            userService.establishSpringSecurityLogin(username);
            if (imageBytes != null && imageBytes.length > 0) {
                MediaType mediaType = ImageUtils.deriveImageMediaType(imageBytes);
                if (mediaType != null) {
                    pathForImage = String.format ("imported_facebook_profile_image-%s.%s" , userOperations.getUserProfile().getId()  , mediaType.getSubtype()).toLowerCase();
                }
                userService.writeUserProfilePhoto(user.getId(), pathForImage, imageBytes);
            } else {
                log.debug(String.format("could not import the user's profile photo " +
                        "byte[] buffer for username '%s' and ID # %s", user.getUsername(), user.getId() + ""));
            }

        }
        return user;
    }

}
