/*
 * Created on Jan 18, 2006
 *
 * Copyright 2006 by Informatics, Inc. 
 */

package org.dwfa.queue.bpa.tasks.move;

import java.beans.BeanDescriptor;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

/**
 * @author kec
 *
 */
public class SelectDestinationRootProcessBeanInfo extends SimpleBeanInfo {

    /**
     * 
     */
    public SelectDestinationRootProcessBeanInfo() {
        super();
    }
    public PropertyDescriptor[] getPropertyDescriptors() {

         return new PropertyDescriptor[] {};
     }        
    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(SetDestination.class);
        bd.setDisplayName("<html><font color='green'><center>ROOT Process<br>Select Destination");
        return bd;
    }

}
