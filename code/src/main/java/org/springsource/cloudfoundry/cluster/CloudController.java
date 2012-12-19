package org.springsource.cloudfoundry.cluster;


import org.apache.commons.lang.StringUtils;
import org.springframework.integration.Message;
import org.springframework.integration.annotation.ServiceActivator;

/**
 * Handles the health of the cloud
 * <p/>
 * Receives requests and needs to send the replies back to the Health Manager
 */
public class CloudController {

    @ServiceActivator(inputChannel = "enriched-inbound-health-status-updates")
    public void handleMesssagesFromTheCluster(Message<?> incomingSpringIntegrationMessage) {


        System.out.println(StringUtils.repeat("-", 100));
        System.out.println(String.format("Received a message from NATS on thread %s.", Thread.currentThread().getName()));
        for (String k : incomingSpringIntegrationMessage.getHeaders().keySet())
            System.out.println(String.format("%s=%s", k, incomingSpringIntegrationMessage.getHeaders().get(k)));
        Object payload = incomingSpringIntegrationMessage.getPayload();

        System.out.println(String.format("The payload is %s", payload));

    }


}
