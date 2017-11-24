package model;

public class ApiException extends Exception {
    public ApiException(String message, Exception cause) {
        super(message, cause);
    }

    public ApiException(String message) {
        super(message);
    }
}
