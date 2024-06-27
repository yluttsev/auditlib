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
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdvice;
import ru.luttsev.springbootstarterauditlib.LogLevel;
import ru.luttsev.springbootstarterauditlib.annotation.WebAuditLog;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Objects;

@ControllerAdvice
public class HttpRequestLoggingAdvice implements RequestBodyAdvice {

    private final Logger logger = LogManager.getLogger(this.getClass());

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

    @Override
    public HttpInputMessage beforeBodyRead(@NonNull HttpInputMessage inputMessage,
                                           @NonNull MethodParameter parameter,
                                           @NonNull Type targetType,
                                           @NonNull Class<? extends HttpMessageConverter<?>> converterType) {
        return inputMessage;
    }

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
