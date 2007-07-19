package org.dwfa.ace.task.queue;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.bpa.tasks.editor.JTextFieldEditorOneLine;
import org.dwfa.bpa.tasks.editor.PropertyNameLabelEditor;

public class MakeQueueConfigInProfileFolderBeanInfo extends SimpleBeanInfo {
    

    public PropertyDescriptor[] getPropertyDescriptors() {
        try {  
            PropertyDescriptor profileDir =
                new PropertyDescriptor("profileDir", getBeanDescriptor().getBeanClass());
            profileDir.setBound(true);
            profileDir.setPropertyEditorClass(JTextFieldEditorOneLine.class);
            profileDir.setDisplayName("<html><font color='green'>profile dir:");
            profileDir.setShortDescription("The profile directory to write the queue config file to.");

            PropertyDescriptor queueFolderName =
                new PropertyDescriptor("queueFolderName", getBeanDescriptor().getBeanClass());
            queueFolderName.setBound(true);
            queueFolderName.setPropertyEditorClass(JTextFieldEditorOneLine.class);
            queueFolderName.setDisplayName("<html><font color='green'>queue dir:");
            queueFolderName.setShortDescription("The directory name for the queue inside the user's profile dir.");

            PropertyDescriptor usernamePropName =
                new PropertyDescriptor("usernamePropName", getBeanDescriptor().getBeanClass());
            usernamePropName.setBound(true);
            usernamePropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            usernamePropName.setDisplayName("<html><font color='green'>username prop:");
            usernamePropName.setShortDescription("The property that contains the username.");

            PropertyDescriptor template =
                new PropertyDescriptor("template", getBeanDescriptor().getBeanClass());
            template.setBound(true);
            template.setPropertyEditorClass(JTextFieldEditorOneLine.class);
            template.setDisplayName("<html><font color='green'>template file:");
            template.setShortDescription("The template that the queue.config file will be based upon.");

            PropertyDescriptor rv[] =
                { profileDir, queueFolderName, usernamePropName, template };
            return rv;
        } catch (IntrospectionException e) {
             throw new Error(e.toString());
        }
     }        
    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(MakeQueueConfigInProfileFolder.class);
        bd.setDisplayName("<html><font color='green'><center>make queue config<br>in profile folder");
        return bd;
    }
}
