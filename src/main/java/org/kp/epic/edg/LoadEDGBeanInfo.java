package org.kp.epic.edg;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.bpa.tasks.editor.PropertyNameLabelEditor;

public class LoadEDGBeanInfo extends SimpleBeanInfo {

	public PropertyDescriptor[] getPropertyDescriptors() {
        PropertyDescriptor inputFilePropName;
		try {
			inputFilePropName = new PropertyDescriptor("inputFilePropName", getBeanDescriptor().getBeanClass());
			inputFilePropName.setBound(true);
			inputFilePropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
			inputFilePropName.setDisplayName("<html><font color='green'>File property:");
			inputFilePropName.setShortDescription("Name of the file to import from. ");

			PropertyDescriptor rv[] = { inputFilePropName };
			return rv;
		} catch (IntrospectionException e) {
            throw new Error(e.toString());
		}
	}

	/**
	 * @see java.beans.BeanInfo#getBeanDescriptor()
	 */
	public BeanDescriptor getBeanDescriptor() {
		BeanDescriptor bd = new BeanDescriptor(LoadEDG.class);
		bd.setDisplayName("<html><font color='green'><center>Load SNOMED EDG");
		return bd;
	}
}
