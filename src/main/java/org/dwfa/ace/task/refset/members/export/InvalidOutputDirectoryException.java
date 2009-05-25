package org.dwfa.ace.task.refset.members.export;

public final class InvalidOutputDirectoryException extends RuntimeException {

    private static final long serialVersionUID = 6936075644115769716L;

    public InvalidOutputDirectoryException(final String message) {
        super(message);
    }

    public InvalidOutputDirectoryException(final Throwable cause) {
        super(cause);
    }


    public InvalidOutputDirectoryException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
