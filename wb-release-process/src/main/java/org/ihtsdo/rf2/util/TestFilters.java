package org.ihtsdo.rf2.util;

import java.util.ArrayList;

import org.dwfa.ace.api.I_AmPart;

public class TestFilters {

	private ArrayList<I_amFilter> filters;
	public boolean testAll(I_AmPart part){
		
		for (I_amFilter filter : filters){
			if (!filter.test(part)){
				return false;
			}
		}
		return true;
	}
	
	public void addFilter(I_amFilter filter){
		if (filters==null){
			filters=new ArrayList<I_amFilter>();
		}
		filters.add(filter);
	}
}
