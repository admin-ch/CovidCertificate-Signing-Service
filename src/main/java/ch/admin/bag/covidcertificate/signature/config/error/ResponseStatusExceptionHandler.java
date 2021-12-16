package ch.admin.bag.covidcertificate.signature.config.error;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.io.IOException;
import java.security.SignatureException;
import java.security.cert.CertificateEncodingException;

@ControllerAdvice
@Slf4j
public class ResponseStatusExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(value = {SecurityException.class, IOException.class, CertificateEncodingException.class})
    protected ResponseEntity<Object> handleInternalServerError(Exception ex) {
        log.warn("Internal server error", ex);
        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(value = {IllegalStateException.class})
    protected ResponseEntity<Object> handleServiceUnavailable(IllegalStateException ex) {
        log.warn("Illegal server state", ex);
        return new ResponseEntity<>(HttpStatus.SERVICE_UNAVAILABLE);
    }

    @ExceptionHandler(value = {SignatureException.class, SignatureCreationException.class})
    protected ResponseEntity<Object> handleBadRequest(Exception ex) {
        log.warn("Failed to create signature:", ex);
        return new ResponseEntity<>("Failed to create signature", HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(value = {CertificateNotFoundException.class })
    protected ResponseEntity<Object> handleCertificateNotFoundException(CertificateNotFoundException ex) {
        log.warn("Certificate not found:", ex);
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.NOT_FOUND);
    }
}
