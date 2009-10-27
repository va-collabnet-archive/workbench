package org.dwfa.tapi;

/**
 * Indicates a path does not exist or a concept is not an actual path.
 * 
 * It does not indicate whether a concept meant to represent a path exists
 * (this would be a {@link NoMappingException})
 * 
 */
public class PathNotExistsException extends TerminologyException {

    private static final long serialVersionUID = 1L;

    public PathNotExistsException() {
	}

	public PathNotExistsException(String message) {
		super(message);
	}

	public PathNotExistsException(Throwable cause) {
		super(cause);
	}

	public PathNotExistsException(String message, Throwable cause) {
		super(message, cause);
	}

}
