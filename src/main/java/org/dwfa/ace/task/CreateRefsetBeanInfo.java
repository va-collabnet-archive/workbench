package org.dwfa.ace.task;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.bpa.tasks.editor.PropertyNameLabelEditor;

public class CreateRefsetBeanInfo extends SimpleBeanInfo {

	public PropertyDescriptor[] getPropertyDescriptors() {
		try {
			PropertyDescriptor refsetName = new PropertyDescriptor(
					"refsetNamePropertyKey",
					CreateRefset.class);
			refsetName.setBound(true);
			refsetName.setPropertyEditorClass(PropertyNameLabelEditor.class);
			refsetName.setDisplayName("<html><font color='green'>Refset name:");
			refsetName.setShortDescription("Refset name");

			PropertyDescriptor rv[] = { refsetName };
			return rv;
		} catch (IntrospectionException e) {
			throw new Error(e.toString());
		}
	}

	public BeanDescriptor getBeanDescriptor() {
		BeanDescriptor bd = new BeanDescriptor(
				CreateRefset.class);
		bd.setDisplayName("<html><font color='green'><center>Create Refset");
		return bd;
	}

}
