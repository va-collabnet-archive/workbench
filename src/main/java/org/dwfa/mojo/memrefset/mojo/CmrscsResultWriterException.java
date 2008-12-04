package org.dwfa.mojo.memrefset.mojo;

public final class CmrscsResultWriterException extends RuntimeException {

    private static final long serialVersionUID = 990388013576161689L;

    public CmrscsResultWriterException(final String message) {
        super(message);
    }

    public CmrscsResultWriterException(final Throwable cause) {
        super(cause);
    }


    public CmrscsResultWriterException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
