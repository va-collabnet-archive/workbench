package org.dwfa.ace.task.queue;

import java.beans.BeanDescriptor;
import java.beans.SimpleBeanInfo;

public class AddAllProfilesToVisibleQueuesBeanInfo extends SimpleBeanInfo {

    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(AddAllProfilesToVisibleQueues.class);
        bd.setDisplayName("<html><font color='green'><center>Add All Profiles<br>To Visible Queues");
        return bd;
    }

}
