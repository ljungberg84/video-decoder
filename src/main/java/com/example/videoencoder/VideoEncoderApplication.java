package com.example.videoencoder;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.jms.annotation.EnableJms;

@SpringBootApplication
@EnableJms
@EnableEurekaClient
@EnableConfigurationProperties(StorageProperties.class)
public class VideoEncoderApplication {

    public static void main(String[] args) {
        SpringApplication.run(VideoEncoderApplication.class, args);
    }

}
