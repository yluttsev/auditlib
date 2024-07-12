package ru.luttsev.springbootstarterauditlib.config;

import jakarta.annotation.PostConstruct;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import ru.luttsev.springbootstarterauditlib.LogLevel;
import ru.luttsev.springbootstarterauditlib.advice.HttpRequestLoggingAdvice;
import ru.luttsev.springbootstarterauditlib.advice.HttpResponseLoggingAdvice;
import ru.luttsev.springbootstarterauditlib.aspect.LoggingAspect;

/**
 * Автоконфигурация библиотеки
 * @author Yuri Luttsev
 */
@Configuration
@ComponentScan("ru.luttsev.springbootstarterauditlib")
@PropertySource("classpath:application.properties")
@EnableConfigurationProperties(AuditLibProperties.class)
public class AuditLibAutoConfiguration {

    /**
     * {@link AuditLibProperties Конфигурация} в properties файле
     */
    private final AuditLibProperties auditLibProperties;

    private final LoggerContext loggerContext;

    private final Logger mainLogger;

    public AuditLibAutoConfiguration(AuditLibProperties auditLibProperties) {
        this.auditLibProperties = auditLibProperties;
        this.loggerContext = LoggerContext.getContext(false);
        this.mainLogger = (Logger) LogManager.getLogger("ru.luttsev.springbootstarterauditlib");
    }

    /**
     * Бин аспекта для логирования работы методов
     * @return {@link LoggingAspect аспект} для логирования
     */
    @Bean
    public LoggingAspect loggingAspect() {
        return new LoggingAspect();
    }

    /**
     * Бин эдвайса для логирования http запросов
     * @return {@link HttpRequestLoggingAdvice эдвайс} для логирования
     */
    @Bean
    public HttpRequestLoggingAdvice httpRequestLoggingAdvice() {
        return new HttpRequestLoggingAdvice();
    }

    /**
     * Бин эдвайса для логирования http ответов
     * @return {@link HttpResponseLoggingAdvice эдвайс} для логирования
     */
    @Bean
    public HttpResponseLoggingAdvice httpResponseLoggingAdvice() {
        return new HttpResponseLoggingAdvice();
    }

    /**
     * До инициализации всех бинов приложения<br>
     * устанавливает настройки логера, указанных в application.properties
     */
    @PostConstruct
    public void configureLogger() {
        configureAppender(auditLibProperties.getAppender());
        configureLogLevel(auditLibProperties.getLevel());
    }

    private void configureLogLevel(String logLevel) {
        this.mainLogger.setLevel(LogLevel.toLog4j2Level(LogLevel.valueOf(logLevel)));
    }

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
        }
    }

}
