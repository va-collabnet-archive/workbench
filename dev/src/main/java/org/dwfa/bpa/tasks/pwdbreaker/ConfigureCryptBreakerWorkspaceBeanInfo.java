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
public class ConfigureCryptBreakerWorkspaceBeanInfo extends SimpleBeanInfo {

    /**
     * 
     */
    public ConfigureCryptBreakerWorkspaceBeanInfo() {
        super();
    }
    public PropertyDescriptor[] getPropertyDescriptors() {
        return new PropertyDescriptor[0];
    }        
    
    

    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(ConfigureCryptBreakerWorkspace.class);
        bd.setDisplayName("<html><font color='blue'><center>Create Workspace:<br>Password Breaker");
        return bd;
    }

}
