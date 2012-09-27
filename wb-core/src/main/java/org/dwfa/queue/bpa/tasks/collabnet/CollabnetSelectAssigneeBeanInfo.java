package org.dwfa.queue.bpa.tasks.collabnet;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.bpa.tasks.editor.JTextFieldEditor;

public class CollabnetSelectAssigneeBeanInfo extends SimpleBeanInfo {

    /**
     * 
     */
    public CollabnetSelectAssigneeBeanInfo() {
        super();
    }

    public PropertyDescriptor[] getPropertyDescriptors() {
        try {
            PropertyDescriptor message = new PropertyDescriptor("assignees", CollabnetSelectAssignee.class);
            message.setBound(true);
            message.setPropertyEditorClass(JTextFieldEditor.class);
            message.setDisplayName("assignees");
            message.setShortDescription("FORMAT: display_name1/collabnet_name1: ... ");

            PropertyDescriptor rv[] = { message };
            return rv;
        } catch (IntrospectionException e) {
            throw new Error(e.toString());
        }
    }
    
    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(CollabnetSelectAssignee.class);
        bd.setDisplayName("<html><font color='green'><center>Select Assignee");
        return bd;
    }
    
}
