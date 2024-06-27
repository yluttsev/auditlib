package ru.luttsev.springbootstarterauditlib;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.test.appender.ListAppender;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.AutoConfigureWebClient;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import ru.luttsev.springbootstarterauditlib.advice.HttpRequestLoggingAdvice;
import ru.luttsev.springbootstarterauditlib.advice.HttpResponseLoggingAdvice;
import ru.luttsev.springbootstarterauditlib.annotation.WebAuditLog;

import java.util.List;

@ExtendWith(SpringExtension.class)
@WebMvcTest(WebAuditLogTests.TestController.class)
@ContextConfiguration(classes = {WebAuditLogTests.TestController.class,
        HttpRequestLoggingAdvice.class,
        HttpResponseLoggingAdvice.class})
@AutoConfigureMockMvc
@AutoConfigureWebClient
public class WebAuditLogTests {

    @Autowired
    private MockMvc mockMvc;

    private final ListAppender listAppender = new ListAppender("ListAppender");

    @BeforeEach
    public void setListAppender() {
        ((Logger) LogManager.getLogger(HttpRequestLoggingAdvice.class)).addAppender(this.listAppender);
        ((Logger) LogManager.getLogger(HttpResponseLoggingAdvice.class)).addAppender(this.listAppender);
        this.listAppender.start();
    }

    @AfterEach
    public void removeListAppender() {
        this.listAppender.stop();
        this.listAppender.clear();
        ((Logger) LogManager.getLogger(HttpRequestLoggingAdvice.class)).removeAppender(this.listAppender);
        ((Logger) LogManager.getLogger(HttpResponseLoggingAdvice.class)).removeAppender(this.listAppender);
    }

    @Test
    public void testHttpGetRequestLogging() throws Exception {
        this.mockMvc.perform(MockMvcRequestBuilders.get("/get"));

        List<String> logMessages = this.listAppender.getEvents().stream()
                .map(event -> event.getMessage().getFormattedMessage())
                .toList();

        Assertions.assertEquals(logMessages.get(0), "Request method: GET; Response status: 200; Response body: \"ok\"");
    }

    @Test
    public void testHttpPostRequestLogging() throws Exception {
        String body = "{\"key\": \"value\"}";
        this.mockMvc.perform(MockMvcRequestBuilders.post("/post")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body));

        List<String> logMessages = this.listAppender.getEvents().stream()
                .map(event -> event.getMessage().getFormattedMessage())
                .toList();

        Assertions.assertAll(
                () -> logMessages.get(0).equals("Request body: %s".formatted(body)),
                () -> logMessages.get(1).equals("Request method: POST; Response status: 200; Response body: \"ok\"")
        );
    }

    @RestController
    public static class TestController {

        @GetMapping("/get")
        @WebAuditLog(logLevel = LogLevel.INFO)
        public String get() {
            return "ok";
        }

        @PostMapping("/post")
        @WebAuditLog(logLevel = LogLevel.INFO)
        public String post(@RequestBody String body) {
            return "ok";
        }

    }

}
