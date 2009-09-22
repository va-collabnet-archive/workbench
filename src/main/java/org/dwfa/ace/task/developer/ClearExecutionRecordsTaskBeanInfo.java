package org.dwfa.ace.task.developer;

import java.beans.BeanDescriptor;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

public class ClearExecutionRecordsTaskBeanInfo extends SimpleBeanInfo {
    public ClearExecutionRecordsTaskBeanInfo() {
        super();
    }

    public PropertyDescriptor[] getPropertyDescriptors() {
        PropertyDescriptor rv[] = {};
        return rv;

    }

    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(ClearExecutionRecordsTask.class);
        bd.setDisplayName("<html><font color='green'><center>Clear BP execution records");
        return bd;
    }
}