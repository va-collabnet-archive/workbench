package org.dwfa.maven.derby;

public final class BuildMarkerException extends RuntimeException {

    private static final long serialVersionUID = -5592200350185896499L;

    public BuildMarkerException(final String message) {
        super(message);
    }

    public BuildMarkerException(final Throwable cause) {
        super(cause);
    }

    public BuildMarkerException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
