package ru.luttsev.springbootstarterauditlib.annotation;

import ru.luttsev.springbootstarterauditlib.LogLevel;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Аннотация для логирования работы метода
 *
 * @author Yuri Luttsev
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AuditLog {

    /**
     * Устанавливает уровень логирования
     *
     * @return уровень логирования
     */
    LogLevel logLevel();

}
