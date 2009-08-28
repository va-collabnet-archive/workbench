package org.dwfa.ace.task.refset.migrate;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.bpa.tasks.editor.PropertyNameLabelEditor;

public class RegenerateMarkedParentsBeanInfo extends SimpleBeanInfo {

	public PropertyDescriptor[] getPropertyDescriptors() {
		try {
			
			PropertyDescriptor memberConceptPropName = 
				new PropertyDescriptor("memberConceptPropName", getBeanDescriptor().getBeanClass());
			memberConceptPropName.setBound(true);
			memberConceptPropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
			memberConceptPropName.setDisplayName("<html><font color='green'>Refset member concept property:");
			memberConceptPropName.setShortDescription("The property containing the concept which defines a refset member. ");
			
			PropertyDescriptor rv[] = { memberConceptPropName };
			return rv;
		} catch (IntrospectionException e) {
            throw new Error(e.toString());
		}
	}

	/**
	 * @see java.beans.BeanInfo#getBeanDescriptor()
	 */
	public BeanDescriptor getBeanDescriptor() {
		BeanDescriptor bd = new BeanDescriptor(RegenerateMarkedParents.class);
		bd.setDisplayName("<html><font color='green'><center>Regenerate<br>marked parents");
		return bd;
	}
	
}
