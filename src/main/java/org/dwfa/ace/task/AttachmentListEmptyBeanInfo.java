package org.dwfa.ace.task;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.bpa.tasks.editor.PropertyNameLabelEditor;

public class AttachmentListEmptyBeanInfo extends SimpleBeanInfo {

    /**
     *
     */
    public AttachmentListEmptyBeanInfo() {
        super();
     }

    public PropertyDescriptor[] getPropertyDescriptors() {
        try {
            PropertyDescriptor listName =
                new PropertyDescriptor("listName", AttachmentListEmpty.class);
            listName.setBound(true);
            listName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            listName.setDisplayName("<html><font color='green'>Name of temporary list:");
            listName.setShortDescription("Name of the temporary list.");

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
        BeanDescriptor bd = new BeanDescriptor(AttachmentListEmpty.class);
        bd.setDisplayName("<html><font color='green'><center>Attachment List Empty");
        return bd;
    }

}
