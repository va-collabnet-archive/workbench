/*
 * Created on Mar 23, 2005
 *
 * Copyright 2005 by Informatics, Inc. 
 */
package org.dwfa.bpa.tasks.util;

import java.beans.BeanDescriptor;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

/**
 * @author kec
 *
 */
public class ChangeProcessInstanceIdBeanInfo extends SimpleBeanInfo {

	/**
	 * 
	 */
	public ChangeProcessInstanceIdBeanInfo() {
		super();
	}
    public PropertyDescriptor[] getPropertyDescriptors() {
        return new PropertyDescriptor[0];
    }        
    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(ChangeProcessInstanceId.class);
        bd.setDisplayName("<html><center>Change Instance Id");
        return bd;
    }

}
