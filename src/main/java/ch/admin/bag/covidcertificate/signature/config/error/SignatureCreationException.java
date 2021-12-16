package ch.admin.bag.covidcertificate.signature.config.error;

public class SignatureCreationException extends RuntimeException{
    public SignatureCreationException(String message) {
        super(message);
    }
    public SignatureCreationException(String message, Throwable cause) {
        super(message, cause);
    }
}
