package org.dwfa.ace.task.developer;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.ace.task.refset.CreateRefsetMembersetPair;
import org.dwfa.bpa.tasks.editor.PropertyNameLabelEditor;

public class ConceptFromNidBeanInfo extends SimpleBeanInfo{
	public ConceptFromNidBeanInfo() {
        super();
     }
    
    public PropertyDescriptor[] getPropertyDescriptors() {
    	try{
    		PropertyDescriptor propName =
    			new PropertyDescriptor("propName", ConceptFromNid.class);
    		propName.setBound(true);
    		propName.setPropertyEditorClass(PropertyNameLabelEditor.class);
    		propName.setDisplayName("<html><font color='green'>Nid property:");
    		propName.setShortDescription("Nid of the concept. ");
    		
    		PropertyDescriptor conceptPropName =
    			new PropertyDescriptor("conceptPropName", ConceptFromNid.class);
    		conceptPropName.setBound(true);
    		conceptPropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
    		conceptPropName.setDisplayName("<html><font color='green'>Concept property:");
    		conceptPropName.setShortDescription("Concept result. ");    	

    		PropertyDescriptor rv[] = {propName,conceptPropName};
    		return rv;
    	} catch (IntrospectionException e) {
    		throw new Error(e.toString());
    	}
     }
    
    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(ConceptFromNid.class);
        bd.setDisplayName("<html><font color='green'><center>Find a Concept from a NID");
        return bd;
    }
}//End class CreateRefsetMembersetPairBeanInfo