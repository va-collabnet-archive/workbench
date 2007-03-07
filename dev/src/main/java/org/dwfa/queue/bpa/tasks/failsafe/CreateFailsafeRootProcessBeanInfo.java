/*
 * Created on Jun 14, 2005
 *
 * Copyright 2005 by Informatics, Inc. 
 */
package org.dwfa.queue.bpa.tasks.failsafe;

import java.beans.BeanDescriptor;

/**
 * @author kec
 *
 */
public class CreateFailsafeRootProcessBeanInfo extends CreateFailsafeBeanInfo {
    protected Class getBeanClass() {
        return CreateFailsafeRootProcess.class;
    }
    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(getBeanClass());
        bd.setDisplayName("<html><font color='green'><center>ROOT Process<br>Create Failsafe");
        return bd;
    }

}
