package org.dwfa.clock.task;

import java.beans.BeanDescriptor;
import java.beans.SimpleBeanInfo;

public class ResetWorkerSimulationClockBeanInfo extends SimpleBeanInfo {

    /**
     * 
     */
    public ResetWorkerSimulationClockBeanInfo() {
        super();
    }
    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(ResetWorkerSimulationClock.class);
        bd.setDisplayName("<html><font color='green'><center>Reset Worker<br>Simulation Clock");
        return bd;
    }
}
