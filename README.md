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

# Выгрузка списка записей к специалистам



## Аутентификация

Для использования API MyTurn необходимо сначала пройти аутентификацию. API использует JWT (JSON Web Token) для аутентификации.

### Вход в систему

```
POST http://localhost:8080/api/clinic/auth/login
```

Тело запроса:
```json
{
  "username": "ваше_имя_пользователя",
  "password": "ваш_пароль"
}
```

Ответ:
```json
{
  "token": "ваш_jwt_токен"
}
```

Используйте этот токен в заголовке Authorization для последующих запросов:
```
Authorization: Bearer ваш_jwt_токен
```

## Записи на прием

### Получение списка записей

Получает список записей на прием на основе указанных фильтров.

```
GET http://localhost:8080/api/clinic/appointments
```

Параметры запроса:
- `startDate` (необязательно): Начальная дата диапазона записей (формат ISO 8601)
- `endDate` (необязательно): Конечная дата диапазона записей (формат ISO 8601)
- `doctorId` (необязательно): ID врача
- `status` (необязательно): Статус записи (например, SCHEDULED, COMPLETED, CANCELLED)
- `sortBy` (необязательно, по умолчанию: "appointmentTime"): Поле для сортировки
- `sortDirection` (необязательно, по умолчанию: "asc"): Направление сортировки ("asc" или "desc")

Пример запроса:
```
GET http://localhost:8080/api/clinic/appointments?startDate=2024-08-19T00:00:00&endDate=2024-08-26T23:59:59&doctorId=1&status=SCHEDULED
```

Ответ:
```json
[
  {
    "id": 1,
    "userId": 100,
    "userFirstName": "Иван",
    "userLastName": "Иванов",
    "userPhoneNumber": "+79001234567",
    "doctor": {
      "id": 1,
      "firstName": "Петр",
      "lastName": "Петров",
      "specialization": "Кардиолог"
    },
    "appointmentTime": "2024-08-20T10:00:00",
    "status": "SCHEDULED"
  },
  // ... другие записи
]
```

Примечание: Убедитесь, что вы включили JWT токен в заголовок Authorization для этого запроса.