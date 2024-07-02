package ru.luttsev.springbootstarterauditlib;

import org.apache.logging.log4j.Level;

/**
 * Уровни логирования
 * @author Yuri Luttsev
 */
public enum LogLevel {

    TRACE, DEBUG, INFO, WARN, ERROR, FATAL;

    /**
     * Преобразует локальный enum {@link LogLevel LogLevel} в {@link Level Level} библиотеки log4j2
     * @param logLevel локальный enum уровней логирования
     * @return уровень логирования для log4j2
     */
    public static Level toLog4j2Level(LogLevel logLevel) {
        return Level.getLevel(logLevel.name());
    }

}
