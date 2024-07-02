package ru.luttsev.springbootstarterauditlib;

import org.apache.logging.log4j.core.test.appender.ListAppender;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.luttsev.springbootstarterauditlib.annotation.AuditLog;
import ru.luttsev.springbootstarterauditlib.aspect.LoggingAspect;

import java.util.List;

@ExtendWith(MockitoExtension.class)
public class AuditLogTests {

    @Mock
    public ProceedingJoinPoint joinPoint;

    @Mock
    public Signature signature;

    @Mock
    public AuditLog auditLog;

    @InjectMocks
    public LoggingAspect loggingAspect;

    @BeforeEach
    public void mockAspect() {
        Mockito.when(joinPoint.getSignature()).thenReturn(signature);
        Mockito.when(auditLog.logLevel()).thenReturn(LogLevel.INFO);
        Mockito.when(signature.getName()).thenReturn("test");
        Mockito.when(joinPoint.getArgs()).thenReturn(new Object[]{"arg1", "arg2"});
    }

    @Test
    public void testMethodLoggingAfterExecute() throws Throwable {
        ListAppender listAppender = new ListAppender("ListAppender1");
        loggingAspect.addAppender(listAppender);
        Mockito.when(joinPoint.proceed()).thenReturn("result");

        listAppender.start();
        loggingAspect.logMethod(joinPoint, auditLog);

        Assertions.assertEquals(getLogMessages(listAppender).get(0),
                "Method name: test; Args: arg1, arg2; Return: result");
    }

    @Test
    public void testMethodLoggingAfterThrowing() throws Throwable {
        ListAppender listAppender = new ListAppender("ListAppender2");
        loggingAspect.addAppender(listAppender);
        Mockito.when(joinPoint.proceed()).thenThrow(IllegalArgumentException.class);

        listAppender.start();

        Assertions.assertAll(
                () -> Assertions.assertThrows(IllegalArgumentException.class,
                        () -> loggingAspect.logMethod(joinPoint, auditLog)),
                () -> Assertions.assertEquals(getLogMessages(listAppender).get(0),
                        "Method name: test; Args: arg1, arg2; Throw: java.lang.IllegalArgumentException")
        );
    }

    private List<String> getLogMessages(ListAppender listAppender) {
        return listAppender.getEvents().stream()
                .map(event -> event.getMessage().getFormattedMessage())
                .toList();
    }

}
