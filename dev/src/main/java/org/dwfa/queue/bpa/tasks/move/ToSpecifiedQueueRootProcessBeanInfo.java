/*
 * Created on Jun 13, 2005
 *
 * Copyright 2005 by Informatics, Inc. 
 */
package org.dwfa.queue.bpa.tasks.move;

import java.beans.BeanDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.bpa.tasks.deadline.SetDeadlineRelative;

/**
 * @author kec
 *
 */
public class ToSpecifiedQueueRootProcessBeanInfo extends SimpleBeanInfo {
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(SetDeadlineRelative.class);
        bd.setDisplayName("<html><font color='green'><center>ROOT Process<br>To Specific Queue");
        return bd;
    }

}
