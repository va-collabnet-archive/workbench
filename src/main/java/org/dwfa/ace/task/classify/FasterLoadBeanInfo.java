package org.dwfa.ace.task.classify;

import java.beans.BeanDescriptor;
import java.beans.SimpleBeanInfo;

public class FasterLoadBeanInfo extends SimpleBeanInfo {
    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(FasterLoad.class);
        bd.setDisplayName("<html><font color='green'><center>Faster Load");
        return bd;
    }

}
