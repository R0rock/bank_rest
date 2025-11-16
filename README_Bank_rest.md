<h1>Система Управления Банковскими Картами (bank_rest)</h1>

<h2>Описание</h2>
<p>
  Это полнофункциональная демонстрационная REST API система для управления банковскими картами.
  Проект использует Java 23, Spring Boot 3, Spring Security (JWT), PostgreSQL и Docker.
</p>

<h2>Технологии</h2>
<ul>
  <li>Java 23</li>
  <li>Spring Boot 3.2.0</li>
  <li>Spring Security 6 + JWT</li>
  <li>Spring Data JPA (Hibernate)</li>
  <li>PostgreSQL 15</li>
  <li>Liquibase</li>
  <li>Docker и Docker Compose</li>
  <li>Swagger / OpenAPI 3</li>
  <li>Lombok</li>
  <li>JUnit 5 и Mockito</li>
  <li>AES (шифрование номеров карт)</li>
</ul>

<h2>Возможности</h2>

<h3>Аутентификация</h3>
<ul>
  <li>Регистрация: <code>POST /api/auth/signup</code></li>
  <li>Вход в систему и получение JWT: <code>POST /api/auth/signin</code></li>
</ul>

<h3>Пользователь (ROLE_USER)</h3>
<ul>
  <li>Просмотр своих карт с пагинацией: <code>GET /api/cards</code></li>
  <li>Просмотр активных карт: <code>GET /api/cards/active</code></li>
  <li>Просмотр конкретной карты: <code>GET /api/cards/{id}</code></li>
  <li>Создание новой карты для себя: <code>POST /api/cards</code></li>
  <li>Запрос на блокировку карты: <code>PUT /api/cards/{id}/block</code></li>
  <li>Запрос на активацию карты: <code>PUT /api/cards/{id}/activate</code></li>
  <li>Перевод между своими картами: <code>POST /api/transfers</code></li>
</ul>

<h3>Администратор (ROLE_ADMIN)</h3>
<ul>
  <li>Все права пользователя</li>
  <li>Просмотр всех пользователей: <code>GET /api/admin/users</code></li>
  <li>Просмотр всех карт: <code>GET /api/admin/cards</code></li>
  <li>Создание карты для любого пользователя: <code>POST /api/admin/cards</code></li>
  <li>Удаление пользователя: <code>DELETE /api/admin/users/{id}</code></li>
  <li>Удаление карты: <code>DELETE /api/admin/cards/{id}</code></li>
</ul>

<h2>Безопасность</h2>
<ul>
  <li>Шифрование номеров карт в базе данных (AES)</li>
  <li>Маскирование номеров в API (например: <code>**** **** **** 1234</code>)</li>
  <li>Разделение доступа по ролям USER и ADMIN</li>
</ul>

<h2>Быстрый старт (Docker)</h2>

<h3>1. Требования</h3>
<ul>
  <li>Docker</li>
  <li>Docker Compose</li>
  <li>Maven</li>
</ul>

<h3>2. Сборка</h3>
<pre>
mvn clean package
</pre>

<h3>3. Запуск</h3>
<pre>
docker-compose up -d --build
</pre>

<h3>4. Доступные сервисы</h3>
<ul>
  <li>Приложение: <a href="http://localhost:8080">http://localhost:8080</a></li>
  <li>Swagger UI: <a href="http://localhost:8080/swagger-ui.html">http://localhost:8080/swagger-ui.html</a></li>
  <li>PostgreSQL: <code>localhost:5432</code></li>
</ul>

<h2>Локальная разработка (без Docker)</h2>
<ul>
  <li>Запустите PostgreSQL (например: <code>docker-compose up -d postgres</code>)</li>
  <li>Настройте файл <code>src/main/resources/application.yml</code></li>
  <li>Запустите приложение:</li>
</ul>

<pre>
mvn spring-boot:run
</pre>

<h2>Тестирование</h2>
<p>Unit-тесты сервисного слоя:</p>
<pre>
mvn test
</pre>

<h2>Очистка</h2>
<p>
  Удалите временные файлы <code>README_*.md</code> после завершения разработки.
</p>