package org.ihtsdo.rf2.util;

import org.dwfa.ace.api.I_AmPart;

public interface I_amFilter {

	public boolean test(I_AmPart part);
}
