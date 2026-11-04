package com.waltercraft.skin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
@ConfigurationPropertiesScan
public class SkinServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(SkinServiceApplication.class, args);
    }
}
