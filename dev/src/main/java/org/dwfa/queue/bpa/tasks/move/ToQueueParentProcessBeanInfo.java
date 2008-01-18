/*
 * Created on Jun 14, 2005
 *
 * Copyright 2005 by Informatics, Inc. 
 */
package org.dwfa.queue.bpa.tasks.move;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.bpa.tasks.editor.ConceptLabelEditor;

/**
 * @author kec
 *
 */
public class ToQueueParentProcessBeanInfo extends SimpleBeanInfo {
    public PropertyDescriptor[] getPropertyDescriptors() {
        try {  
            PropertyDescriptor queueType =
                new PropertyDescriptor("queueType", ToQueueParentProcess.class);
            queueType.setBound(true);
            queueType.setPropertyEditorClass(ConceptLabelEditor.class);
            queueType.setDisplayName("Queue type");
            queueType.setShortDescription("The type of queue to place this process into.");



            PropertyDescriptor rv[] =
                {queueType};
            return rv;
        } catch (IntrospectionException e) {
             throw new Error(e.toString());
        }
     }        
    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(ToQueueParentProcess.class);
        bd.setDisplayName("<html><font color='green'><center>Parent Process<br>To Queue");
        return bd;
    }

}
