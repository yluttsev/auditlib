package ru.luttsev.springbootstarterauditlib.annotation;

import ru.luttsev.springbootstarterauditlib.LogLevel;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Аннотация для логирования http эндпоинта
 * @author Yuri Luttsev
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface WebAuditLog {

    /**
     * Устанавливает уровень логирования
     * @return уровень логирования
     */
    LogLevel logLevel();

}
