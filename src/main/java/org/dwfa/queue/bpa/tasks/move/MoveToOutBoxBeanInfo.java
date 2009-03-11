/*
 * Created on Mar 24, 2005
 *
 * Copyright 2005 by Informatics, Inc. 
 */
package org.dwfa.queue.bpa.tasks.move;

import java.beans.BeanDescriptor;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

/**
 * @author kec
 *
 */
public class MoveToOutBoxBeanInfo extends SimpleBeanInfo {

	/**
	 * 
	 */
	public MoveToOutBoxBeanInfo() {
		super();
	}
    public PropertyDescriptor[] getPropertyDescriptors() {
        return new PropertyDescriptor[0];
    }        
    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(MoveToOutBox.class);
        bd.setDisplayName("<html><font color='green'>Move To Out Box");
        return bd;
    }

}
