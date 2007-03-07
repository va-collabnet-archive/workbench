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

import org.dwfa.bpa.tasks.editor.PropertyNameLabelEditor;

public class ToQueueWithEntryRecordBeanInfo extends SimpleBeanInfo {

    public ToQueueWithEntryRecordBeanInfo() {
        super();
    }
    public PropertyDescriptor[] getPropertyDescriptors() {
        try {  
            
            PropertyDescriptor localPropertyName =
                new PropertyDescriptor("localPropName", ToQueueWithEntryRecord.class);
            localPropertyName.setBound(true);
            localPropertyName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            localPropertyName.setDisplayName("<html><font color='blue'>Entry record:");
            localPropertyName.setShortDescription("Name of the local property that holds the Entry record. ");

            PropertyDescriptor rv[] = { localPropertyName };
            return rv;
        } catch (IntrospectionException e) {
             throw new Error(e.toString());
        }
    }        
    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
           BeanDescriptor bd = new BeanDescriptor(ToQueueWithEntryRecord.class);
           bd.setDisplayName("<html><center>To Queue<br>With Entry Record");
        return bd;
    }


}
