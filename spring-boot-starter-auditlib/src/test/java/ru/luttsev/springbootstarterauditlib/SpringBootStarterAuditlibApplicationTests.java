package ru.luttsev.springbootstarterauditlib;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import ru.luttsev.springbootstarterauditlib.config.AuditLibAutoConfiguration;

@SpringBootTest(
        classes = {AuditLibAutoConfiguration.class},
        properties = {
                "auditlib.appender=console",
                "auditlib.level=INFO"
        })
class SpringBootStarterAuditlibApplicationTests {

    @Test
    void contextLoads() {
    }

}
