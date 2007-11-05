package org.dwfa.ace.task.data.checks;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.bpa.tasks.editor.PropertyNameLabelEditor;

public class VerifyValidIsADupsBeanInfo extends SimpleBeanInfo {

	public VerifyValidIsADupsBeanInfo() {
        super();
     }
	
    public PropertyDescriptor[] getPropertyDescriptors() {
        try {
            PropertyDescriptor activeConceptPropName =
            	new PropertyDescriptor("activeConceptPropName", getBeanDescriptor().getBeanClass());
            activeConceptPropName.setBound(true);
            activeConceptPropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            activeConceptPropName.setDisplayName("<html><font color='green'>Concept property:");
            activeConceptPropName.setShortDescription("Name of the property containing the concept. ");
                     
            PropertyDescriptor rv[] = { activeConceptPropName };
            return rv;
        } catch (IntrospectionException e) {
             throw new Error(e.toString());
        }
    }
    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(VerifyValidIsADups.class);
        bd.setDisplayName("<html><font color='green'><center>Verify duplicate concept is resolved");
        return bd;
    }

}//End class VerifyValidIsADupsBeanInfo