package org.dwfa.ace.task;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.bpa.tasks.editor.PropertyNameLabelEditor;

/**
 * Bean info to NewAttachmentListFromChosenFile class.
 * @author Christine Hill
 *
 */
public class ChooseFileBeanInfo extends SimpleBeanInfo {

    /**
     *
     */
    public ChooseFileBeanInfo() {
        super();
     }

    public PropertyDescriptor[] getPropertyDescriptors() {
        try {

            PropertyDescriptor fileKey =
                new PropertyDescriptor("fileKey", ChooseFile.class);
            fileKey.setBound(true);
            fileKey.setPropertyEditorClass(PropertyNameLabelEditor.class);
            fileKey.setDisplayName("<html><font color='green'>Name of file key:");
            fileKey.setShortDescription("Name of file key.");

            PropertyDescriptor rv[] = { fileKey };
            return rv;
        } catch (IntrospectionException e) {
             throw new Error(e.toString());
        }
    }
    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(ChooseFile.class);
        bd.setDisplayName("<html><font color='green'><center>Choose File");
        return bd;
    }

}
