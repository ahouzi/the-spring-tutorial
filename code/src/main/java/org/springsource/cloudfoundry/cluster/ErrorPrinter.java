package org.springsource.cloudfoundry.cluster;


import org.springframework.integration.Message;
import org.springframework.integration.annotation.ServiceActivator;

public class ErrorPrinter {
    @ServiceActivator
    static public void printErrorMessage(Message<?> msg) throws Throwable {
        System.out.println(String.format("the message is ", msg.getPayload()));
        for (String k : msg.getHeaders().keySet())
            System.out.println(String.format("%s=%s", k, msg.getHeaders().get(k)));
    }
}
