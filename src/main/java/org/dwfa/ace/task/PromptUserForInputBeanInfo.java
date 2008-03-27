package org.dwfa.ace.task;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.bpa.tasks.editor.JTextFieldEditor;
import org.dwfa.bpa.tasks.editor.PropertyNameLabelEditor;

public class PromptUserForInputBeanInfo extends SimpleBeanInfo {
	   public PropertyDescriptor[] getPropertyDescriptors() {
	        try {  
	            PropertyDescriptor instruction =
	                new PropertyDescriptor("instruction", PromptUserForInput.class);
	            instruction.setBound(true);
	            instruction.setPropertyEditorClass(JTextFieldEditor.class);
	            instruction.setDisplayName("<html><font color='green'>Instruction:");
	            instruction.setShortDescription("Instructions to present to the user in the workflow panel. ");
	            
	            PropertyDescriptor newRefsetPropName =
	                new PropertyDescriptor("newRefsetPropName", getBeanDescriptor().getBeanClass());
	            newRefsetPropName.setBound(true);
	            newRefsetPropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
	            newRefsetPropName.setDisplayName("<html><font color='green'>refset Name:");
	            newRefsetPropName.setShortDescription("The property to put the refset name into.");
	            
	            PropertyDescriptor rv[] = { instruction, newRefsetPropName };
	            return rv;
	        } catch (IntrospectionException e) {
	             throw new Error(e.toString());
	        }
	    }        
	    /**
	     * @see java.beans.BeanInfo#getBeanDescriptor()
	     */
	    public BeanDescriptor getBeanDescriptor() {
	        BeanDescriptor bd = new BeanDescriptor(PromptUserForInput.class);
	        bd.setDisplayName("<html><font color='green'><center>Prompt user for input");
	        return bd;
	    }
}//End class 