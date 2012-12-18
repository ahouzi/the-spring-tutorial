package org.springsource.examples.spring31.backoffice;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.springframework.integration.Message;
import org.springframework.integration.annotation.Header;
import org.springframework.integration.annotation.Payload;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springsource.examples.spring31.backoffice.utils.ImageResizeUtil;
import org.springsource.examples.spring31.services.User;
import org.springsource.examples.spring31.services.UserService;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.io.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 * @author Josh Long
 */
@Component("photoConverter")
public class PhotoTransformationService {

    private Logger logger = Logger.getLogger(getClass());
    private int defaultImageWidth = 300;
    private UserService userService;

    // this needs to be on the path
    private String convertCommandLinePath = "convert";

    private AtomicInteger atomicInteger = new AtomicInteger(0);

    /**
     * mainly for debugging purposes
     */
    public AtomicInteger getAtomicInteger() {
        return this.atomicInteger;
    }

    @Inject
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @PostConstruct
    public void setup() {
        assert this.defaultImageWidth > 0 : "the width must be a positive number. By default this value is initialized to '300'.";
        assert StringUtils.hasText(this.convertCommandLinePath) : "You must provide a value for the 'convert' command's command line argument.";
    }

    /**
     * place to set the path of the 'convert' ImageMagic tool. This is one place where the
     * configuration of this class can benefit greatly from the use of Spring's profiles, allowing
     * different environments to be deployed to with ease. The path for the convert library is different on Cloud Foundry,
     * in our case, than it would be on any of our local development machines.
     */
    public void setConvertCommandLinePath(String x) {
        this.convertCommandLinePath = x;
    }

    /**
     * Tell the processor what width the images need to be. These are
     * going to be profile photo images, so they need to be small.
     * <p/>
     * There's not going to be a large profile shot, and we don't
     * want a large memory footprint serving up lots of images.
     *
     * @param dw desired width
     */
    public void setDefaultImageWidth(int dw) {
        this.defaultImageWidth = dw;
    }

    /**
     * This method handles all incoming Spring Integration messages and copies the profile photo bytes
     * to a staging photo which can be then manipulate to perform the conversion and rotation.
     * <p/>
     * Finally, we'll connect the work back to the original gridFs file system entry.
     *
     * @throws Throwable
     */
    @ServiceActivator
    public long convertAndResizeUserProfilePhoto(@Header("fileExtension") String fileExtension, @Payload Long userId) throws Throwable {
        InputStream profilePhotoBytesFromGridFs = null, fileInputStream = null;
        OutputStream outputStream = null;
        File tmpStagingFile = null, convertedFile = null;

        try {
            User user = userService.getUserById(userId);
            assert user != null : "the user reference should still be valid!";
            tmpStagingFile = File.createTempFile("profilePhoto" + userId, "." + fileExtension);
            convertedFile = File.createTempFile("profilePhotoConvertedAndResized" + userId, ".jpg");
            profilePhotoBytesFromGridFs = userService.readUserProfilePhoto(userId);
            outputStream = new FileOutputStream(tmpStagingFile);
            copyStreamsAndClose(profilePhotoBytesFromGridFs, outputStream);
            ImageResizeUtil.resizeToWidth(tmpStagingFile, convertedFile, this.defaultImageWidth, userId);
            fileInputStream = new FileInputStream(convertedFile);
            userService.writeUserProfilePhoto(userId, convertedFile.getName(), fileInputStream);
            logger.debug("wrote converted image for " + userId + ". The temporary staging file is " + convertedFile.getAbsolutePath());
        } finally {
            IOUtils.closeQuietly(fileInputStream);
            IOUtils.closeQuietly(profilePhotoBytesFromGridFs);
            IOUtils.closeQuietly(outputStream);
            assert ensureRemovalOfFile(convertedFile) : "the file " + pathForFile(convertedFile) + " must be either be deleted or not exist.";
            assert ensureRemovalOfFile(tmpStagingFile) : "the file " + pathForFile(convertedFile) + " must be either deleted or not exist.";
        }
        atomicInteger.incrementAndGet();

        return userId;
    }

    private String pathForFile(File fi) {
        return fi == null ? "null" : fi.getAbsolutePath();
    }

    /**
     * copy the streams, and make sure that the streams' descriptors are closed.
     *
     * @param inputStream  the input stream
     * @param outputStream the output stream
     */
    private void copyStreamsAndClose(InputStream inputStream, OutputStream outputStream) throws Exception {
        assert null != inputStream : "the input stream can't be null";
        assert null != outputStream : "the output stream can't be null";
        try {
            IOUtils.copy(inputStream, outputStream);
        } finally {
            IOUtils.closeQuietly(inputStream);
            IOUtils.closeQuietly(outputStream);
        }

    }

    /**
     * Deletes the file if it exists
     *
     * @param file the {@link File file} object to remove
     * @return whether or not
     */
    private boolean ensureRemovalOfFile(File file) {
        return null != file && (!file.exists() || file.delete());
    }

    /**
     * logs information about the incoming message requests.
     *
     * @param msg the incoming Spring Integration message.
     * @throws Throwable
     */
    private void dumpInformationAboutTheIncomingMessage(Message<?> msg) throws Throwable {
        if (null == msg || !logger.isDebugEnabled())
            return;
        logger.debug("Payload: " + msg.getPayload() + "");
        for (String k : msg.getHeaders().keySet()) {
            logger.debug(k + "=" + msg.getHeaders().get(k));
        }
    }

}
