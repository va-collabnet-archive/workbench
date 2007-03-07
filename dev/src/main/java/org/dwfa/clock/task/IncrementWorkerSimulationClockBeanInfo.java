package org.dwfa.clock.task;

import java.beans.BeanDescriptor;
import java.beans.SimpleBeanInfo;

public class IncrementWorkerSimulationClockBeanInfo extends SimpleBeanInfo {

    /**
     * 
     */
    public IncrementWorkerSimulationClockBeanInfo() {
        super();
    }
    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(IncrementWorkerSimulationClock.class);
        bd.setDisplayName("<html><font color='green'><center>Increment Worker<br>Simulation Clock");
        return bd;
    }
}
