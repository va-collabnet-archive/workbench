/*
 * Created on Feb 22, 2006
 *
 * Copyright 2006 by Informatics, Inc. 
 */
package org.dwfa.bpa.tasks.prop;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.bpa.tasks.editor.ProcessDataIdEditor;
import org.dwfa.bpa.tasks.editor.PropertyNameLabelEditor;

public class SetLocalPropertyBeanInfo extends SimpleBeanInfo {

    public SetLocalPropertyBeanInfo() {
        super();
    }
    public PropertyDescriptor[] getPropertyDescriptors() {
        try {  
            PropertyDescriptor remotePropertyName =
                new PropertyDescriptor("remotePropName", SetLocalProperty.class);
            remotePropertyName.setBound(true);
            remotePropertyName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            remotePropertyName.setDisplayName("<html><font color='green'>Remote property:");
            remotePropertyName.setShortDescription("Name of the remote property to get. ");
            
            PropertyDescriptor localPropertyName =
                new PropertyDescriptor("localPropName", SetLocalProperty.class);
            localPropertyName.setBound(true);
            localPropertyName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            localPropertyName.setDisplayName("<html><font color='blue'>Local property:");
            localPropertyName.setShortDescription("Name of the local property to set. ");

            PropertyDescriptor process =
                new PropertyDescriptor("processDataId", SetLocalProperty.class);
            process.setBound(true);
            process.setPropertyEditorClass(ProcessDataIdEditor.class);
            process.setDisplayName("<html><font color='green'>Process:");
            process.setShortDescription("A data id for the process container to launch. Only data containers that contain I_EncodeBusinessProcess objects can be dropped. ");


            PropertyDescriptor rv[] = {process, localPropertyName, remotePropertyName};
            return rv;
        } catch (IntrospectionException e) {
             throw new Error(e.toString());
        }
    }        
    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
           BeanDescriptor bd = new BeanDescriptor(SetLocalProperty.class);
           bd.setDisplayName("<html>Set <font color='blue'>Local</font> Property");
        return bd;
    }

}

