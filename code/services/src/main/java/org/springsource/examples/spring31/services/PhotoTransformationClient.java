package org.springsource.examples.spring31.services;

import org.springframework.integration.annotation.Gateway;
import org.springframework.integration.annotation.Header;
import org.springframework.integration.annotation.Payload;

/**
 * client to a service that processes a transformation.
 * <p/>
 * Spring Integration will synthesize this interface and provide
 * a client that works over Spring Integration messaging, but abstracts
 * the clients to such a degree that they're unaware of it
 *
 * @author Josh Long
 */
public interface PhotoTransformationClient {
    /**
     * Takes the userId and loads the bytes for the
     * uploaded profile picture and then resizes
     * it and converts it to .jpg,
     *
     * @param userId the ID of the user
     * @throws Exception the exception thrown from the photo transformation service
     */
    @Gateway
    void transformUserProfilePhoto(@Payload long userId, @Header("fileExtension") String extension);
}
