package org.dwfa.tapi;

public class UnknownComponentException extends TerminologyException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public UnknownComponentException() {
	}

	public UnknownComponentException(String message) {
		super(message);
	}

	public UnknownComponentException(Throwable cause) {
		super(cause);
	}

	public UnknownComponentException(String message, Throwable cause) {
		super(message, cause);
	}

}
