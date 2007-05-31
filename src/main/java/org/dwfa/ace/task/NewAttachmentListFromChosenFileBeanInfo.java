package org.dwfa.ace.task;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.bpa.tasks.editor.JTextFieldEditor;

/**
 * Bean info to NewAttachmentListFromChosenFile class.
 * @author Christine Hill
 *
 */
public class NewAttachmentListFromChosenFileBeanInfo extends SimpleBeanInfo {

    /**
     *
     */
    public NewAttachmentListFromChosenFileBeanInfo() {
        super();
     }

    public PropertyDescriptor[] getPropertyDescriptors() {
        try {

            PropertyDescriptor listName =
                new PropertyDescriptor("listName", NewAttachmentListFromChosenFile.class);
            listName.setBound(true);
            listName.setPropertyEditorClass(JTextFieldEditor.class);
            listName.setDisplayName("<html><font color='green'>Name of temporary list:");
            listName.setShortDescription("Name of the temporary list.");

            PropertyDescriptor fileKey =
                new PropertyDescriptor("fileKey", NewAttachmentListFromChosenFile.class);
            fileKey.setBound(true);
            fileKey.setPropertyEditorClass(JTextFieldEditor.class);
            fileKey.setDisplayName("<html><font color='green'>File key:");
            fileKey.setShortDescription("File key.");

            PropertyDescriptor rv[] = { listName };
            return rv;
        } catch (IntrospectionException e) {
             throw new Error(e.toString());
        }
    }
    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(NewAttachmentListFromChosenFile.class);
        bd.setDisplayName("<html><font color='green'><center>New Attachment<br>List From<br>Previously<br>Chosen File");
        return bd;
    }

}
