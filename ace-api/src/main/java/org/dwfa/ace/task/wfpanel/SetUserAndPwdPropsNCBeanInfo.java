package org.dwfa.ace.task.wfpanel;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.bpa.tasks.editor.JTextFieldEditor;
import org.dwfa.bpa.tasks.editor.PropertyNameLabelEditor;

public class SetUserAndPwdPropsNCBeanInfo extends SimpleBeanInfo {

	public PropertyDescriptor[] getPropertyDescriptors() {
		try {
			PropertyDescriptor userPropName = new PropertyDescriptor(
					"userPropName", getBeanDescriptor().getBeanClass());
			userPropName.setBound(true);
			userPropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
			userPropName.setDisplayName("<html><font color='green'>username prop:");
			userPropName.setShortDescription("The property that will contain the username.");

			PropertyDescriptor passwordPropName = new PropertyDescriptor(
					"passwordPropName", getBeanDescriptor().getBeanClass());
			passwordPropName.setBound(true);
			passwordPropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
			passwordPropName.setDisplayName("<html><font color='green'>password prop:");
			passwordPropName.setShortDescription("The property that will contain the password.");

			PropertyDescriptor message = new PropertyDescriptor(
					"message", getBeanDescriptor().getBeanClass());
			message.setBound(true);
			message.setPropertyEditorClass(JTextFieldEditor.class);
			message.setDisplayName("<html><font color='green'>prompt:");
			message.setShortDescription("Prompt to provide the user what the username and password is for. ");

			PropertyDescriptor rv[] = { userPropName, passwordPropName, message };
			return rv;
		} catch (IntrospectionException e) {
			throw new Error(e.toString());
		}
	}

    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(SetUserAndPwdPropsNC.class);
        bd.setDisplayName("<html><font color='green'><center>Set Props for<br>User and Pwd<br>Next, or Cancel");
        return bd;
    }

}
