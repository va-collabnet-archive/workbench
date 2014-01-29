package org.ihtsdo.rf2.module.util;

import java.util.ArrayList;

import org.dwfa.ace.api.I_AmPart;

// TODO: Auto-generated Javadoc
/**
 * The Class TestFilters.
 */
public class TestFilters {

	/** The filters. */
	private ArrayList<I_amFilter> filters;
	
	/**
	 * Test all.
	 *
	 * @param part the part
	 * @return true, if successful
	 */
	public boolean testAll(I_AmPart part){
		
		for (I_amFilter filter : filters){
			if (!filter.test(part)){
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Adds the filter.
	 *
	 * @param filter the filter
	 */
	public void addFilter(I_amFilter filter){
		if (filters==null){
			filters=new ArrayList<I_amFilter>();
		}
		filters.add(filter);
	}
}
