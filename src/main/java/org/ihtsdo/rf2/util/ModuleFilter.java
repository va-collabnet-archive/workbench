package org.ihtsdo.rf2.util;

import org.dwfa.ace.api.I_AmPart;

public class ModuleFilter implements I_amFilter {

	
	@Override
	public boolean test(I_AmPart part) {
		String mod=ExportUtil.getModuleSCTIDForStampNid(part.getModuleNid());
		return (mod==null? false:true);
	}

}
