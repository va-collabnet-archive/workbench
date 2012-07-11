package org.ihtsdo.tk.spec;

import java.io.IOException;

public class ValidationException extends IOException {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public ValidationException() {
    }

    public ValidationException(String message) {
        super(message);
    }

    public ValidationException(Throwable throwable) {
        super(throwable);
    }

    public ValidationException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
