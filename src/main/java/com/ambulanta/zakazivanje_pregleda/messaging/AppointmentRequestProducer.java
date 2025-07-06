package com.ambulanta.zakazivanje_pregleda.messaging;

import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class AppointmentRequestProducer {
    @Autowired
    private AmqpTemplate amqpTemplate;

    @Value("${app.rabbitmq.exchange-name}")
    private String exchange;

    @Value("${app.rabbitmq.routing-key}")
    private String routingKey;

    public void send(Long appointmentId) {
        amqpTemplate.convertAndSend(exchange, routingKey, appointmentId);
        System.out.println("Sent msg to RabbitMQ: " + appointmentId);
    }
}