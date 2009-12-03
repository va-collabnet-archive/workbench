package org.dwfa.ace.task.classify;

import java.beans.BeanDescriptor;
import java.beans.SimpleBeanInfo;

public class LoadClassifyWriteBeanInfo extends SimpleBeanInfo {
    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(LoadClassifyWrite.class);
        // :EDIT:MEC: color & text change
        // bd.setDisplayName("<html><font color='green'><center>Load &amp; Classify");
        bd.setDisplayName("<html><font color='#B3B3B3'><center>LoadClassifyWrite");
        return bd;
    }
}