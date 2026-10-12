package com.fourspicehotpot.backend.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ApiResponseCommon<T> {

    HttpStatus status;
    String message;
    T data;
    boolean success;
    int code;

    public static <T> ApiResponseCommon<T> success(T data, HttpStatus status, String message) {
        return ApiResponseCommon.<T>builder()
                .success(true)
                .code(status.value())
                .data(data)
                .message(message)
                .status(status)
                .build();
    }
}
