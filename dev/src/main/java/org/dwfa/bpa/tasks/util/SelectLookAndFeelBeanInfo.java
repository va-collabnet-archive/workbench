/*
 * Created on Apr 2, 2005
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
public class SelectLookAndFeelBeanInfo extends SimpleBeanInfo {

    /**
     * 
     */
    public SelectLookAndFeelBeanInfo() {
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
        bd.setDisplayName("<html><center>Select Look and Feel");
        return bd;
    }

}
