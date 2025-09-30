package com.pokemon.api.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Response {

    private Object data;
    private String responseMessage;
    private String responseCode;
    private HttpStatus httpStatus;

    public static Response success(Object data, String responseMessage) {
        return Response.builder()
                .data(data)
                .responseMessage(responseMessage)
                .responseCode("200")
                .httpStatus(HttpStatus.OK)
                .build();
    }

    public static Response error(String responseMessage) {
        return Response.builder()
                .data(null)
                .responseMessage(responseMessage)
                .responseCode("500")
                .httpStatus(HttpStatus.INTERNAL_SERVER_ERROR)
                .build();
    }
}
