package org.dwfa.ace.task.refset.migrate;

import java.beans.BeanDescriptor;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

public class MigrateAllSpecRefsetsBeanInfo extends SimpleBeanInfo {

	public PropertyDescriptor[] getPropertyDescriptors() {
		PropertyDescriptor rv[] = {};
		return rv;
	}

	/**
	 * @see java.beans.BeanInfo#getBeanDescriptor()
	 */
	public BeanDescriptor getBeanDescriptor() {
		BeanDescriptor bd = new BeanDescriptor(MigrateAllSpecRefsets.class);
		bd.setDisplayName("<html><font color='green'><center>Migrate all<br>specification refsets");
		return bd;
	}
	
}
