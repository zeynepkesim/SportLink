error id: file:///C:/Users/ushen/Desktop/UT/firstYear/ESI/ESI_project_2026/sportlink-project/booking-service/src/main/java/com/sportlink/booking/exception/GlobalExceptionHandler.java:_empty_/BookingNotFoundException#getMessage#
file:///C:/Users/ushen/Desktop/UT/firstYear/ESI/ESI_project_2026/sportlink-project/booking-service/src/main/java/com/sportlink/booking/exception/GlobalExceptionHandler.java
empty definition using pc, found symbol in pc: _empty_/BookingNotFoundException#getMessage#
empty definition using semanticdb
empty definition using fallback
non-local guesses:

offset: 608
uri: file:///C:/Users/ushen/Desktop/UT/firstYear/ESI/ESI_project_2026/sportlink-project/booking-service/src/main/java/com/sportlink/booking/exception/GlobalExceptionHandler.java
text:
```scala
package com.sportlink.booking.exception;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BookingNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleNotFound(BookingNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("message", ex.getMes@@sage()));
    }

    @ExceptionHandler(InvalidBookingStateException.class)
    public ResponseEntity<Map<String, String>> handleInvalidState(InvalidBookingStateException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("message", ex.getMessage()));
    }

    @ExceptionHandler(SchedulingServiceException.class)
    public ResponseEntity<Map<String, String>> handleScheduling(SchedulingServiceException ex) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of("message", ex.getMessage()));
    }

    @ExceptionHandler(SearchServiceException.class)
    public ResponseEntity<Map<String, String>> handleSearch(SearchServiceException ex) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of("message", ex.getMessage()));
    }
}

```


#### Short summary: 

empty definition using pc, found symbol in pc: _empty_/BookingNotFoundException#getMessage#