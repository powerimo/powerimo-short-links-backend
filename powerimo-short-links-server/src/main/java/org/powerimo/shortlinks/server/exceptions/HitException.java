package org.powerimo.shortlinks.server.exceptions;

public class HitException extends RuntimeException {
    public HitException() {
        super();
    }

    public HitException(Throwable cause) {
        super(cause);
    }

    public HitException(String message) {
        super(message);
    }

    public HitException(String message, Throwable cause) {
        super(message, cause);
    }
}
