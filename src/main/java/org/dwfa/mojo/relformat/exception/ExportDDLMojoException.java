package org.dwfa.mojo.relformat.exception;

public class ExportDDLMojoException extends RuntimeException {

    private static final long serialVersionUID = 6189873267265257275L;

    public ExportDDLMojoException(final Throwable cause) {
        super(cause);
    }

    public ExportDDLMojoException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
