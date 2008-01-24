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
public class CreateFailsafeParentProcessBeanInfo extends CreateFailsafeBeanInfo {
    protected Class<CreateFailsafeParentProcess> getBeanClass() {
        return CreateFailsafeParentProcess.class;
    }
    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(getBeanClass());
        bd.setDisplayName("<html><font color='green'><center>Parent Process<br>Create Failsafe");
        return bd;
    }

}
