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

    private Logger log = Logger.getLogger(getClass());
    private UserService userService;

    @Inject
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @RequestMapping(value = "/api/users/{userId}", method = RequestMethod.GET)
    @ResponseBody
    public User getUserById(@PathVariable("userId") Long userId) {
        return this.userService.getUserById(userId);
    }

    @RequestMapping(value = "/api/user/{userId}", method = RequestMethod.PUT)
    @ResponseBody
    public User updateUserById(@PathVariable("userId") Long userId,  @RequestParam String email, @RequestParam String password) {
        User usr = this.userService.updateUser(userId, email, password);

        return usr;
    }


    @RequestMapping(value = "/api/users/{userId}/photo", method = RequestMethod.POST)
    @ResponseBody
    public Long uploadBasedOnRequestParameter(@PathVariable("userId") Long userId, @RequestParam("file") MultipartFile file) {
        try {
            assert userId != null : "you must specify a userId when uploading!";
            assert file != null : "you must specify a file object when uploading!";
            byte[] bytesForImage = file.getBytes();
            userService.writeUserProfilePhotoAndQueueForConversion(userId, file.getName(), bytesForImage);
            return userId;
        } catch (Throwable th) {
            throw new RuntimeException("Something happened while uploading the managed upload", th);
        }
    }


    @RequestMapping(value = "/api/users/{userId}/photo", method = RequestMethod.GET)
    public void renderMedia(HttpServletResponse httpServletResponse, OutputStream os, @PathVariable("userId") Long userId) {
        InputStream is = userService.readUserProfilePhoto(userId);
        httpServletResponse.setContentType("image/jpg");
        if (null == is && log.isInfoEnabled()) {
            log.info("couldn't read the byte[]s for user #" + userId + " 's profile photo.");
            return;
        }
        try {
            IOUtils.copyLarge(is, os);
        } catch (Exception e1) {
            if (log.isInfoEnabled()) log.info("couldn't render the photo for user#" + userId);
            throw new RuntimeException(e1);
        } finally {
            IOUtils.closeQuietly(is);
            IOUtils.closeQuietly(os);
        }
    }

}
