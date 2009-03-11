/*
 * Created on Jun 9, 2005
 *
 * Copyright 2005 by Informatics, Inc. 
 */
package org.dwfa.queue.bpa.tasks.failsafe;

import java.beans.BeanDescriptor;
import java.beans.SimpleBeanInfo;

public class LaunchFailsafeBeanInfo extends SimpleBeanInfo {

    public LaunchFailsafeBeanInfo() {
        super();
    }
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(LaunchFailsafeBeanInfo.class);
        bd.setDisplayName("<html><font color='green'><center>Launch Failsafe");
        return bd;
    }

}
