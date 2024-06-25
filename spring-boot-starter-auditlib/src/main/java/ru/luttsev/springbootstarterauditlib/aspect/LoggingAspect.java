package ru.luttsev.springbootstarterauditlib.aspect;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import ru.luttsev.springbootstarterauditlib.LogLevel;
import ru.luttsev.springbootstarterauditlib.annotation.AuditLog;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Aspect
@Component
public class LoggingAspect {

    private final Logger logger = LogManager.getLogger(this.getClass());

    @Pointcut("@annotation(auditLog)")
    public void annotatedMethods(AuditLog auditLog) {
    }

    @Around(value = "annotatedMethods(auditLog)", argNames = "joinPoint, auditLog")
    public void logMethod(ProceedingJoinPoint joinPoint, AuditLog auditLog) throws Throwable {
        String methodName = joinPoint.getSignature().getName();
        List<String> parameters = getArgs(joinPoint.getArgs());
        try {
            Object returnType = joinPoint.proceed();
            String messageAfterExecution = "Method: %s; Args: %s return %s";
            String logMessage = messageAfterExecution.formatted(
                    methodName,
                    String.join(", ", parameters),
                    !Objects.isNull(returnType) ? returnType.toString() : "void"
            );
            logger.log(getLogLevel(auditLog.logLevel()), logMessage);
        } catch (Throwable e) {
            String messageAfterThrowing = "Method: %s; Args: %s throw %s";
            String logMessage = messageAfterThrowing.formatted(
                    methodName,
                    String.join(", ", parameters),
                    e.getClass().getName()
            );
            logger.log(getLogLevel(auditLog.logLevel()), logMessage);
            throw e;
        }
    }

    private List<String> getArgs(Object[] args) {
        return Arrays.stream(args)
                .map(arg -> !Objects.isNull(arg) ? arg.toString() : "null")
                .toList();
    }

    private Level getLogLevel(LogLevel logLevel) {
        return Level.getLevel(logLevel.name());
    }

}
