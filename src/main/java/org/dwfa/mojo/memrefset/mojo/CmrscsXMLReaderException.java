package org.dwfa.mojo.memrefset.mojo;

public final class CmrscsXMLReaderException extends RuntimeException {

    private static final long serialVersionUID = 4748701883339267923L;

    public CmrscsXMLReaderException(final String message) {
        super(message);
    }

    public CmrscsXMLReaderException(final Throwable cause) {
        super(cause);
    }


    public CmrscsXMLReaderException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
