package org.ihtsdo.rf2.module.util;

import java.util.List;

import org.dwfa.ace.api.I_AmPart;

public interface I_amFilter {

	public boolean test(I_AmPart part);
	
	public void setValuesToMatch(List<Object> objects);
}
