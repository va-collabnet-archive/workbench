package org.dwfa.ace.task.classify;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.bpa.tasks.editor.JTextFieldEditor;

public class GetBaseClassificationBeanInfo extends SimpleBeanInfo {
    
    public PropertyDescriptor[] getPropertyDescriptors() {
        try {
            PropertyDescriptor fileName =
                new PropertyDescriptor("fileName", GetBaseClassification.class);
            fileName.setBound(true);
            fileName.setPropertyEditorClass(JTextFieldEditor.class);
            fileName.setDisplayName("<html><font color='green'>File name:");
            fileName.setShortDescription("Enter the name of the file from which to load the base classification state.");

            PropertyDescriptor rv[] = {
                    fileName,
            };
            return rv;
        } catch (IntrospectionException e) {
            throw new Error(e.toString());
        }
    }
    
    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(GetResults.class);
        bd.setDisplayName("<html><font color='green'><center>Get Base Classification");
        return bd;
    }

}
