package org.dwfa.ace.task;

import java.beans.BeanDescriptor;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

public class CommitBeanInfo extends SimpleBeanInfo {

	public PropertyDescriptor[] getPropertyDescriptors() {
		PropertyDescriptor rv[] = {};
		return rv;
	}

	/**
	 * @see java.beans.BeanInfo#getBeanDescriptor()
	 */
	public BeanDescriptor getBeanDescriptor() {
		BeanDescriptor bd = new BeanDescriptor(Commit.class);
		bd.setDisplayName("<html><font color='green'><center>Commit Changes");
		return bd;
	}
}