package org.dwfa.ace.task;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.bpa.tasks.editor.JTextFieldEditor;
import org.dwfa.bpa.tasks.editor.PropertyNameLabelEditor;

/**
 * Bean info for ReadUuidListListFromUrl class.
 * @author Susan Castillo
 *
 */
public class ReadUuidListListFromUrlBeanInfo extends SimpleBeanInfo {

    /**
     *
     */
    public ReadUuidListListFromUrlBeanInfo() {
        super();
     }

    public PropertyDescriptor[] getPropertyDescriptors() {
        try {
            PropertyDescriptor potDupUuidListPropName =
                new PropertyDescriptor("potDupUuidListPropName", ReadUuidListListFromUrl.class);
            potDupUuidListPropName.setBound(true);
            potDupUuidListPropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            potDupUuidListPropName.setDisplayName("<html><font color='green'>Uuid List prop:");
            potDupUuidListPropName.setShortDescription("Uuid list.");
            
            PropertyDescriptor dupPotFileName =
                new PropertyDescriptor("dupPotFileName", ReadUuidListListFromUrl.class);
            dupPotFileName.setBound(true);
            dupPotFileName.setPropertyEditorClass(JTextFieldEditor.class);
            dupPotFileName.setDisplayName("<html><font color='green'>Dup File Name:");
            dupPotFileName.setShortDescription("File Name.");

            PropertyDescriptor rv[] = { potDupUuidListPropName, dupPotFileName };
            return rv;
        } catch (IntrospectionException e) {
             throw new Error(e.toString());
        }
    }
    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(ReadUuidListListFromUrl.class);
        bd.setDisplayName("<html><font color='green'><center>Read UUID List <br> List From File");
        return bd;
    }

}