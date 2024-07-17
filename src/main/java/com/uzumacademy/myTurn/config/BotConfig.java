package com.uzumacademy.myTurn.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;



@Configuration
@ConfigurationProperties(prefix = "bot")
@Data
public class BotConfig {

    private String username;

    private String token;
}
