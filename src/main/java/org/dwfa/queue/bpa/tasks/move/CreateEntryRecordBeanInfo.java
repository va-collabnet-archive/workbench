/*
 * Created on Mar 8, 2006
 *
 * Copyright 2006 by Informatics, Inc. 
 */
package org.dwfa.queue.bpa.tasks.move;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.bpa.tasks.editor.ConceptLabelEditor;
import org.dwfa.bpa.tasks.editor.PropertyNameLabelEditor;

public class CreateEntryRecordBeanInfo extends SimpleBeanInfo {

    public CreateEntryRecordBeanInfo() {
        super();
    }
    public PropertyDescriptor[] getPropertyDescriptors() {
        try {  
            PropertyDescriptor queueType =
                new PropertyDescriptor("queueType", CreateEntryRecord.class);
            queueType.setBound(true);
            queueType.setPropertyEditorClass(ConceptLabelEditor.class);
            queueType.setDisplayName("Queue type:");
            queueType.setShortDescription("The type of queue to generate the entry record for.");

            PropertyDescriptor localPropertyName =
                new PropertyDescriptor("localPropName", CreateEntryRecord.class);
            localPropertyName.setBound(true);
            localPropertyName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            localPropertyName.setDisplayName("<html><font color='blue'>Entry record:");
            localPropertyName.setShortDescription("Name of the local property that holds the entry record. ");


            PropertyDescriptor rv[] =
                {queueType, localPropertyName};
            return rv;
        } catch (IntrospectionException e) {
             throw new Error(e.toString());
        }
     }        
    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(CreateEntryRecord.class);
        bd.setDisplayName("<html><font color='green'><center>Create Entry Record");
        return bd;
    }
}
