package org.ihtsdo.rules.tasks;

import java.beans.BeanDescriptor;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

public class LaunchQAManagerBeanInfo extends SimpleBeanInfo {

	/**
	 * 
	 */
	public LaunchQAManagerBeanInfo() {
		super();
	}
	public PropertyDescriptor[] getPropertyDescriptors() {
		PropertyDescriptor rv[] =
		{};
		return rv;

	}        
	/**
	 * @see java.beans.BeanInfo#getBeanDescriptor()
	 */
	public BeanDescriptor getBeanDescriptor() {
		BeanDescriptor bd = new BeanDescriptor(LaunchQAManager.class);
		bd.setDisplayName("<html><font color='green'><center>Launch QA Manager");
		return bd;
	}

}
