package org.dwfa.ace.task.refset.members;

public final class PropertyNotFoundException extends RuntimeException {

    private static final long serialVersionUID = -2267599569812066741L;

    public PropertyNotFoundException(final String message) {
        super(message);
    }

    public PropertyNotFoundException(final Throwable cause) {
        super(cause);
    }


    public PropertyNotFoundException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
