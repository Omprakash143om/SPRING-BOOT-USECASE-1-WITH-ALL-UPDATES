package com.example.goodreads;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching   //Enable Spring Cache
public class GoodreadsApplication {
    public static void main(String[] args) {
        SpringApplication.run(GoodreadsApplication.class, args);
    }
}
