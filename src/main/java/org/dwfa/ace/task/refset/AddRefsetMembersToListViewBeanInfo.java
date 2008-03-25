package org.dwfa.ace.task.refset;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.bpa.tasks.editor.PropertyNameLabelEditor;

public class AddRefsetMembersToListViewBeanInfo extends SimpleBeanInfo {

	public PropertyDescriptor[] getPropertyDescriptors() {
        PropertyDescriptor componentPropName;
		try {
			componentPropName = new PropertyDescriptor("componentPropName", AddRefsetMembersToListView.class);
			componentPropName.setBound(true);
			componentPropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
			componentPropName.setDisplayName("<html><font color='green'>Concept property:");
			componentPropName.setShortDescription("Name of the property containing the concept. ");

			PropertyDescriptor rv[] = { componentPropName };
			return rv;
		} catch (IntrospectionException e) {
            throw new Error(e.toString());
		}
	}

	/**
	 * @see java.beans.BeanInfo#getBeanDescriptor()
	 */
	public BeanDescriptor getBeanDescriptor() {
		BeanDescriptor bd = new BeanDescriptor(AddRefsetMembersToListView.class);
		bd.setDisplayName("<html><font color='green'><center>Add Refset members to list view<br>");
		return bd;
	}
}
