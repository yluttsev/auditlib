package ru.luttsev.springbootstarterauditlib.appender;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.impl.Log4jLogEvent;
import org.apache.logging.log4j.message.SimpleMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import ru.luttsev.springbootstarterauditlib.config.AuditLibAutoConfiguration;
import ru.luttsev.springbootstarterauditlib.model.KafkaMessage;
import ru.luttsev.springbootstarterauditlib.util.SpringContextRestarter;
import ru.luttsev.springbootstarterauditlib.util.SpringContextRestarterExtension;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringJUnitConfig
@Import(AuditLibAutoConfiguration.class)
@ExtendWith(SpringContextRestarterExtension.class)
@Testcontainers
class ExactlyOnceAppenderTests {

    private static final String TOPIC_NAME = "test-topic";

    private static KafkaContainer kafkaContainer = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka"));

    private KafkaConsumer<String, KafkaMessage> consumer;

    @Autowired
    private KafkaAppender kafkaAppender;

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry properties) {
        properties.add("kafka.topic.name", () -> TOPIC_NAME);
        properties.add("spring.kafka.bootstrap-servers", kafkaContainer::getBootstrapServers);
    }

    static {
        kafkaContainer.start();
    }

    @BeforeEach
    void setupConsumer() {
        consumer = new KafkaConsumer<>(
                Map.of(
                        ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaContainer.getBootstrapServers(),
                        ConsumerConfig.GROUP_ID_CONFIG, "test-group",
                        ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest",
                        ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class,
                        ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class
                )
        );
        consumer.subscribe(Collections.singletonList(TOPIC_NAME));
    }

    @Test
    void testProducerSaveMessageWithPauseKafkaBroker() {
        String message = "Test message";
        kafkaAppender.start();
        consumer.subscribe(Collections.singletonList(TOPIC_NAME));
        ConsumerRecords<String, KafkaMessage> messages = consumer.poll(Duration.of(10, ChronoUnit.SECONDS));
        assertTrue(messages.isEmpty());
        SpringContextRestarter.getInstance().restart(() -> kafkaAppender.append(Log4jLogEvent.newBuilder()
                .setMessage(new SimpleMessage(message))
                .setLevel(Level.INFO)
                .build()));
        await().atMost(15, TimeUnit.SECONDS).untilAsserted(() -> {
            ConsumerRecords<String, KafkaMessage> newMessages = consumer.poll(Duration.of(10, ChronoUnit.SECONDS));
            for (ConsumerRecord<String, KafkaMessage> record : newMessages) {
                assertEquals(message, record.value().getMessage());
            }
        });
    }

}
