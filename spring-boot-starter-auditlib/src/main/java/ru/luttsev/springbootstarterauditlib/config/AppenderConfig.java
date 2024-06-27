package ru.luttsev.springbootstarterauditlib.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Кофигурация библиотеки
 * @author Yuri Luttsev
 */
@Configuration
@ConfigurationProperties(prefix = "auditlib")
public class AppenderConfig {

    /**
     * Appender для логирования работы методов
     */
    private String appender;

    /**
     * Уровень логирования
     */
    private String level;

    public String getAppender() {
        return appender;
    }

    public void setAppender(String appender) {
        this.appender = appender;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

}
