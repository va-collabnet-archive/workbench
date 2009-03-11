package org.dwfa.tapi;

public class NoMappingException extends TerminologyException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public NoMappingException() {
	}

	public NoMappingException(String message) {
		super(message);
	}

	public NoMappingException(Throwable cause) {
		super(cause);
	}

	public NoMappingException(String message, Throwable cause) {
		super(message, cause);
	}

}
