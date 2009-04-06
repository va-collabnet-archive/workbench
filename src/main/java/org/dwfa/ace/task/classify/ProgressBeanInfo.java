package org.dwfa.ace.task.classify;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.bpa.tasks.editor.JTextFieldEditor;

public class ProgressBeanInfo extends SimpleBeanInfo {

	public PropertyDescriptor[] getPropertyDescriptors() {
		try {
			PropertyDescriptor progress = new PropertyDescriptor("progress",
					Progress.class);
			progress.setBound(true);
			progress.setPropertyEditorClass(JTextFieldEditor.class);
			progress
					.setDisplayName("<html><font color='green'>Progress (max 100):");
			progress.setShortDescription("Enter progress. min = 0, max = 100");
			PropertyDescriptor rv[] = { progress };
			return rv;
		} catch (IntrospectionException e) {
			throw new Error(e.toString());
		}
	}

	public BeanDescriptor getBeanDescriptor() {
		BeanDescriptor bd = new BeanDescriptor(Progress.class);
		bd.setDisplayName("<html><font color='green'><center>Progress");
		return bd;
	}

}