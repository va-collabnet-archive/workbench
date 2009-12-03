package org.dwfa.ace.task.classify;

import java.beans.BeanDescriptor;
import java.beans.SimpleBeanInfo;

public class FasterLoadBeanInfo extends SimpleBeanInfo {
    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(FasterLoad.class);
        // :EDIT:MEC: color & text change
        // bd.setDisplayName("<html><font color='green'><center>Faster Load");
        bd.setDisplayName("<html><font color='#B3B3B3'><center>FasterLoad");
        return bd;
    }

}
