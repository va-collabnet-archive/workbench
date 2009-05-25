package org.dwfa.ace.task.refset.members.export;

public final class RefsetExportValidationException extends RuntimeException {

    private static final long serialVersionUID = 8701512682878583267L;

    public RefsetExportValidationException(final String message) {
        super(message);
    }

    public RefsetExportValidationException(final Throwable cause) {
        super(cause);
    }


    public RefsetExportValidationException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
