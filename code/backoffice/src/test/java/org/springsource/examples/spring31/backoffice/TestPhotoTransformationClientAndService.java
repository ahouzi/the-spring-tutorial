package org.springsource.examples.spring31.backoffice;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import org.springsource.examples.spring31.backoffice.config.BackOfficeConfiguration;
import org.springsource.examples.spring31.services.User;
import org.springsource.examples.spring31.services.UserService;
import org.springsource.examples.spring31.services.config.ServicesConfiguration;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;

/**
 * @author Josh Long
 */
@ActiveProfiles("default")
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(loader = AnnotationConfigContextLoader.class)
public class TestPhotoTransformationClientAndService {


    private static Logger log = Logger.getLogger(TestPhotoTransformationClientAndService.class);

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
    }

}


/**
 * class that will launch the client and the service in separate {@link ApplicationContext application contexts }
 * and exercise that messages sent in one application context are received by the Spring Integration client
 * in the other, and that the total sum of the messages processed equals the number of messages sent in the first one
 *
 * @author Josh Long
 */
class Runner {
    private Logger log = Logger.getLogger(getClass());

    private File image;

    private InputStream inputStreamForResourceFromMongoDbGridFs;

    private String profile = "default";

    public ApplicationContext runBackOffice() throws Throwable {
        return buildAndStartApplicationContext(profile, BackOfficeConfiguration.class);
    }


    public ApplicationContext runFrontOffice(int numberOfUsersAndMessagesToCreate) throws Throwable {

        ApplicationContext applicationContext = buildAndStartApplicationContext(profile, ServicesConfiguration.class);
        RabbitAdmin rabbitAdmin = applicationContext.getBean(RabbitAdmin.class);
        Queue photosQueue = applicationContext.getBean(Queue.class);
        UserService userService = applicationContext.getBean(UserService.class);

        String joshUser = "josh@joshlong.com", joshPw = "password";
        // lets create an admin user josh@...
        User josh = userService.createOrGet( joshUser, joshPw);
        User michael = userService.createOrGet("michael@domain.com", "password");

        assert userService.createOrGet( joshUser, joshPw)
                .getId().equals( josh.getId())
                : "they should be the same instance!";

        // setup consistent state
        String qName = photosQueue.getName();
        rabbitAdmin.purgeQueue(qName, false);

        // go
        InputStream inputStream = null;
        for (int x = 0; x < numberOfUsersAndMessagesToCreate; x++) {
            inputStream = new FileInputStream(image);
            long nao = System.currentTimeMillis();
            String user = "usr@web.com" + (nao), pw = "password" + (nao);
            User usr = userService.createOrGet(user, pw);
            assert usr != null : "there must be a user at this point";
            userService.writeUserProfilePhotoAndQueueForConversion(usr.getId(), image.getName(), inputStream);

            log.debug("wrote a resized image for " + usr.getId() + " having file name '" + image.getName() + "'");

            IOUtils.closeQuietly(inputStream);
        }

        return applicationContext;
    }


    @Value("classpath:/ss.png")
    public void setFileResource(Resource fileResource) throws Exception {
        inputStreamForResourceFromMongoDbGridFs = fileResource.getInputStream();
    }

    @PostConstruct
    public void setup() throws Throwable {
        FileOutputStream fileOutputStream = null;
        this.image = File.createTempFile("image", ".png");
        try {
            fileOutputStream = new FileOutputStream(image);
            IOUtils.copy(this.inputStreamForResourceFromMongoDbGridFs, fileOutputStream);
            assert this.image.exists();
        } finally {
            IOUtils.closeQuietly(this.inputStreamForResourceFromMongoDbGridFs);
            IOUtils.closeQuietly(fileOutputStream);
        }


    }

    @PreDestroy
    public void destroy() {
        assert this.image != null && (!this.image.exists() || this.image.delete());
    }

    /**
     * Builds an application context.
     *
     * @param profile the profile for the application
     * @param clazzes the configuration classes for the application itself.
     * @return the  application context  configured according to the profile and configuration classes specified
     */
    private AnnotationConfigApplicationContext buildAndStartApplicationContext(String profile, Class<?>... clazzes) {
        try {
            AnnotationConfigApplicationContext ac = new AnnotationConfigApplicationContext();
            ac.getEnvironment().setActiveProfiles(profile);
            ac.register(clazzes);
            ac.refresh();

            ac.start();
            return ac;
        } catch (Throwable t) {
            log.info("oops! " + ExceptionUtils.getFullStackTrace(t), t);
            throw new RuntimeException(t);
        }

    }
}