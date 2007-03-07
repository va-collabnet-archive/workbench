package org.dwfa.clock.task;

import java.beans.BeanDescriptor;
import java.beans.SimpleBeanInfo;

public class IncrementMasterClockBeanInfo extends SimpleBeanInfo {

    /**
     * 
     */
    public IncrementMasterClockBeanInfo() {
        super();
    }
    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(IncrementMasterClock.class);
        bd.setDisplayName("<html><font color='green'><center>Increment<br>Master Clock");
        return bd;
    }
}
