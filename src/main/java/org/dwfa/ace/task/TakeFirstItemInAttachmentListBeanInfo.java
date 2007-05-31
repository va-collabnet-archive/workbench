package org.dwfa.ace.task;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.bpa.tasks.editor.PropertyNameLabelEditor;

/**
 * Bean info for TakeFirstItemInAttachmentList class.
 * @author Christine Hill
 *
 */
public class TakeFirstItemInAttachmentListBeanInfo extends SimpleBeanInfo {

    /**
     *
     */
    public TakeFirstItemInAttachmentListBeanInfo() {
        super();
     }

    public PropertyDescriptor[] getPropertyDescriptors() {
        try {
            PropertyDescriptor listName =
                new PropertyDescriptor("listName", TakeFirstItemInAttachmentList.class);
            listName.setBound(true);
            listName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            listName.setDisplayName("<html><font color='green'>Name of temporary list:");
            listName.setShortDescription("Name of the temporary list.");

            PropertyDescriptor conceptKey =
                new PropertyDescriptor("conceptKey", TakeFirstItemInAttachmentList.class);
            conceptKey.setBound(true);
            conceptKey.setPropertyEditorClass(PropertyNameLabelEditor.class);
            conceptKey.setDisplayName("<html><font color='green'>Concept key:");
            conceptKey.setShortDescription("Concept key.");

            PropertyDescriptor rv[] = { listName, conceptKey };
            return rv;
        } catch (IntrospectionException e) {
             throw new Error(e.toString());
        }
    }
    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(TakeFirstItemInAttachmentList.class);
        bd.setDisplayName("<html><font color='green'><center>Take First Item<br>In Attachment<br> List");
        return bd;
    }

}