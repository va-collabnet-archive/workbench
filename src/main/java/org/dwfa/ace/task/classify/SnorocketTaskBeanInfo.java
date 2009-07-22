package org.dwfa.ace.task.classify;

import java.beans.BeanDescriptor;
import java.beans.SimpleBeanInfo;

public class SnorocketTaskBeanInfo extends SimpleBeanInfo {
	/**
	 * @see java.beans.BeanInfo#getBeanDescriptor()
	 */
	public BeanDescriptor getBeanDescriptor() {
		String s = new String("<html><font color='#0087FF'>");
		s = s.concat("<center>Classifer (Current)");
		BeanDescriptor bd = new BeanDescriptor(SnorocketTask.class);
		bd.setDisplayName(s);
		return bd;
	}

}
