package ru.luttsev.springbootstarterauditlib.appender;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Property;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import ru.luttsev.springbootstarterauditlib.model.KafkaMessage;

import java.io.Serializable;

/**
 * Аппендер для отправки логов в кафку
 *
 * @author Yuri Luttsev
 */
@Slf4j
@Plugin(name = "KafkaAppender", category = "Core", elementType = "appender", printObject = true)
public class KafkaAppender extends AbstractAppender {

    private final KafkaTemplate<String, String> kafkaTemplate;

    private final String topicName;

    private final String serviceName;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public KafkaAppender(String name,
                         Filter filter,
                         Layout<? extends Serializable> layout,
                         boolean ignoreExceptions,
                         Property[] properties,
                         KafkaTemplate<String, String> kafkaTemplate,
                         String topicName,
                         @Value("${spring.application.name}") String serviceName) {
        super(name, filter, layout, ignoreExceptions, properties);
        this.kafkaTemplate = kafkaTemplate;
        this.topicName = topicName;
        this.serviceName = serviceName;
    }

    @Override
    public void append(LogEvent event) {
        KafkaMessage message = KafkaMessage.builder()
                .serviceName(serviceName)
                .message(event.getMessage().getFormattedMessage())
                .build();
        kafkaTemplate.executeInTransaction(
                operations -> {
                    try {
                        return operations.send(
                                topicName, objectMapper.writeValueAsString(message)
                        );
                    } catch (JsonProcessingException e) {
                        log.error("JSON parse error.");
                        throw new RuntimeException(e);
                    }
                }
        );
    }

}
