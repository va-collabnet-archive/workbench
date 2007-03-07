/*
 * Created on Mar 8, 2006
 *
 * Copyright 2006 by Informatics, Inc. 
 */
package org.dwfa.bpa.tasks.prop;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.bpa.tasks.editor.JTextFieldEditorOneLine;
import org.dwfa.bpa.tasks.editor.ProcessDataIdEditor;
import org.dwfa.bpa.tasks.editor.PropertyNameLabelEditor;

public class SetRemotePropertyFromTextBeanInfo extends SimpleBeanInfo {

    public SetRemotePropertyFromTextBeanInfo() {
        super();
    }
    public PropertyDescriptor[] getPropertyDescriptors() {
        try {  
            PropertyDescriptor remotePropertyName =
                new PropertyDescriptor("remotePropertyName", SetRemotePropertyFromText.class);
            remotePropertyName.setBound(true);
            remotePropertyName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            remotePropertyName.setDisplayName("<html><font color='green'>Remote property:");
            remotePropertyName.setShortDescription("Name of the remote property to get. ");
            
            PropertyDescriptor process =
                new PropertyDescriptor("processDataId", SetRemotePropertyFromText.class);
            process.setBound(true);
            process.setPropertyEditorClass(ProcessDataIdEditor.class);
            process.setDisplayName("<html><font color='green'>Process:");
            process.setShortDescription("A data id for the process container to launch. Only data containers that contain I_EncodeBusinessProcess objects can be dropped. ");

            PropertyDescriptor valueTextPropertyName =
                new PropertyDescriptor("valueText", SetRemotePropertyFromText.class);
            valueTextPropertyName.setBound(true);
            valueTextPropertyName.setPropertyEditorClass(JTextFieldEditorOneLine.class);
            valueTextPropertyName.setDisplayName("<html><font color='green'>New value:");
            valueTextPropertyName.setShortDescription("Text representation of the new property value. ");

            PropertyDescriptor rv[] = {process, remotePropertyName, valueTextPropertyName};
            return rv;
        } catch (IntrospectionException e) {
             throw new Error(e.toString());
        }
    }        
    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
           BeanDescriptor bd = new BeanDescriptor(SetRemotePropertyFromText.class);
           bd.setDisplayName("<html><center>Set <font color='green'>Remote</font> Property<p>from text");
        return bd;
    }

}

