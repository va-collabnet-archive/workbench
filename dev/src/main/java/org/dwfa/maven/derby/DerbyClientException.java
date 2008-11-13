package org.dwfa.maven.derby;

public final class DerbyClientException extends RuntimeException {

    private static final long serialVersionUID = 556673711063676638L;

    public DerbyClientException(final Throwable cause) {
        super(cause);
    }

    public DerbyClientException(final String message) {
        super(message);
    }
}
