# MyTurn - Telegram Bot для записи к врачу

## Описание
MyTurn - это Telegram бот, разработанный для упрощения процесса записи пациентов к врачам. Бот позволяет пользователям регистрироваться, просматривать список доступных врачей, записываться на прием и управлять своими записями.

## Требования
- Java 21
- Spring Boot 3.3.1
- Maven
- H2 Database
- Telegram Bot API

## Зависимости
- spring-boot-starter
- spring-boot-starter-data-jpa
- spring-boot-starter-web
- telegrambots-spring-boot-starter
- lombok
- h2 (для базы данных)

## Настройка и запуск

1. Клонируйте репозиторий:
   https://github.com/djam388/myTurn.git

2. Настройте параметры бота в `application.properties`:

bot.username=your_bot_username
bot.token=your_bot_token

Замените `your_bot_username` и `your_bot_token` на актуальные данные вашего Telegram бота.

## Использование

1. Найдите бота в Telegram по имени, указанному в `bot.username`.
2. Нажмите "Начать" для запуска процесса регистрации.
3. Следуйте инструкциям бота для завершения регистрации.
4. После регистрации используйте кнопку "Меню" для доступа к функциям бота:
- Просмотр списка врачей
- Запись на прием
- Просмотр ваших записей
- Просмотр вашего профиля

## Структура проекта

- `com.uzumacademy.myTurn.bot`: Содержит основную логику Telegram бота
- `com.uzumacademy.myTurn.config`: Конфигурационные классы
- `com.uzumacademy.myTurn.model`: Модели данных (User, Doctor, Appointment)
- `com.uzumacademy.myTurn.repository`: Репозитории для работы с базой данных
- `com.uzumacademy.myTurn.service`: Сервисные классы для бизнес-логики