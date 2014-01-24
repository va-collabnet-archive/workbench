package org.ihtsdo.rf2.module.util;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import org.dwfa.ace.api.I_AmPart;
import org.dwfa.ace.api.Terms;
import org.dwfa.tapi.TerminologyException;

public class ModuleFilter implements I_amFilter {

	HashSet<Integer> valuesToMatch;
	@Override
	public boolean test(I_AmPart part) {
		return  valuesToMatch.contains(part.getModuleNid());
	}

	@Override
	public void setValuesToMatch(List<Object> values) {
		valuesToMatch=new HashSet<Integer>();
		for(Object value:values){
			try {
				valuesToMatch.add((Integer) Terms.get().uuidToNative(UUID.fromString(value.toString())));
			} catch (TerminologyException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}
