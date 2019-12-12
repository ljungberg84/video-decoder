package com.example.videoencoder;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.jms.annotation.EnableJms;

@SpringBootApplication
@EnableJms
public class VideoEncoderApplication {

    public static void main(String[] args) {
        SpringApplication.run(VideoEncoderApplication.class, args);
    }

}
