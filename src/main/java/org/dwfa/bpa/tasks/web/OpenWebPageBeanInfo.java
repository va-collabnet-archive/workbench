package org.dwfa.bpa.tasks.web;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.bpa.tasks.editor.JTextFieldEditor;

public class OpenWebPageBeanInfo extends SimpleBeanInfo {
	/**
	 * 
	 */
	public OpenWebPageBeanInfo() {
		super();
	}

	public PropertyDescriptor[] getPropertyDescriptors() {
		try {
			PropertyDescriptor webURLString = new PropertyDescriptor(
					"webURLString", OpenWebPage.class);
			webURLString.setBound(true);
			webURLString.setPropertyEditorClass(JTextFieldEditor.class);
			webURLString.setDisplayName("Web Address");
			webURLString
					.setShortDescription("A webURL to open in the platform native web browser.");

			PropertyDescriptor rv[] = { webURLString };
			return rv;
		} catch (IntrospectionException e) {
			throw new Error(e.toString());
		}
	}

	/**
	 * @see java.beans.BeanInfo#getBeanDescriptor()
	 */
	public BeanDescriptor getBeanDescriptor() {
		BeanDescriptor bd = new BeanDescriptor(FetchFromWeb.class);
		bd.setDisplayName("<html><font color='green'><center>Open Web Page");
		return bd;
	}
}