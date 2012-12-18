package org.springsource.cloudfoundry.cluster;


import org.springframework.integration.annotation.Header;
import org.springframework.integration.annotation.Payload;
import org.springframework.integration.annotation.Publisher;

import java.util.Date;

import static org.springsource.cloudfoundry.cluster.HeaderConstants.DATE_I_SENT_IT_HEADER;
import static org.springsource.cloudfoundry.cluster.HeaderConstants.NODE_ID_HEADER;

public class HealthManager {


    private String healthManagerName;

    HealthManager(){}

    public HealthManager(String hnName) {
        this.healthManagerName = "hm" + hnName;
    }

    @Publisher(channel = "outbound-health-status-updates")
    public void broadcastHealthStatus(     @Header(DATE_I_SENT_IT_HEADER) Date date,
                                           @Header(NODE_ID_HEADER) String nodeId,
                                       @Payload String status) {
        System.out.println(String.format("sending health manager status %s", status));

    }
}


