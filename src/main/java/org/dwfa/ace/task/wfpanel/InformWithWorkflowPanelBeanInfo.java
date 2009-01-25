package org.dwfa.ace.task.wfpanel;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.bpa.tasks.editor.JTextFieldEditor;
import org.dwfa.bpa.tasks.editor.PropertyNameLabelEditor;

public class InformWithWorkflowPanelBeanInfo extends SimpleBeanInfo {
	public PropertyDescriptor[] getPropertyDescriptors() {
		try {
            PropertyDescriptor profilePropName =
                new PropertyDescriptor("profilePropName", getBeanDescriptor().getBeanClass());
            profilePropName.setBound(true);
            profilePropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            profilePropName.setDisplayName("<html><font color='green'>profile prop:");
            profilePropName.setShortDescription("The property that will contain the profile this task uses and/or modifies.");

            PropertyDescriptor information = new PropertyDescriptor(
					"information", getBeanDescriptor().getBeanClass());
			information.setBound(true);
			information.setPropertyEditorClass(JTextFieldEditor.class);
			information.setDisplayName("<html><font color='green'>Information:");
			information.setShortDescription("Information to present to the user in the workflow panel. ");

			PropertyDescriptor rv[] = { profilePropName, information };
			return rv;
		} catch (IntrospectionException e) {
			throw new Error(e.toString());
		}
	}

	/**
	 * @see java.beans.BeanInfo#getBeanDescriptor()
	 */
	public BeanDescriptor getBeanDescriptor() {
		BeanDescriptor bd = new BeanDescriptor(
				InformWithWorkflowPanel.class);
		bd.setDisplayName("<html><font color='green'><center>Inform");
		return bd;
	}
}
