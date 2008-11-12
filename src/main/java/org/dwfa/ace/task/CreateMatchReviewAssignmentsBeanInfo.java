package org.dwfa.ace.task;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.bpa.tasks.editor.PropertyNameLabelEditor;

/**
 * Bean info for CreateMatchReviewAssignments class.
 * 
 * @author Eric Mays (EKM)
 * 
 */
public class CreateMatchReviewAssignmentsBeanInfo extends SimpleBeanInfo {

	/**
     *
     */
	public CreateMatchReviewAssignmentsBeanInfo() {
		super();
	}

	public PropertyDescriptor[] getPropertyDescriptors() {
		try {
			PropertyDescriptor inputFileNamePropName = new PropertyDescriptor(
					"inputFileNamePropName", CreateMatchReviewAssignments.class);
			inputFileNamePropName.setBound(true);
			inputFileNamePropName
					.setPropertyEditorClass(PropertyNameLabelEditor.class);
			inputFileNamePropName
					.setDisplayName("<html><font color='green'>Input File Name:");
			inputFileNamePropName.setShortDescription("File Name");

			PropertyDescriptor bpFileNamePropName = new PropertyDescriptor(
					"bpFileNamePropName", CreateMatchReviewAssignments.class);
			bpFileNamePropName.setBound(true);
			bpFileNamePropName
					.setPropertyEditorClass(PropertyNameLabelEditor.class);
			bpFileNamePropName
					.setDisplayName("<html><font color='green'>BP Input File Name:");
			bpFileNamePropName.setShortDescription("File Name");

			PropertyDescriptor rv[] = { inputFileNamePropName,
					bpFileNamePropName };
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
				CreateMatchReviewAssignments.class);
		bd
				.setDisplayName("<html><font color='green'><center>Create Match Review <br> Assignments");
		return bd;
	}

}