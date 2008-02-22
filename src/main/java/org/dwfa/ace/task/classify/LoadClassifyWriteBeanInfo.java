package org.dwfa.ace.task.classify;

import java.beans.BeanDescriptor;
import java.beans.SimpleBeanInfo;

public class LoadClassifyWriteBeanInfo  extends SimpleBeanInfo {
    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(LoadClassifyWrite.class);
        bd.setDisplayName("<html><font color='green'><center>Load Classify Write");
        return bd;
    }

}