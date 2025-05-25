package com.example.coffeechat;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class CoffeechatApplication {

    public static void main(String[] args) {
        SpringApplication.run(CoffeechatApplication.class, args);
    }

}
