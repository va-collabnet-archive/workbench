package org.dwfa.ace.task;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.bpa.tasks.editor.JTextFieldEditor;
import org.dwfa.bpa.tasks.editor.PropertyNameLabelEditor;

public class InstructAndWaitDoBeanInfo extends SimpleBeanInfo {
	   public PropertyDescriptor[] getPropertyDescriptors() {
	        try {  
	            PropertyDescriptor instruction =
	                new PropertyDescriptor("instruction", InstructAndWaitDo.class);
	            instruction.setBound(true);
	            instruction.setPropertyEditorClass(JTextFieldEditor.class);
	            instruction.setDisplayName("<html><font color='green'>Instruction:");
	            instruction.setShortDescription("Instructions to present to the user in the workflow panel. ");
	            
				PropertyDescriptor termPropName = new PropertyDescriptor(
						"termPropName", InstructAndWaitDo.class);
				termPropName.setBound(true);
				termPropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
				termPropName.setDisplayName("<html><font color='green'>Term:");
				termPropName.setShortDescription("Term");

	            PropertyDescriptor rv[] = { instruction, termPropName };
	            return rv;
	        } catch (IntrospectionException e) {
	             throw new Error(e.toString());
	        }
	    }        
	    /**
	     * @see java.beans.BeanInfo#getBeanDescriptor()
	     */
	    public BeanDescriptor getBeanDescriptor() {
	        BeanDescriptor bd = new BeanDescriptor(InstructAndWaitDo.class);
	        bd.setDisplayName("<html><font color='green'><center>Instruct and Wait<br>until Done");
	        return bd;
	    }
}
