package org.dwfa.ace.task.queue;

import java.beans.BeanDescriptor;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

public class OpenAllInboxesBeanInfo extends SimpleBeanInfo {
    public PropertyDescriptor[] getPropertyDescriptors() {
            return new PropertyDescriptor[] {};
    }

    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(OpenAllInboxes.class);
        bd.setDisplayName("<html><font color='green'><center>Open All Inboxes");
        return bd;
    }

}
