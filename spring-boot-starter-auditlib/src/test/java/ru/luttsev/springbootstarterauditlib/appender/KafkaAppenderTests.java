package ru.luttsev.springbootstarterauditlib.appender;


import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.impl.Log4jLogEvent;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.apache.logging.log4j.message.SimpleMessageFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaOperations;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.util.ReflectionTestUtils;
import ru.luttsev.springbootstarterauditlib.model.KafkaMessage;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class KafkaAppenderTests {

    @Mock
    private KafkaTemplate<String, KafkaMessage> kafkaTemplate;

    @Mock
    private KafkaOperations<String, KafkaMessage> kafkaOperations;

    private final String topicName = "test";

    private String serviceName = "test-service";

    private KafkaAppender kafkaAppender;

    @BeforeEach
    void setup() {
        kafkaAppender = new KafkaAppender("KafkaAppender",
                null,
                PatternLayout.createDefaultLayout(),
                true,
                null,
                kafkaTemplate,
                topicName,
                serviceName);
    }

    @Test
    void testSendLogMessageInKafka() {
        KafkaMessage message = KafkaMessage.builder()
                .serviceName(serviceName)
                .message("test message")
                .build();
        ReflectionTestUtils.setField(kafkaAppender, "topicName", topicName);
        when(kafkaTemplate.executeInTransaction(any())).thenAnswer(mockResult -> {
            KafkaOperations.OperationsCallback<String, KafkaMessage, Object> operationsCallback = mockResult.getArgument(0);
            return operationsCallback.doInOperations(kafkaOperations);
        });

        kafkaAppender.append(Log4jLogEvent.newBuilder()
                .setMessage(SimpleMessageFactory.INSTANCE.newMessage(message.getMessage()))
                .setLevel(Level.INFO)
                .build());
        verify(kafkaOperations).send(topicName, message);
    }

}
