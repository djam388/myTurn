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

Отлично, я рад, что проблема решена. Давайте обновим README.md с инструкциями по развертыванию проекта с использованием Docker. Вот примерное содержание, которое вы можете добавить или обновить в вашем README.md:

```markdown
# MyTurn Project

## Развертывание проекта с использованием Docker

Для запуска проекта с использованием Docker, выполните следующие шаги:

1. Убедитесь, что у вас установлен Docker и Docker Compose.

2. Склонируйте репозиторий:
   ```
git clone https://github.com/djam388/myturn.git
cd myturn
   ```

3. Создайте файл `.env` в корневой директории проекта и добавьте в него следующие переменные окружения:
   ```
POSTGRES_DB=myturn
POSTGRES_USER=myturnuser
POSTGRES_PASSWORD=myturnpassword
BOT_USERNAME=your_bot_username
BOT_TOKEN=your_bot_token
   ```
   Замените `your_bot_username` и `your_bot_token` на актуальные данные вашего Telegram бота.

4. Соберите JAR-файл приложения:
   ```
./mvnw clean package -DskipTests
   ```

5. Соберите и запустите контейнеры:
   ```
docker-compose up --build
   ```

6. После успешного запуска, приложение будет доступно по адресу `http://localhost:8080`, а база данных PostgreSQL будет доступна на порту 5432.

7. Чтобы остановить и удалить контейнеры, выполните:
   ```
docker-compose down
   ```

## Дополнительные команды

- Для просмотра логов контейнеров:
  ```
docker-compose logs
  ```

- Для перезапуска отдельного сервиса (например, приложения):
  ```
docker-compose restart app
  ```

- Для входа в контейнер базы данных:
  ```
docker-compose exec db psql -U myturnuser -d myturn
  ```

## Примечания

- Убедитесь, что порты 8080 и 5432 не заняты другими приложениями на вашем компьютере.
- При изменении кода приложения необходимо пересобрать JAR-файл и перезапустить контейнеры.
- Для использования в продакшене рекомендуется настроить дополнительные параметры безопасности и оптимизации.

```

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

- `com.uzumacademy.myTurn.bot`: Содержит основные компоненты Telegram бота
   - `menu`: Новый подпакет для обработки команд меню
      - `commands`: Содержит классы отдельных команд
      - `CommandChain`: Реализует паттерн Chain of Responsibility для обработки команд
      - `MenuHandler`: Основной обработчик меню бота
   - `AppointmentHandler`: Обработчик записей на прием
   - `KeyboardFactory`: Фабрика для создания клавиатур бота
   - `MessageHandler`: Обработчик входящих сообщений
   - `MessageSender`: Отправка сообщений пользователям
   - `MyTurnBot`: Основной класс бота
   - `RegistrationHandler`: Обработчик процесса регистрации пользователей

- `com.uzumacademy.myTurn.config`: Конфигурационные классы
   - `BotConfig`: Конфигурация бота
   - `SecurityConfig`: Настройки безопасности
   - `JpaConfig`: Конфигурация JPA
   - `DataInitializer`: Инициализация начальных данных

- `com.uzumacademy.myTurn.controller`: Контроллеры REST API
   - `ClinicEmployeeAuthController`: Аутентификация сотрудников клиники
   - `ClinicEmployeeController`: Управление сотрудниками клиники
   - `ClinicManagementController`: Управление записями на прием

- `com.uzumacademy.myTurn.dto`: Data Transfer Objects
   - Классы DTO для передачи данных между слоями приложения

- `com.uzumacademy.myTurn.model`: Модели данных
   - `User`: Пользователь системы
   - `Doctor`: Врач
   - `Appointment`: Запись на прием
   - `ClinicEmployee`: Сотрудник клиники

- `com.uzumacademy.myTurn.repository`: Репозитории для работы с базой данных
   - Интерфейсы репозиториев для каждой модели данных

- `com.uzumacademy.myTurn.service`: Сервисные классы для бизнес-логики
   - `AppointmentService`: Управление записями на прием
   - `UserService`: Управление пользователями
   - `DoctorService`: Управление данными врачей
   - `ClinicEmployeeService`: Управление сотрудниками клиники
   - `AuthenticationService`: Аутентификация пользователей

- `com.uzumacademy.myTurn.security`: Компоненты безопасности
   - `JwtAuthenticationFilter`: Фильтр аутентификации JWT
   - `JwtTokenProvider`: Генерация и валидация JWT токенов

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
   "username": "admin",
   "password": "adminPassword"
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
      "userId": 1,
      "userFirstName": "о",
      "userLastName": "д",
      "userPhoneNumber": "+123456789012",
      "doctor": {
         "id": 1,
         "firstName": "Иван",
         "lastName": "Петров",
         "specialization": "Терапевт",
         "phoneNumber": "+00001234567",
         "email": "ivan.petrov@example.com",
         "createdAt": "2024-08-19T18:54:03.248352",
         "updatedAt": "2024-08-19T18:54:03.248352",
         "workingHours": [],
         "active": true
      },
      "appointmentTime": "2024-08-21T13:00:00",
      "status": "SCHEDULED"
   }
]
```

Примечание: Убедитесь, что вы включили JWT токен в заголовок Authorization для этого запроса.
