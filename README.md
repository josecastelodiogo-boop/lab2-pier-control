# SkyService — Лабораторная работа №3, часть 2

Java 17+ · Maven · Lombok · Datafaker · Stream API · JUnit 5

## Запуск в IntelliJ IDEA
1. File → Open → выбрать папку проекта (там, где `pom.xml`) → *Open as Project*.
2. Дождаться загрузки Maven (кнопка «Load Maven Changes», если появится).
3. **Settings → Build, Execution, Deployment → Compiler → Annotation Processors → Enable annotation processing** (обычно включено автоматически).
4. Установить плагин **Lombok** (в новых IDEA встроен).
5. Запуск: открыть `Main.java` → зелёная стрелка у `main`.

## Maven (панель Maven или терминал)
```
mvn clean test      # прогон JUnit-тестов
mvn package         # сборка target/skyservice-1.0.jar
java -jar target/skyservice-1.0.jar
```
