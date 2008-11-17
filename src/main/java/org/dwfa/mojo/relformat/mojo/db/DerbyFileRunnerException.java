package org.dwfa.mojo.relformat.mojo.db;

public final class DerbyFileRunnerException extends RuntimeException {

    private static final long serialVersionUID = -2176141340061216802L;

    public DerbyFileRunnerException(final String message) {
        super(message);
    }

    public DerbyFileRunnerException(final Throwable cause) {
        super(cause);
    }


    public DerbyFileRunnerException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
