package com.betoniarka.biblioteka.mail;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;


@Service
public class EmailService {

    @Async
    public void send(String to, String subject, String text) {
        System.out.println("Email sent to " + to + " with subject " + subject + " and text " + text);
    }

}
