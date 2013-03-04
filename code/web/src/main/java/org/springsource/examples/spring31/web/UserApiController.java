package org.springsource.examples.spring31.web;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springsource.examples.spring31.services.User;
import org.springsource.examples.spring31.services.UserService;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Simple Spring MVC controller that can be used to adminster information about the users
 *
 * @author Josh Long
 */
@Controller
public class UserApiController {

    /**
     * Root URL template for all modifications to a {@link User}
     */
    static public final String USER_COLLECTION_URL = "/api/users";
    static public final String USER_COLLECTION_ENTRY_URL = USER_COLLECTION_URL + "/{userId}";

    private Logger log = Logger.getLogger(getClass());
    private UserService userService;

    @Inject
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @RequestMapping(value = USER_COLLECTION_ENTRY_URL, method = RequestMethod.GET)
    @ResponseBody
    public User getUserById(@PathVariable("userId") Long userId) {
        return this.userService.getUserById(userId);
    }

    @RequestMapping(value = USER_COLLECTION_ENTRY_URL, method = RequestMethod.PUT)
    @ResponseBody
    public User updateUserById(@PathVariable("userId") Long userId,
                               @RequestParam("username") String username,
                               @RequestParam("password") String password,
                               @RequestParam("firstname") String fn,
                               @RequestParam("lastname") String ln) {
        User existingUser = this.getUserById(userId);
        return this.userService.updateUser(userId, username, password, fn, ln, existingUser.isImportedFromServiceProvider());
    }

    @RequestMapping(value = USER_COLLECTION_URL + "/usernames/{username}", method = RequestMethod.GET)
    @ResponseBody
    public boolean isUserNameTaken(@PathVariable("username") String username) {
        boolean taken = this.userService.isUserNameAlreadyTaken(username);
        log.debug("the username " + username + " is taken: " + taken);
        return taken;
    }

    @RequestMapping(value = USER_COLLECTION_URL, method = RequestMethod.POST)
    @ResponseBody
    public User registerUser(@RequestParam("username") String username,
                             @RequestParam("password") String password,
                             @RequestParam("firstname") String fn,
                             @RequestParam("lastname") String ln,
                             @RequestParam("imported") boolean importedFromServiceProvider) {
        return this.userService.createOrGet(username, password, fn, ln, importedFromServiceProvider);
    }

    @RequestMapping(value = USER_COLLECTION_ENTRY_URL + "/photo", method = RequestMethod.POST)
    @ResponseBody
    public Long uploadBasedOnPathVariable(final @PathVariable("userId") Long userId, final @RequestParam("file") MultipartFile file) throws Throwable {
        assert userId != null : "you must specify a userId when uploading!";
        assert file != null : "you must specify a file object when uploading!";
        byte[] bytesForImage = file.getBytes();
        userService.writeUserProfilePhotoAndQueueForConversion(userId, file.getName(), bytesForImage);
        return userId;
    }


    @RequestMapping(value = USER_COLLECTION_ENTRY_URL + "/photo", method = RequestMethod.GET)
    public void renderMedia(HttpServletResponse httpServletResponse, OutputStream os, @PathVariable("userId") Long userId) throws Throwable {
        InputStream is = userService.readUserProfilePhoto(userId);
        httpServletResponse.setContentType("image/jpg");
        if (null == is) {
            return;
        }
        try {
            IOUtils.copyLarge(is, os);
        } finally {
            IOUtils.closeQuietly(is);
            IOUtils.closeQuietly(os);
        }
    }

}
