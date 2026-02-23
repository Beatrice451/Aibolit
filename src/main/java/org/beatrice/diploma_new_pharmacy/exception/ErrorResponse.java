package org.beatrice.diploma_new_pharmacy.exception;

import java.time.LocalDateTime;

public class ErrorResponse {
    private final int status;
    private final String message;
    private final String path;
    private final LocalDateTime timestamp = LocalDateTime.now();

    public ErrorResponse(int status, String message, String path) {
        this.status = status;
        this.message = message;
        this.path = path;
    }

    public int getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public String getPath() {
        return path;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }
}
