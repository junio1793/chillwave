package com.wrapper;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class WrapperMain {
    public static void main(String[] args) {
        SpringApplication springApplication = new SpringApplication(WrapperMain.class);
        springApplication.run(args);
    }
}