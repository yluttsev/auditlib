package ru.luttsev.springbootstarterauditlib.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Кофигурация библиотеки
 *
 * @author Yuri Luttsev
 */
@Configuration
@ConfigurationProperties(prefix = "auditlib")
@Getter
@Setter
public class AuditLibProperties {

    /**
     * Appender для логирования работы методов
     */
    private String appender = "console";

    /**
     * Уровень логирования
     */
    private String logLevel = "INFO";

    /**
     * Название топика Kafka
     */
    private String kafkaTopicName = "default";
}
