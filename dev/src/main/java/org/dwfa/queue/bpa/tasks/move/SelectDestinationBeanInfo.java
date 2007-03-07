/*
 * Created on Jun 1, 2005
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
public class SelectDestinationBeanInfo extends SimpleBeanInfo {

    /**
     * 
     */
    public SelectDestinationBeanInfo() {
        super();
        // TODO Auto-generated constructor stub
    }
    public PropertyDescriptor[] getPropertyDescriptors() {

         return new PropertyDescriptor[] {};
     }        
    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(SetDestination.class);
        bd.setDisplayName("<html><font color='green'><center>Select Destination");
        return bd;
    }

}
