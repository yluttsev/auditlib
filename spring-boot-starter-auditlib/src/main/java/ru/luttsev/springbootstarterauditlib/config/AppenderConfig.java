package ru.luttsev.springbootstarterauditlib.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "auditlib")
public class AppenderConfig {

    private String appender;
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
