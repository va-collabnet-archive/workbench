package org.dwfa.clock.task;

import java.beans.BeanDescriptor;
import java.beans.SimpleBeanInfo;

public class SetWorkerSystemClockBeanInfo extends SimpleBeanInfo {

    /**
     * 
     */
    public SetWorkerSystemClockBeanInfo() {
        super();
    }
    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(SetWorkerSystemClock.class);
        bd.setDisplayName("<html><font color='green'><center>Set Worker Time<br>To System Clock");
        return bd;
    }
}
