package org.dwfa.ace.task;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.bpa.tasks.editor.PropertyNameLabelEditor;
import org.dwfa.bpa.tasks.editor.JTextFieldEditor;

public class NewAttachmentListFromFileBeanInfo extends SimpleBeanInfo {

    /**
     *
     */
    public NewAttachmentListFromFileBeanInfo() {
        super();
     }

    public PropertyDescriptor[] getPropertyDescriptors() {
        try {

            PropertyDescriptor listName =
                new PropertyDescriptor("listName", NewAttachmentListFromFile.class);
            listName.setBound(true);
            listName.setPropertyEditorClass(JTextFieldEditor.class);
            listName.setDisplayName("<html><font color='green'>Name of temporary list:");
            listName.setShortDescription("Name of the temporary list.");

            PropertyDescriptor fileName =
                new PropertyDescriptor("fileName", NewAttachmentListFromFile.class);
            fileName.setBound(true);
            fileName.setPropertyEditorClass(JTextFieldEditor.class);
            fileName.setDisplayName("<html><font color='green'>File name:");
            fileName.setShortDescription("Name/location of the file to attach.");

            PropertyDescriptor rv[] = { listName, fileName };
            return rv;
        } catch (IntrospectionException e) {
             throw new Error(e.toString());
        }
    }
    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(NewAttachmentListFromFile.class);
        bd.setDisplayName("<html><font color='green'><center>New Attachment List<br> From File");
        return bd;
    }

}
