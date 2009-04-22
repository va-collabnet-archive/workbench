package org.dwfa.ace.task.refset.members;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.bpa.tasks.editor.PropertyNameLabelEditor;

public class AddListViewConceptsToRefsetBeanInfo extends SimpleBeanInfo {

	public PropertyDescriptor[] getPropertyDescriptors() {
		try {
			
			PropertyDescriptor refsetConceptPropName = 
				new PropertyDescriptor("refsetConceptPropName", getBeanDescriptor().getBeanClass());
			refsetConceptPropName.setBound(true);
			refsetConceptPropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
			refsetConceptPropName.setDisplayName("<html><font color='green'>Refset concept property:");
			refsetConceptPropName.setShortDescription("The property containing the refset concept. ");

			PropertyDescriptor conceptExtValuePropName = 
				new PropertyDescriptor("conceptExtValuePropName", getBeanDescriptor().getBeanClass());
			conceptExtValuePropName.setBound(true);
			conceptExtValuePropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
			conceptExtValuePropName.setDisplayName("<html><font color='green'>Extension value concept property:");
			conceptExtValuePropName.setShortDescription("The property containing the value for the new concept extension. ");
			
			PropertyDescriptor rv[] = { refsetConceptPropName, conceptExtValuePropName };
			return rv;
		} catch (IntrospectionException e) {
            throw new Error(e.toString());
		}
	}

	/**
	 * @see java.beans.BeanInfo#getBeanDescriptor()
	 */
	public BeanDescriptor getBeanDescriptor() {
		BeanDescriptor bd = new BeanDescriptor(AddListViewConceptsToRefset.class);
		bd.setDisplayName("<html><font color='green'><center>Add concepts from<br>list view to refset");
		return bd;
	}
	
}
