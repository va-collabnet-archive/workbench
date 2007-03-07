/*
 * Created on Jun 9, 2005
 *
 * Copyright 2005 by Informatics, Inc. 
 */
package org.dwfa.queue.bpa.tasks.failsafe;

import java.beans.BeanDescriptor;
import java.beans.SimpleBeanInfo;

public class DestroyFailsafeBeanInfo extends SimpleBeanInfo {

    public DestroyFailsafeBeanInfo() {
        super();
    }
    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(DestroyFailsafe.class);
        bd.setDisplayName("<html><font color='red'><center>Destroy Failsafe");
        return bd;
    }

}
