package org.dwfa.ace.task.svn;

import java.beans.BeanDescriptor;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

public class RemoveSvnEntryBeanInfo extends SimpleBeanInfo {

    public PropertyDescriptor[] getPropertyDescriptors() {
             return new PropertyDescriptor[] { };
    }

    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(RemoveSvnEntry.class);
        bd.setDisplayName("<html><font color='green'><center>Remove Svn Entry<br>(selected from list)");
        return bd;
    }
}