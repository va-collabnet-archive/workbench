package org.dwfa.ace.api.cs;


/**
 * Validates a component. Warns if the component parts are different but not part of the change,
 * fails if parts are different that are part of the change.
 * 
 * @author Dion McMurtrie
 * 
 */
public class LenientComponentValidator extends ComponentValidator {

	public LenientComponentValidator() {
		super();
		super.setStrictMode(false);
		super.setTimeLenient(true);
	}
}
