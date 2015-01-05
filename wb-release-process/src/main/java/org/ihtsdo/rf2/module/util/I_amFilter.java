package org.ihtsdo.rf2.module.util;

import java.util.List;

import org.dwfa.ace.api.I_AmPart;

// TODO: Auto-generated Javadoc
/**
 * The Interface I_amFilter.
 */
public interface I_amFilter {

	/**
	 * Test.
	 *
	 * @param part the part
	 * @return true, if successful
	 */
	public boolean test(I_AmPart part);
	
	/**
	 * Sets the values to match.
	 *
	 * @param objects the new values to match
	 */
	public void setValuesToMatch(List<Object> objects);
}
