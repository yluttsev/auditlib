package ru.luttsev.springbootstarterauditlib.config;

import jakarta.annotation.PostConstruct;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Property;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.PropertySource;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;
import ru.luttsev.springbootstarterauditlib.LogLevel;
import ru.luttsev.springbootstarterauditlib.advice.HttpRequestLoggingAdvice;
import ru.luttsev.springbootstarterauditlib.advice.HttpResponseLoggingAdvice;
import ru.luttsev.springbootstarterauditlib.appender.KafkaAppender;
import ru.luttsev.springbootstarterauditlib.aspect.LoggingAspect;
import ru.luttsev.springbootstarterauditlib.model.KafkaMessage;

import java.util.HashMap;

/**
 * Автоконфигурация библиотеки
 *
 * @author Yuri Luttsev
 */
@AutoConfiguration
@ComponentScan("ru.luttsev.springbootstarterauditlib")
@PropertySource("classpath:application.yml")
@EnableConfigurationProperties(AuditLibProperties.class)
public class AuditLibAutoConfiguration {

    private final AuditLibProperties auditLibProperties;

    private final LoggerContext loggerContext;

    private final Logger mainLogger;

    @Value("${spring.application.name}")
    private String serviceName;

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    public AuditLibAutoConfiguration(AuditLibProperties auditLibProperties) {
        this.auditLibProperties = auditLibProperties;
        this.loggerContext = LoggerContext.getContext(false);
        this.mainLogger = (Logger) LogManager.getLogger("ru.luttsev.springbootstarterauditlib");
    }

    @Bean
    public ProducerFactory<String, KafkaMessage> producerFactory() {
        HashMap<String, Object> properties = new HashMap<>();
        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        properties.put(ProducerConfig.ACKS_CONFIG, "all");
        properties.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, "true");
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        properties.put(ProducerConfig.TRANSACTIONAL_ID_CONFIG, "auditlib-id");
        return new DefaultKafkaProducerFactory<>(properties);
    }

    @Bean
    public KafkaTemplate<String, KafkaMessage> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

    /**
     * Aspect для логирования работы методов
     *
     * @return {@link LoggingAspect аспект} для логирования
     */
    @Bean
    public LoggingAspect loggingAspect() {
        return new LoggingAspect();
    }

    /**
     * Advice для логирования HTTP запросов
     *
     * @return {@link HttpRequestLoggingAdvice эдвайс} для логирования
     */
    @Bean
    public HttpRequestLoggingAdvice httpRequestLoggingAdvice() {
        return new HttpRequestLoggingAdvice();
    }

    /**
     * Advice для логирования HTTP ответов
     *
     * @return {@link HttpResponseLoggingAdvice эдвайс} для логирования
     */
    @Bean
    public HttpResponseLoggingAdvice httpResponseLoggingAdvice() {
        return new HttpResponseLoggingAdvice();
    }

    @Bean
    public Appender kafkaAppender() {
        KafkaAppender kafkaAppender = new KafkaAppender("KafkaAppender",
                null,
                PatternLayout.createDefaultLayout(),
                false,
                new Property[]{Property.createProperty("bootstrap.servers", bootstrapServers)},
                kafkaTemplate(),
                auditLibProperties.getKafkaTopicName(),
                serviceName);
        kafkaAppender.start();
        return kafkaAppender;
    }

    /**
     * Конфигурация логгера из properties файла
     */
    @PostConstruct
    public void configureLogger() {
        configureAppender(auditLibProperties.getAppender());
        configureLogLevel(auditLibProperties.getLogLevel());
    }

    /**
     * Конфигурация уровня логирования из properties файла
     *
     * @param logLevel {@link LogLevel уровень логирования}
     */
    private void configureLogLevel(String logLevel) {
        this.mainLogger.setLevel(LogLevel.toLog4j2Level(LogLevel.valueOf(logLevel)));
    }

    /**
     * Конфигурация аппендера из properties файла
     *
     * @param appender аппендер логирования
     */
    private void configureAppender(String appender) {
        switch (appender) {
            case "console" -> {
                Appender fileAppender = this.loggerContext.getConfiguration()
                        .getAppender("FileAppender");
                this.mainLogger.removeAppender(fileAppender);
            }
            case "file" -> {
                Appender consoleAppender = this.loggerContext.getConfiguration()
                        .getAppender("ConsoleAppender");
                this.mainLogger.removeAppender(consoleAppender);
            }
            case "kafka", "all" -> this.mainLogger.addAppender(kafkaAppender());
            default -> throw new IllegalArgumentException("Unknown parameter: %s".formatted(appender));
        }
    }

}
