package com.ambulanta.zakazivanje_pregleda.messaging;

import com.ambulanta.zakazivanje_pregleda.service.AppointmentService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AppointmentRequestConsumer {

    @Autowired
    private AppointmentService appointmentService;

    @RabbitListener(queues = "${app.rabbitmq.queue-name}")
    public void receiveMessage(Long appointmentId) {
        try {
            appointmentService.processAppointment(appointmentId);
        } catch (Exception e) {
            System.err.println("Error processing appointment ID " + appointmentId + ": " + e.getMessage());
            // TODO: Implementiraj retry mehanizam, ili posalji u "dead-letter" queue
        }
    }
}