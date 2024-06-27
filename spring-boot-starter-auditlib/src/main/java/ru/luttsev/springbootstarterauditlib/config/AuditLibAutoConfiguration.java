package ru.luttsev.springbootstarterauditlib.config;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import ru.luttsev.springbootstarterauditlib.LogLevel;
import ru.luttsev.springbootstarterauditlib.aspect.LoggingAspect;

import java.util.Objects;

@Configuration
@ComponentScan("ru.luttsev.springbootstarterauditlib")
@PropertySource("classpath:application.properties")
@EnableConfigurationProperties(AppenderConfig.class)
public class AuditLibAutoConfiguration {

    private final AppenderConfig appenderConfig;

    public AuditLibAutoConfiguration(AppenderConfig appenderConfig) {
        this.appenderConfig = appenderConfig;
    }

    @Bean
    public LoggingAspect loggingAspect() {
        LoggingAspect loggingAspect = new LoggingAspect();
        if (Objects.nonNull(appenderConfig.getAppender()) && Objects.nonNull(appenderConfig.getLevel())) {
            switch (appenderConfig.getAppender()) {
                case "console" -> {
                    LoggerContext context = (LoggerContext) LogManager.getContext(false);
                    context.getConfiguration().getRootLogger().removeAppender("FileAppender");
                    context.updateLoggers();
                    setLogLevel(LogLevel.valueOf(appenderConfig.getLevel()), context);
                }
                case "file" -> {
                    LoggerContext context = LoggerContext.getContext(false);
                    context.getConfiguration().getRootLogger().removeAppender("ConsoleAppender");
                    context.updateLoggers();
                    setLogLevel(LogLevel.valueOf(appenderConfig.getLevel()), context);
                }
                default -> throw new IllegalArgumentException("Unknown parameter: %s".formatted(appenderConfig.getAppender()));
            }
            return loggingAspect;
        }
        throw new IllegalArgumentException("One or more logging parameters are not specified in the application.properties file");
    }

    private void setLogLevel(LogLevel level, LoggerContext loggerContext) {
        loggerContext.getConfiguration().getRootLogger().setLevel(Level.getLevel(level.name()));
    }

}
