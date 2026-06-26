package com.ycf.liveborrowsample;

import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@ConfigurationPropertiesScan
public class LiveBorrowSampleApplication {

    public static void main(String[] args) {
        SpringApplication.run(LiveBorrowSampleApplication.class, args);
    }
}
