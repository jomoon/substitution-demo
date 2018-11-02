package com.hjj.substitutiondemo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class SubstitutionDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(SubstitutionDemoApplication.class, args);
    }
}
