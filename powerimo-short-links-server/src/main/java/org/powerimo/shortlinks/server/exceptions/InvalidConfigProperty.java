package org.powerimo.shortlinks.server.exceptions;

public class InvalidConfigProperty extends Exception {

    public InvalidConfigProperty(String message) {
        super(message);
    }

    public InvalidConfigProperty(String message, Throwable cause) {
        super(message, cause);
    }
}
