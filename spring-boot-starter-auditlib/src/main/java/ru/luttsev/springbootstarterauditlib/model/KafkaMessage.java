package ru.luttsev.springbootstarterauditlib.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class KafkaMessage {

    private String serviceName;

    private String message;

}
