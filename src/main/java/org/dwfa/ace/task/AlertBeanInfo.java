package org.dwfa.ace.task;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.bpa.tasks.editor.JTextFieldEditor;
import org.dwfa.bpa.tasks.editor.PropertyNameLabelEditor;

public class AlertBeanInfo extends SimpleBeanInfo {
	   public PropertyDescriptor[] getPropertyDescriptors() {
	        try {  
	            PropertyDescriptor alertText =
	                new PropertyDescriptor("alertText", getBeanDescriptor().getBeanClass());
	            alertText.setBound(true);
	            alertText.setPropertyEditorClass(JTextFieldEditor.class);
	            alertText.setDisplayName("<html><font color='green'>Alert Text:");
	            alertText.setShortDescription("Alert text to display to the user.");
	            
	            PropertyDescriptor alertTextProperty =
	                new PropertyDescriptor("alertTextProperty", getBeanDescriptor().getBeanClass());
	            alertTextProperty.setBound(true);
	            alertTextProperty.setPropertyEditorClass(PropertyNameLabelEditor.class);
	            alertTextProperty.setDisplayName("<html><font color='green'>Alert Text Property:");
	            alertTextProperty.setShortDescription("Text that is appended to the alert text.");
	            
	            PropertyDescriptor rv[] = { alertText, alertTextProperty };
	            return rv;
	        } catch (IntrospectionException e) {
	             throw new Error(e.toString());
	        }
	    }        
	    /**
	     * @see java.beans.BeanInfo#getBeanDescriptor()
	     */
	    public BeanDescriptor getBeanDescriptor() {
	        BeanDescriptor bd = new BeanDescriptor(Alert.class);
	        bd.setDisplayName("<html><font color='green'><center>Alert box");
	        return bd;
	    }

}
