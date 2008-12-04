package org.dwfa.mojo.memrefset.mojo;

public final class RefSetNameRetrieverException extends RuntimeException {

    private static final long serialVersionUID = 1644919908838003921L;

    public RefSetNameRetrieverException(final String message) {
        super(message);
    }

    public RefSetNameRetrieverException(final Throwable cause) {
        super(cause);
    }


    public RefSetNameRetrieverException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
