package org.dwfa.tapi;

public class TerminologyRuntimeException extends RuntimeException {

	private static final long serialVersionUID = 3636392795657583995L;

	public TerminologyRuntimeException() {
	}

	public TerminologyRuntimeException(String message) {
		super(message);
	}

	public TerminologyRuntimeException(Throwable cause) {
		super(cause);
	}

	public TerminologyRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}

}
