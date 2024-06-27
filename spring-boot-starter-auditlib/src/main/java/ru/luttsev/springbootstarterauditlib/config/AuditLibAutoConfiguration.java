package ru.luttsev.springbootstarterauditlib.config;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.springframework.boot.context.event.ApplicationContextInitializedEvent;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.event.EventListener;
import ru.luttsev.springbootstarterauditlib.LogLevel;
import ru.luttsev.springbootstarterauditlib.advice.HttpRequestLoggingAdvice;
import ru.luttsev.springbootstarterauditlib.advice.HttpResponseLoggingAdvice;
import ru.luttsev.springbootstarterauditlib.aspect.LoggingAspect;

import java.util.Objects;

/**
 * Автоконфигурация библиотеки
 * @author Yuri Luttsev
 */
@Configuration
@ComponentScan("ru.luttsev.springbootstarterauditlib")
@PropertySource("classpath:application.properties")
@EnableConfigurationProperties(AppenderConfig.class)
public class AuditLibAutoConfiguration {

    /**
     * {@link AppenderConfig Конфигурация} в properties файле
     */
    private final AppenderConfig appenderConfig;

    public AuditLibAutoConfiguration(AppenderConfig appenderConfig) {
        this.appenderConfig = appenderConfig;
    }

    /**
     * Настройка {@link LoggingAspect аспекта} для логирования<br>
     * Устанавливает аппендер, указанный в application.properties
     *
     * @return {@link LoggingAspect аспект} для логирования
     */
    @Bean
    public LoggingAspect loggingAspect() {
        return new LoggingAspect();
    }

    @Bean
    public HttpRequestLoggingAdvice httpRequestLoggingAdvice() {
        return new HttpRequestLoggingAdvice();
    }

    @Bean
    public HttpResponseLoggingAdvice httpResponseLoggingAdvice() {
        return new HttpResponseLoggingAdvice();
    }

    @EventListener(ApplicationContextInitializedEvent.class)
    public void setConfiguration() {
        if (Objects.nonNull(appenderConfig.getAppender()) && Objects.nonNull(appenderConfig.getLevel())) {
            switch (appenderConfig.getAppender()) {
                case "console" -> {
                    LoggerContext context = (LoggerContext) LogManager.getContext(false);
                    context.getConfiguration().getRootLogger().removeAppender("FileAppender");
                    setLogLevel(LogLevel.valueOf(appenderConfig.getLevel()), context);
                    context.updateLoggers();
                }
                case "file" -> {
                    LoggerContext context = LoggerContext.getContext(false);
                    context.getConfiguration().getRootLogger().removeAppender("ConsoleAppender");
                    setLogLevel(LogLevel.valueOf(appenderConfig.getLevel()), context);
                    context.updateLoggers();
                }
                default ->
                        throw new IllegalArgumentException("Unknown parameter: %s".formatted(appenderConfig.getAppender()));
            }
        }
        throw new IllegalArgumentException("One or more logging parameters are not specified in the application.properties file");
    }

    /**
     * Устанавливает уровень логирования, указанный
     * в properties файле
     *
     * @param level         уровень логирования
     * @param loggerContext контекст логирования
     */
    private void setLogLevel(LogLevel level, LoggerContext loggerContext) {
        loggerContext.getConfiguration().getRootLogger().setLevel(Level.getLevel(level.name()));
    }

}
