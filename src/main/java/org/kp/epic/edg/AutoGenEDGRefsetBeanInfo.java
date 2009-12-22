package org.kp.epic.edg;

import java.beans.BeanDescriptor;
import java.beans.SimpleBeanInfo;

public class AutoGenEDGRefsetBeanInfo extends SimpleBeanInfo {
    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(AutoGenEDGRefset.class);
        bd.setDisplayName("<html><font color='green'><center>Autogen EDG Clinical<br>Refset Members");
        return bd;
    }

}
