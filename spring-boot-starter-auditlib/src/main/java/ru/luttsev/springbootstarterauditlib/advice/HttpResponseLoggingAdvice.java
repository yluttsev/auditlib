package ru.luttsev.springbootstarterauditlib.advice;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;
import ru.luttsev.springbootstarterauditlib.LogLevel;
import ru.luttsev.springbootstarterauditlib.annotation.WebAuditLog;

import java.lang.annotation.Annotation;
import java.util.Objects;

@ControllerAdvice
public class HttpResponseLoggingAdvice implements ResponseBodyAdvice<Object> {

    private final Logger logger = LogManager.getLogger(this.getClass());

    @Override
    public boolean supports(@NonNull MethodParameter returnType,
                            @NonNull Class<? extends HttpMessageConverter<?>> converterType) {
        for (Annotation annotation : returnType.getMethodAnnotations()) {
            if (Objects.equals(annotation.annotationType(), WebAuditLog.class)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Object beforeBodyWrite(@Nullable Object body,
                                  @NonNull MethodParameter returnType,
                                  @NonNull MediaType selectedContentType,
                                  @NonNull Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                  @NonNull ServerHttpRequest request,
                                  @NonNull ServerHttpResponse response) {
        ObjectMapper mapper = new ObjectMapper();
        int statusCode = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
                .getResponse()
                .getStatus();
        Level logLevel = LogLevel.toLog4j2Level(
                returnType.getMethodAnnotation(WebAuditLog.class).logLevel()
        );
        try {
            logger.log(logLevel, "Request method: %s; Response status: %d; Response body: %s"
                    .formatted(request.getMethod().name(),
                            statusCode,
                            Objects.isNull(body) ? "[empty]" : mapper.writeValueAsString(body)));
        } catch (JsonProcessingException e) {
            logger.log(logLevel, "Error in processing the body: %s.".formatted(e.getMessage()));
        }
        return body;
    }

}
