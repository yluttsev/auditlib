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
     * Логирование в консоль
     */
    private Boolean enableConsoleLogging = true;

    /**
     * Логирование в файл
     */
    private Boolean enableFileLogging;

    /**
     * Логирование в топик кафки
     */
    private Boolean enableKafkaLogging;

    /**
     * Уровень логирования
     */
    private String logLevel = "INFO";

    /**
     * Название топика Kafka
     */
    private String kafkaTopicName = "default";
}
