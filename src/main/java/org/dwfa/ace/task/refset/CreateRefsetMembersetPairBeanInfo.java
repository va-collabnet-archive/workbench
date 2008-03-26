package org.dwfa.ace.task.refset;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.ace.task.NewConcept;
import org.dwfa.bpa.tasks.editor.PropertyNameLabelEditor;

public class CreateRefsetMembersetPairBeanInfo extends SimpleBeanInfo{
	public CreateRefsetMembersetPairBeanInfo() {
        super();
     }
    
    public PropertyDescriptor[] getPropertyDescriptors() {
    	try{
    		PropertyDescriptor propName =
    			new PropertyDescriptor("propName", CreateRefsetMembersetPair.class);
    		propName.setBound(true);
    		propName.setPropertyEditorClass(PropertyNameLabelEditor.class);
    		propName.setDisplayName("<html><font color='green'>Concept property:");
    		propName.setShortDescription("Name of the property containing the concept. ");
    	
    		PropertyDescriptor rv[] = {propName};
    		return rv;
    	} catch (IntrospectionException e) {
    		throw new Error(e.toString());
    	}
     }
    
    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(NewConcept.class);
        bd.setDisplayName("<html><font color='green'><center>Create New Refset/Memberset pair");
        return bd;
    }
}//End class CreateRefsetMembersetPairBeanInfo