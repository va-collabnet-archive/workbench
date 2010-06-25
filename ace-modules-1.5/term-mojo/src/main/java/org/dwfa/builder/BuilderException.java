package org.dwfa.builder;

/**
 * {@code BuilderException} should be thrown by a {@link Builder} when there has
 * been an error building the Component specified by the {@link Builder} generic
 * type.
 * @author Matthew Edwards
 */
public class BuilderException extends Exception {

    /**
     * A universal version identifier for this Serializable class.
     * Deserialization uses this number to ensure that a loaded class
     * corresponds exactly to a serialized object.
     */
    static final long serialVersionUID = -3786871272527242530L;

    /**
     * Creates a new instance of <code>BuilderException</code> without detail
     * message. Default No Arguments Constructor.
     */
    public BuilderException() {
    }

    /**
     * Constructs an instance of <code>BuilderException</code> with the
     * specified detail message.
     * @param message the detail message.
     */
    public BuilderException(final String message) {
        super(message);
    }

    /**
     * Constructs an instance of {@code BuilderException} with the specified
     * {@link Throwable} cause.
     * @param cause The {@link Throwable} cause.
     */
    public BuilderException(final Throwable cause) {
        super(cause);
    }

    /**
     * Constructs an instance of <code>BuilderException</code> with the
     * specified detail message and the specified {@link Throwable} cause.
     * @param message the detail message.
     * @param cause The {@link Throwable} cause.
     */
    public BuilderException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
