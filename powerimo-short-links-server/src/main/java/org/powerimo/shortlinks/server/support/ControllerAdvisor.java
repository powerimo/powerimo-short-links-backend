package org.powerimo.shortlinks.server.support;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.powerimo.shortlinks.server.exceptions.InvalidArgument;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
@RequiredArgsConstructor
@Slf4j
public class ControllerAdvisor {

    @ExceptionHandler(InvalidArgument.class)
    public ResponseEntity<?> handleInvalidArgument(InvalidArgument ex) {
        log.error("Exception caught", ex);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }
}
