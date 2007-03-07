/*
 * Created on Jun 9, 2005
 *
 * Copyright 2005 by Informatics, Inc. 
 */
package org.dwfa.queue.bpa.tasks.move;

import java.beans.BeanDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.bpa.tasks.deadline.SetDeadlineRelative;

public class ToSpecifiedQueueBeanInfo extends SimpleBeanInfo {

    public ToSpecifiedQueueBeanInfo() {
        super();
    }
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(SetDeadlineRelative.class);
        bd.setDisplayName("<html><font color='green'><center>To Specific Queue");
        return bd;
    }

}
