package org.dwfa.clock.task;

import java.beans.BeanDescriptor;
import java.beans.SimpleBeanInfo;

public class ResetMasterClockBeanInfo extends SimpleBeanInfo {

    /**
     * 
     */
    public ResetMasterClockBeanInfo() {
        super();
    }
    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(ResetMasterClock.class);
        bd.setDisplayName("<html><font color='green'><center>Reset Master Clock");
        return bd;
    }
}
