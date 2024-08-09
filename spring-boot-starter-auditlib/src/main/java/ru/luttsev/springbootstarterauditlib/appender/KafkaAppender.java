package ru.luttsev.springbootstarterauditlib.appender;

import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Property;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.springframework.kafka.core.KafkaTemplate;

import java.io.Serializable;

/**
 * Аппендер для отправки логов в кафку
 *
 * @author Yuri Luttsev
 */
@Plugin(name = "KafkaAppender", category = "Core", elementType = "appender", printObject = true)
public class KafkaAppender extends AbstractAppender {

    private final KafkaTemplate<String, String> kafkaTemplate;

    private final String topicName;

    public KafkaAppender(String name,
                         Filter filter,
                         Layout<? extends Serializable> layout,
                         boolean ignoreExceptions,
                         Property[] properties,
                         KafkaTemplate<String, String> kafkaTemplate,
                         String topicName) {
        super(name, filter, layout, ignoreExceptions, properties);
        this.kafkaTemplate = kafkaTemplate;
        this.topicName = topicName;
    }

    @Override
    public void append(LogEvent event) {
        kafkaTemplate.executeInTransaction(operations -> operations.send(topicName, event.getMessage().getFormattedMessage()));
    }

}
