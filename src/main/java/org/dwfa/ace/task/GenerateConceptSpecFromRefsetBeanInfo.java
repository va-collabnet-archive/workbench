package org.dwfa.ace.task;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.bpa.tasks.editor.JTextFieldEditor;
import org.dwfa.bpa.tasks.editor.PropertyNameLabelEditor;

public class GenerateConceptSpecFromRefsetBeanInfo extends SimpleBeanInfo {
	
	public PropertyDescriptor[] getPropertyDescriptors() {
		try {
			PropertyDescriptor packageName = new PropertyDescriptor(
					"packageNamePropertyKey", GenerateConceptSpecFromRefset.class);
			packageName.setBound(true);
			packageName.setPropertyEditorClass(PropertyNameLabelEditor.class);
			packageName
					.setDisplayName("<html><font color='green'>Package name:");
			packageName.setShortDescription("Jave package name");

			PropertyDescriptor className = new PropertyDescriptor("classNamePropertyKey",
					GenerateConceptSpecFromRefset.class);
			className.setBound(true);
			className.setPropertyEditorClass(PropertyNameLabelEditor.class);
			className.setDisplayName("<html><font color='green'>Class name:");
			className.setShortDescription("Jave class name");

			PropertyDescriptor outputDirectory = new PropertyDescriptor(
					"outputDirectoryPropertyKey", GenerateConceptSpecFromRefset.class);
			outputDirectory.setBound(true);
			outputDirectory.setPropertyEditorClass(PropertyNameLabelEditor.class);
			outputDirectory
					.setDisplayName("<html><font color='green'>Output directory:");
			outputDirectory
					.setShortDescription("Output directory for generated class");

			PropertyDescriptor rv[] = { packageName, className, outputDirectory };
			return rv;
		} catch (IntrospectionException e) {
			throw new Error(e.toString());
		}
	}

	public BeanDescriptor getBeanDescriptor() {
		BeanDescriptor bd = new BeanDescriptor(GenerateConceptSpecFromRefset.class);
		bd
				.setDisplayName("<html><font color='green'><center>Generate Concept Spec<br>from Refset");
		return bd;
	}

}
