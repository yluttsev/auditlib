# AuditLib

Библиотека для логирования работы методов в Spring Boot приложениях.


### Конфигурация для логирования

Для работы необходимо исключить стандартную библиотеку логирования в ```pom.xml```.<br>
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter</artifactId>
    <exclusions>
        <exclusion>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-logging</artifactId>
        </exclusion>
    </exclusions>
</dependency>
```

### Способы логирования

Данная библиотека предоставляет 3 способа логирования:

- ```auditlib.appender=console``` логирует в консоль
- ```auditlib.appender=file``` логирует в файл
- ```auditlib.appender=all``` сочетает в себе два предыдущих способа

Помимо способа логирования необходимо также установить уровень логирования с помощью **auditlib.level** свойства.
Конфигурация осуществляется в *application.properties* файле.


### Логирование методов
Для логирования работы метода необходимо над установить аннотацию ```@AuditLib``` на нужный метод. Данная аннотация включает в себя обязательное свойство ```logLevel```,
которое определяет уровень логирования для данного метода.

**Пример:**

```java
@AuditLog(logLevel = LogLevel.INFO)
public void someMethod(String arg1, Integer arg2) {
    // некоторая логика метода
}
```
В случае успешного выполнения метода данная аннотация выведет в указанный способ логирования строку:<br> ```Method name: someMethod; Args: arg1, arg2; Return: void```.<br><br>
Если выполнение метода завершится выбросом исключения, аннотация выведет строку:<br> ```Method name: someMethod; Args: arg1, arg2; Throw: exception```,
где ```exception``` — тип исключения (например: ```java.lang.IllegalArgumentException```).
