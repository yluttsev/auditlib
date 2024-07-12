package ru.luttsev.springbootstarterauditlib.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Кофигурация библиотеки
 *
 * @author Yuri Luttsev
 */
@Configuration
@ConfigurationProperties(prefix = "auditlib")
public class AuditLibProperties {

    /**
     * Appender для логирования работы методов
     */
    private String appender = "console";

    /**
     * Уровень логирования
     */
    private String logLevel = "INFO";

    public String getAppender() {
        return appender;
    }

    public void setAppender(String appender) {
        this.appender = appender;
    }

    public String getLogLevel() {
        return logLevel;
    }

    public void setLogLevel(String logLevel) {
        this.logLevel = logLevel;
    }

}
