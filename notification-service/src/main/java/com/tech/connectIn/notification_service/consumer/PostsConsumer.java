package com.tech.connectIn.notification_service.consumer;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class PostsConsumer {

    @KafkaListener(topics = "post_created_topic")
    public void handlePostCreated(){

    }
}
