package org.dwfa.ace.task.refset.members;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.bpa.tasks.editor.PropertyNameLabelEditor;

public class UpdateParentMembersBeanInfo extends SimpleBeanInfo {

	public PropertyDescriptor[] getPropertyDescriptors() {
		try {
			
			PropertyDescriptor refsetConceptPropName = 
				new PropertyDescriptor("refsetConceptPropName", getBeanDescriptor().getBeanClass());
			refsetConceptPropName.setBound(true);
			refsetConceptPropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
			refsetConceptPropName.setDisplayName("<html><font color='green'>Refset concept property:");
			refsetConceptPropName.setShortDescription("The property containing the refset concept. ");

			PropertyDescriptor memberConceptPropName = 
				new PropertyDescriptor("memberConceptPropName", getBeanDescriptor().getBeanClass());
			memberConceptPropName.setBound(true);
			memberConceptPropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
			memberConceptPropName.setDisplayName("<html><font color='green'>Member concept property:");
			memberConceptPropName.setShortDescription("The property containing the target member concept. ");

			PropertyDescriptor conceptExtValuePropName = 
				new PropertyDescriptor("conceptExtValuePropName", getBeanDescriptor().getBeanClass());
			conceptExtValuePropName.setBound(true);
			conceptExtValuePropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
			conceptExtValuePropName.setDisplayName("<html><font color='green'>Extension value concept property:");
			conceptExtValuePropName.setShortDescription("The property containing the value for the new concept extension. ");
			
			PropertyDescriptor rv[] = { refsetConceptPropName, memberConceptPropName, conceptExtValuePropName };
			return rv;
		} catch (IntrospectionException e) {
            throw new Error(e.toString());
		}
	}

	/**
	 * @see java.beans.BeanInfo#getBeanDescriptor()
	 */
	public BeanDescriptor getBeanDescriptor() {
		BeanDescriptor bd = new BeanDescriptor(AddConceptChildrenToRefset.class);
		bd.setDisplayName("<html><font color='green'><center>Add concept children<br>to refset");
		return bd;
	}
	
}
