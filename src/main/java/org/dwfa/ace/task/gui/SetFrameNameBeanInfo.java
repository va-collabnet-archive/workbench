package org.dwfa.ace.task.gui;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.ace.task.address.AddElectronicAddress;
import org.dwfa.bpa.tasks.editor.JTextFieldEditor;
import org.dwfa.bpa.tasks.editor.PropertyNameLabelEditor;

public class SetFrameNameBeanInfo extends SimpleBeanInfo {
	   

	   public PropertyDescriptor[] getPropertyDescriptors() {
	       try {  
	           PropertyDescriptor profilePropName =
	               new PropertyDescriptor("profilePropName", getBeanDescriptor().getBeanClass());
	           profilePropName.setBound(true);
	           profilePropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
	           profilePropName.setDisplayName("<html><font color='green'>profile property:");
	           profilePropName.setShortDescription("The property containing the profile to change.");

	           PropertyDescriptor newFrameName =
	                new PropertyDescriptor("newFrameName", AddElectronicAddress.class);
	            newFrameName.setBound(true);
	            newFrameName.setPropertyEditorClass(JTextFieldEditor.class);
	            newFrameName.setDisplayName("<html><font color='green'>Name:");
	            newFrameName.setShortDescription("Enter the name for the new frame.");

	            PropertyDescriptor rv[] =
	               { profilePropName, newFrameName };
	           return rv;
	       } catch (IntrospectionException e) {
	            throw new Error(e.toString());
	       }
	    }        
	   /**
	    * @see java.beans.BeanInfo#getBeanDescriptor()
	    */
	   public BeanDescriptor getBeanDescriptor() {
	       BeanDescriptor bd = new BeanDescriptor(SetFrameName.class);
	       bd.setDisplayName("<html><font color='green'><center>Set Frame<br>Name");
	       return bd;
	   }
	}
