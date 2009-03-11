/*
 * Created on Apr 19, 2005
 *
 * Copyright 2005 by Informatics, Inc. 
 */
package org.dwfa.bpa.tasks.pwdbreaker;

import java.beans.BeanDescriptor;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

/**
 * @author kec
 *
 */
public class BreakPasswordBeanInfo extends SimpleBeanInfo {

    /**
     * 
     */
    public BreakPasswordBeanInfo() {
        super();
    }
    public PropertyDescriptor[] getPropertyDescriptors() {
        return new PropertyDescriptor[0];
    }        
    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(BreakPassword.class);
        bd.setDisplayName("<html><font color='blue'><center>Break Password");
        return bd;
    }

}
