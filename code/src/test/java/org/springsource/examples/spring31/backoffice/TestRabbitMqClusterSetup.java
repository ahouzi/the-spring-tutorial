package org.springsource.examples.spring31.backoffice;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import org.springsource.cloudfoundry.cluster.CloudController;
import org.springsource.cloudfoundry.cluster.HealthManager;
import org.springsource.cloudfoundry.cluster.RabbitMqClusterConfiguration;

import javax.inject.Inject;
import java.util.Date;

/**
 * @author Josh Long
 */
@ActiveProfiles("default")
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(loader = AnnotationConfigContextLoader.class)
public class TestRabbitMqClusterSetup {


    private volatile HealthManager[] healthManagers;

    private volatile CloudController[] cloudControllers;

    @Inject
    public void setHealthManagers(HealthManager[] h) {
        this.healthManagers = h;
    }

    @Inject
    public void setCloudControllers(CloudController[] c) {
        this.cloudControllers = c;
    }

    @Test
    public void proveThatOneCloudControllerReceivesTheMessage() throws Throwable {

        int nodeId = 0;
        for (HealthManager healthManager : this.healthManagers) {
            nodeId += 1;
            if (Math.random() > .5) {
                healthManager.broadcastHealthStatus(new Date(),
                        Integer.toString(nodeId),
                        String.format("Node #%s is having serious trouble!", nodeId));
            }
        }
    }


    @Configuration
    @Import(RabbitMqClusterConfiguration.class)
    public static class TestConfiguration {


        private CloudController cloudController(String name) {
            return new CloudController(name);
        }

        private HealthManager healthManager(String n) {
            return new HealthManager(n);
        }

        @Bean
        public HealthManager hm1() {
            return healthManager("1");
        }

        @Bean
        public HealthManager hm2() {
            return healthManager("2");
        }

        @Bean
        public HealthManager hm3() {
            return healthManager("3");
        }

        @Bean
        public CloudController cc1() {
            return cloudController("1");
        }

        @Bean
        public CloudController cc2() {
            return cloudController("2");
        }

        @Bean
        public CloudController cc3() {
            return cloudController("3");
        }

    }


/*
private static Logger log = Logger.getLogger(TestRabbitMqClusterSetup.class);

@Configuration
@EnableAsync
public static class TestConfiguration {

    @Bean
    public TaskExecutor taskExecutor() {
        return new SimpleAsyncTaskExecutor();
    }

    @Bean
    public Runner runner() {
        return new Runner();
    }
}

@Inject
private Runner runner;

@Test
public void runBothClientAndServer() throws Throwable {
    int howManyMsgsToSend = 5;
    runner.runFrontOffice(howManyMsgsToSend);
    if (true) return;
    ApplicationContext backOfficeApplicationContext = runner.runBackOffice();
    PhotoTransformationService photoTransformationService = backOfficeApplicationContext.getBean(PhotoTransformationService.class);
    while (photoTransformationService.getAtomicInteger().get() < howManyMsgsToSend) {
        Thread.sleep(1000);
    }
    assert photoTransformationService.getAtomicInteger().get() >= howManyMsgsToSend : "there should be " + howManyMsgsToSend + " messages.";
}*/

}

