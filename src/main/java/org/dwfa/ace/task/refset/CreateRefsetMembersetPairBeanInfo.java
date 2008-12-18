package org.dwfa.ace.task.refset;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

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

            PropertyDescriptor altIsAUuid =
    			new PropertyDescriptor("altIsA", CreateRefsetMembersetPair.class);
    		altIsAUuid.setBound(true);
    		altIsAUuid.setPropertyEditorClass(PropertyNameLabelEditor.class);
    		altIsAUuid.setDisplayName("<html><font color='green'>Alt IS_A (optional):");
    		altIsAUuid.setShortDescription("An alternate 'is a' relationship UUID (default = SNOMED 'Is a')");
    	
    		PropertyDescriptor rv[] = {propName, altIsAUuid};
    		return rv;
    	} catch (IntrospectionException e) {
    		throw new Error(e.toString());
    	}
     }
    
    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(CreateRefsetMembersetPair.class);
        bd.setDisplayName("<html><font color='green'><center>Create New<br>Refset/Memberset pair");
        return bd;
    }
}//End class CreateRefsetMembersetPairBeanInfo