package org.dwfa.ace.task.file;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.bpa.tasks.editor.JTextFieldEditor;
import org.dwfa.bpa.tasks.editor.PropertyNameLabelEditor;

/**
 * Bean info for ChooseTxtFileTask class.
 * 
 * @author Christine Hill
 * 
 */
public class ChooseTxtFileCancelOrCompleteTaskBeanInfo extends SimpleBeanInfo {

    /**
     *
     */
    public ChooseTxtFileCancelOrCompleteTaskBeanInfo() {
        super();
    }

    public PropertyDescriptor[] getPropertyDescriptors() {
        try {

            PropertyDescriptor fileKey = new PropertyDescriptor("fileKey", getBeanDescriptor().getBeanClass());
            fileKey.setBound(true);
            fileKey.setPropertyEditorClass(PropertyNameLabelEditor.class);
            fileKey.setDisplayName("<html><font color='green'>Name of file key:");
            fileKey.setShortDescription("Name of file key.");

            PropertyDescriptor message = new PropertyDescriptor("message", getBeanDescriptor().getBeanClass());
            message.setBound(true);
            message.setPropertyEditorClass(JTextFieldEditor.class);
            message.setDisplayName("<html><font color='green'>Prompt to user:");
            message.setShortDescription("Message when prompting user for file");

            PropertyDescriptor rv[] = { fileKey, message };
            return rv;
        } catch (IntrospectionException e) {
            throw new Error(e.toString());
        }
    }

    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(ChooseTxtFileCancelOrCompleteTask.class);
        bd.setDisplayName("<html><font color='green'><center>Choose text file<br>cancel or complete");
        return bd;
    }

}
