package org.dwfa.ace.task;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.bpa.tasks.editor.JTextFieldEditor;
import org.dwfa.bpa.tasks.editor.PropertyNameLabelEditor;

public class WriteUuidListListToDiskBeanInfo extends SimpleBeanInfo {

     public PropertyDescriptor[] getPropertyDescriptors() {
        try {
            PropertyDescriptor uuidListListPropName =
                new PropertyDescriptor("uuidListListPropName", getBeanDescriptor().getBeanClass());
            uuidListListPropName.setBound(true);
            uuidListListPropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            uuidListListPropName.setDisplayName("<html><font color='green'>Uuid List:");
            uuidListListPropName.setShortDescription("Uuid list.");
            
            PropertyDescriptor fileName =
                new PropertyDescriptor("fileName", getBeanDescriptor().getBeanClass());
            fileName.setBound(true);
            fileName.setPropertyEditorClass(JTextFieldEditor.class);
            fileName.setDisplayName("<html><font color='green'>Uuid File:");
            fileName.setShortDescription("File Name of UUID list list file.");

            PropertyDescriptor rv[] = { uuidListListPropName, fileName };
            return rv;
        } catch (IntrospectionException e) {
             throw new Error(e.toString());
        }
    }
    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(WriteUuidListListToDisk.class);
        bd.setDisplayName("<html><font color='green'><center>Write UUID List <br> List to File");
        return bd;
    }

}