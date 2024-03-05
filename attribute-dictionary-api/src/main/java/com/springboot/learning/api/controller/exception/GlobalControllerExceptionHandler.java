package com.springboot.learning.api.controller.exception;

import com.springboot.learning.api.controller.contract.v1.HttpErrorInfo;
import com.springboot.learning.service.exception.InvalidInputException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.ZonedDateTime;

@RestControllerAdvice
public class GlobalControllerExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalControllerExceptionHandler.class);

    @ResponseStatus(value = HttpStatus.UNPROCESSABLE_ENTITY)
    @ExceptionHandler(InvalidInputException.class)
    public HttpErrorInfo invalidInputException(ServerHttpRequest request, Exception ex) {
        return createHttpErrorInfo(HttpStatus.UNPROCESSABLE_ENTITY, request, ex);
    }

    /**
     * @param httpStatus : the http status
     * @param request : the request
     * @param ex : the exception
     * @return {@link HttpErrorInfo}
     */
    private HttpErrorInfo createHttpErrorInfo(HttpStatus httpStatus, ServerHttpRequest request, Exception ex) {

        final String path = request.getPath().pathWithinApplication().value();
        final String message = ex.getMessage();

        log.debug("Returning HTTP status: {} for path: {}, message: {}", httpStatus, path, message);

        return new HttpErrorInfo(path, httpStatus, message, ZonedDateTime.now().toLocalDateTime());
    }
}
