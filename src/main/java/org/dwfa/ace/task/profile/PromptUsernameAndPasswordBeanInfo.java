package org.dwfa.ace.task.profile;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.bpa.tasks.editor.JTextFieldEditor;
import org.dwfa.bpa.tasks.editor.PropertyNameLabelEditor;

public class PromptUsernameAndPasswordBeanInfo extends SimpleBeanInfo {
    

    public PropertyDescriptor[] getPropertyDescriptors() {
        try {  
            PropertyDescriptor promptMessage =
                new PropertyDescriptor("promptMessage", getBeanDescriptor().getBeanClass());
            promptMessage.setBound(true);
            promptMessage.setPropertyEditorClass(JTextFieldEditor.class);
            promptMessage.setDisplayName("<html><font color='green'>prompt message:");
            promptMessage.setShortDescription("The prompt message.");

            PropertyDescriptor usernamePropName =
                new PropertyDescriptor("usernamePropName", getBeanDescriptor().getBeanClass());
            usernamePropName.setBound(true);
            usernamePropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            usernamePropName.setDisplayName("<html><font color='green'>username prop:");
            usernamePropName.setShortDescription("The property that contains the username.");

            PropertyDescriptor passwordPropName =
                new PropertyDescriptor("passwordPropName", getBeanDescriptor().getBeanClass());
            passwordPropName.setBound(true);
            passwordPropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            passwordPropName.setDisplayName("<html><font color='green'>password prop:");
            passwordPropName.setShortDescription("The property that contains the password.");

            PropertyDescriptor rv[] =
                { promptMessage, usernamePropName, passwordPropName };
            return rv;
        } catch (IntrospectionException e) {
             throw new Error(e.toString());
        }
     }        
    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(PromptUsernameAndPassword.class);
        bd.setDisplayName("<html><font color='green'><center>prompt<br>username/password");
        return bd;
    }
}
