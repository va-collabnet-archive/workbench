package org.dwfa.ace.task;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.bpa.tasks.editor.PropertyNameLabelEditor;

/**
 * Bean info for ReadMatchReviewItemFromAttachment class.
 * 
 * @author Eric Mays (EKM)
 * 
 */
public class ReadMatchReviewItemFromAttachmentBeanInfo extends SimpleBeanInfo {

	/**
     *
     */
	public ReadMatchReviewItemFromAttachmentBeanInfo() {
		super();
	}

	public PropertyDescriptor[] getPropertyDescriptors() {
		try {
			PropertyDescriptor uuidListListPropName = new PropertyDescriptor(
					"uuidListListPropName",
					ReadMatchReviewItemFromAttachment.class);
			uuidListListPropName.setBound(true);
			uuidListListPropName
					.setPropertyEditorClass(PropertyNameLabelEditor.class);
			uuidListListPropName
					.setDisplayName("<html><font color='green'>Uuid List:");
			uuidListListPropName.setShortDescription("Uuid list.");

			PropertyDescriptor inputFileNamePropName = new PropertyDescriptor(
					"inputFileNamePropName",
					ReadMatchReviewItemFromAttachment.class);
			inputFileNamePropName.setBound(true);
			inputFileNamePropName
					.setPropertyEditorClass(PropertyNameLabelEditor.class);
			inputFileNamePropName
					.setDisplayName("<html><font color='green'>Input File Name:");
			inputFileNamePropName.setShortDescription("File Name");

			PropertyDescriptor htmlPropName = new PropertyDescriptor(
					"htmlPropName", ReadMatchReviewItemFromAttachment.class);
			htmlPropName.setBound(true);
			htmlPropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
			htmlPropName.setDisplayName("<html><font color='green'>HTML:");
			htmlPropName.setShortDescription("HTML");

			PropertyDescriptor termPropName = new PropertyDescriptor(
					"termPropName", ReadMatchReviewItemFromAttachment.class);
			termPropName.setBound(true);
			termPropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
			termPropName.setDisplayName("<html><font color='green'>Term:");
			termPropName.setShortDescription("Term");

			PropertyDescriptor rv[] = { uuidListListPropName,
					inputFileNamePropName, htmlPropName, termPropName };
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
				ReadMatchReviewItemFromAttachment.class);
		bd
				.setDisplayName("<html><font color='green'><center>Read Match Review <br> Item From Attachment");
		return bd;
	}

}