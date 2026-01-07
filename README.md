# Electronic Store Project

Веб-застосунок для магазину електроніки (Java Servlet API + React).

## 📚 Документація проєкту (Javadoc)

Повна технічна документація класів та методів доступна за посиланням нижче:

👉 **[ВІДКРИТИ ДОКУМЕНТАЦІЮ (GitHub Pages)](https://ваш-нік.github.io/назва-репозиторію/)**

---

## 🛠 Технології
* **Backend:** Java 17, Jakarta Servlets, JDBC
* **Database:** PostgreSQL / Oracle
* **Build Tool:** Gradle / Maven
* **Frontend:** React.js

## 🚀 Як запустити
1. Клонуйте репозиторій.
2. Налаштуйте `DBConnection.java` (введіть ваші логін/пароль до БД).
3. Запустіть скрипт ініціалізації БД.
4. Запустіть сервлети через Tomcat.

## 📋 Структура API
* `POST /api/auth/register` - Реєстрація
* `POST /api/auth/login` - Вхід
* `GET /api/products` - Каталог товарів
* `GET /api/cart` - Корзина
* `POST /api/orders` - Оформлення замовлення
