package ru.luttsev.springbootstarterauditlib.advice;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdvice;
import ru.luttsev.springbootstarterauditlib.LogLevel;
import ru.luttsev.springbootstarterauditlib.annotation.WebAuditLog;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Objects;

/**
 * Advice для перехвата HTTP запросов
 * @author Yuri Luttsev
 */
@RestControllerAdvice
public class HttpRequestLoggingAdvice implements RequestBodyAdvice {

    private final Logger logger = LogManager.getLogger(this.getClass());

    /**
     * Проверка на совместимость перехватчика
     * @param methodParameter параметр метода
     * @param targetType тип для преобразования тела запроса
     * @param converterType тип конвертера, преобразующего тело запроса
     * @return поддержка метода
     */
    @Override
    public boolean supports(@NonNull MethodParameter methodParameter,
                            @NonNull Type targetType,
                            @NonNull Class<? extends HttpMessageConverter<?>> converterType) {
        for (Annotation annotation : methodParameter.getMethodAnnotations()) {
            if (Objects.equals(annotation.annotationType(), WebAuditLog.class)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Обработка тела запроса до прочтения
     * @param inputMessage http сообщение
     * @param parameter параметр метода
     * @param targetType тип для преобразования тела запроса
     * @param converterType тип конвертера, преобразующего тело запроса
     * @return http сообщение
     */
    @Override
    public HttpInputMessage beforeBodyRead(@NonNull HttpInputMessage inputMessage,
                                           @NonNull MethodParameter parameter,
                                           @NonNull Type targetType,
                                           @NonNull Class<? extends HttpMessageConverter<?>> converterType) {
        return inputMessage;
    }

    /**
     * Обработка тела запроса после прочтения
     * @param body тело запроса
     * @param inputMessage http сообщение
     * @param parameter параметр метода
     * @param targetType тип для преобразования тела запроса
     * @param converterType тип конвертера, преобразующего тело запроса
     * @return тело запроса
     */
    @Override
    public Object afterBodyRead(@Nullable Object body,
                                @NonNull HttpInputMessage inputMessage,
                                @NonNull MethodParameter parameter,
                                @NonNull Type targetType,
                                @NonNull Class<? extends HttpMessageConverter<?>> converterType) {
        ObjectMapper mapper = new ObjectMapper();
        Level logLevel = LogLevel.toLog4j2Level(
                parameter.getMethodAnnotation(WebAuditLog.class).logLevel()
        );
        try {
            logger.log(logLevel,
                    "Request body: %s".formatted(Objects.isNull(body) ? "[empty]" : mapper.writeValueAsString(body)));
        } catch (JsonProcessingException e) {
            logger.log(logLevel, "Error in processing the body: %s.".formatted(e.getMessage()));
        }
        return body;
    }

    /**
     * Обработка пустого тела запроса
     * @param body тело запроса
     * @param inputMessage http сообщение
     * @param parameter параметр метода
     * @param targetType тип для преобразования тела запроса
     * @param converterType тип конвертера, преобразующего тело запроса
     * @return тело запроса
     */
    @Override
    public Object handleEmptyBody(@Nullable Object body,
                                  @NonNull HttpInputMessage inputMessage,
                                  @NonNull MethodParameter parameter,
                                  @NonNull Type targetType,
                                  @NonNull Class<? extends HttpMessageConverter<?>> converterType) {
        Level logLevel = LogLevel.toLog4j2Level(
                parameter.getParameterAnnotation(WebAuditLog.class).logLevel()
        );
        logger.log(logLevel, "The method does not expect an empty body.");
        return body;
    }

}
