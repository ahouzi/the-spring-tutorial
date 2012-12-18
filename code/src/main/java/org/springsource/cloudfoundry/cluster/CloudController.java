package org.springsource.cloudfoundry.cluster;


import org.springframework.integration.annotation.Header;
import org.springframework.integration.annotation.Payload;
import org.springframework.integration.annotation.ServiceActivator;

import java.util.Date;

import static org.springsource.cloudfoundry.cluster.HeaderConstants.DATE_I_SENT_IT_HEADER;
import static org.springsource.cloudfoundry.cluster.HeaderConstants.NODE_ID_HEADER;

/**
 * Handles the health of the cloud
 * <p/>
 * Receives requests and needs to send the replies back to the Health Manager
 */
public class CloudController {
    private String cloudControllerName;

    CloudController() {
    }

    public CloudController(String ccn) {
        this.cloudControllerName = "cc" + ccn;
    }


    @ServiceActivator(inputChannel = "enriched-inbound-health-status-updates")
    public void handleMesssagesFromTheCluster(
            @Header(DATE_I_SENT_IT_HEADER) Date date,
            @Header(NODE_ID_HEADER) String nodeId,
            @Payload String natsMessage) {
        System.out.println(String.format("Received NATS message (%s)", natsMessage));
    }


}
