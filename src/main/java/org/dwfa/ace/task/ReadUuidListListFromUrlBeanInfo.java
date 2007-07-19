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
            PropertyDescriptor uuidListListPropName =
                //new PropertyDescriptor("luceneDups/dupPotMatchResults/dwfaDups.txt", ReadUuidListListFromUrl.class);
            	new PropertyDescriptor("uuidListListPropName", ReadUuidListListFromUrl.class);
            uuidListListPropName.setBound(true);
            uuidListListPropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            uuidListListPropName.setDisplayName("<html><font color='green'>Uuid List:");
            uuidListListPropName.setShortDescription("Uuid list.");
            
            PropertyDescriptor uuidFileNamePropName =
                new PropertyDescriptor("uuidFileNamePropName", ReadUuidListListFromUrl.class);
            uuidFileNamePropName.setBound(true);
            uuidFileNamePropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            uuidFileNamePropName.setDisplayName("<html><font color='green'>UUID File Name:");
            uuidFileNamePropName.setShortDescription("File Name");

            PropertyDescriptor rv[] = { uuidListListPropName, uuidFileNamePropName };
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