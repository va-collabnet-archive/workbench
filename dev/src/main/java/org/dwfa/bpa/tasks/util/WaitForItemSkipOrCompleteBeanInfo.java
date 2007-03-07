/*
 * Created on Mar 24, 2005
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
public class WaitForItemSkipOrCompleteBeanInfo extends SimpleBeanInfo {

	/**
	 * 
	 */
	public WaitForItemSkipOrCompleteBeanInfo() {
		super();
	}
    public PropertyDescriptor[] getPropertyDescriptors() {
        return new PropertyDescriptor[0];
    }        
    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(WaitForItemSkipOrComplete.class);
        bd.setDisplayName("<html><font color='red'><center>Wait for User Input<br>Instruction Panel<br>Skip or Complete");
        return bd;
    }

}
