package org.springsource.cloudfoundry.cluster;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;

@Configuration
@ImportResource({"/backoffice/health-manager-producer.xml", "/backoffice/cloud-controller-consumer.xml"})
public class RabbitMqClusterConfiguration {

    private String queueName = "health";

    @Bean
    public ConnectionFactory rabbitMqConnectionFactory() throws Exception {
        return new CachingConnectionFactory("127.0.0.1", 5672);
    }

    @Bean
    public AmqpTemplate rabbitTemplate() throws Throwable {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(rabbitMqConnectionFactory());
        rabbitTemplate.setMessageConverter(rabbitTemplateMessageConverter());
        return rabbitTemplate;
    }

    @Bean
    public MessageConverter rabbitTemplateMessageConverter() {
        return new JsonMessageConverter();
    }

    @Bean
    public AmqpAdmin amqpAdmin() throws Throwable {
        return new RabbitAdmin(rabbitMqConnectionFactory());
    }

    @Bean
    public Queue healthQueue() throws Throwable {
        Queue q = new Queue(this.queueName);
        amqpAdmin().declareQueue(q);
        return q;
    }

    @Bean
    public DirectExchange healthExchange() throws Throwable {
        DirectExchange directExchange = new DirectExchange(queueName);
        this.amqpAdmin().declareExchange(directExchange);
        return directExchange;
    }

    @Bean
    public Binding healthBinding() throws Throwable {
        return BindingBuilder.bind(healthQueue()).to(healthExchange()).with(this.queueName);
    }

    @Bean
    public ErrorPrinter errorPrinter() {
        return new ErrorPrinter();
    }

}

