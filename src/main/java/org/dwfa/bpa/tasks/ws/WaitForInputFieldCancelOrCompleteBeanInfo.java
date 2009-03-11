/*
 * Created on Apr 19, 2005
 *
 * Copyright 2005 by Informatics, Inc. 
 */
package org.dwfa.bpa.tasks.ws;

import java.beans.BeanDescriptor;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

/**
 * @author kec
 *
 */
public class WaitForInputFieldCancelOrCompleteBeanInfo extends SimpleBeanInfo {

    /**
     * 
     */
    public WaitForInputFieldCancelOrCompleteBeanInfo() {
        super();
    }
    public PropertyDescriptor[] getPropertyDescriptors() {
        return new PropertyDescriptor[0];
    }        
    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(WaitForInputFieldCancelOrComplete.class);
        bd.setDisplayName("<html><font color='red'><center>Wait for User Input<br>Field Input Panel<br>Cancel or Complete");
        return bd;
    }

}
