package org.dwfa.mojo.memrefset.mojo;

public final class CmrscsReaderException extends RuntimeException {

    private static final long serialVersionUID = -4658216582281240212L;

    public CmrscsReaderException(final String message) {
        super(message);
    }

    public CmrscsReaderException(final Throwable cause) {
        super(cause);
    }


    public CmrscsReaderException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
