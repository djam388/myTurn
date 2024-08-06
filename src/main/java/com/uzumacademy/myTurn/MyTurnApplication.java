package com.uzumacademy.myTurn;

import com.uzumacademy.myTurn.bot.MyTurnBot;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.context.annotation.ComponentScan;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@SpringBootApplication
@EntityScan("com.uzumacademy.myTurn.model")
@EnableJpaRepositories("com.uzumacademy.myTurn.repository")
@ComponentScan(basePackages = {"com.uzumacademy.myTurn"})
public class MyTurnApplication {

	public static void main(String[] args) {
		SpringApplication.run(MyTurnApplication.class, args);
	}

	@Bean
	public TelegramBotsApi telegramBotsApi(MyTurnBot myTurnBot) throws TelegramApiException {
		TelegramBotsApi api = new TelegramBotsApi(DefaultBotSession.class);
		api.registerBot(myTurnBot);
		return api;
	}

}