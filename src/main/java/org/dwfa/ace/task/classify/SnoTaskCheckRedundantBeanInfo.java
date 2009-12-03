package org.dwfa.ace.task.classify;

import java.beans.BeanDescriptor;
import java.beans.SimpleBeanInfo;

public class SnoTaskCheckRedundantBeanInfo extends SimpleBeanInfo{

	/**
	 * @see java.beans.BeanInfo#getBeanDescriptor()
	 */
	public BeanDescriptor getBeanDescriptor() {
		String s = new String("<html><font color='#0087FF'>");
		s = s.concat("<center>Check Redundant");
		BeanDescriptor bd = new BeanDescriptor(SnoTaskCheckRedundant.class);
		bd.setDisplayName(s);
		return bd;
	}

}
