package org.dwfa.ace.task.cs;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.bpa.tasks.editor.JTextFieldEditor;

public class ConvertChangeSetToXmlBeanInfo extends SimpleBeanInfo {
	   public PropertyDescriptor[] getPropertyDescriptors() {
	        try {  
	        	
	            PropertyDescriptor filename =
	                new PropertyDescriptor("filename", getBeanDescriptor().getBeanClass());
	            filename.setBound(true);
	            filename.setPropertyEditorClass(JTextFieldEditor.class);
	            filename.setDisplayName("<html><font color='green'>changeset:");
	            filename.setShortDescription("The file to convert. ");
	            
	            PropertyDescriptor outputSuffix =
	                new PropertyDescriptor("outputSuffix", getBeanDescriptor().getBeanClass());
	            outputSuffix.setBound(true);
	            outputSuffix.setPropertyEditorClass(JTextFieldEditor.class);
	            outputSuffix.setDisplayName("<html><font color='green'>output file suffix:");
	            outputSuffix.setShortDescription("The generated output file suffix. ");
	            
	            PropertyDescriptor rv[] = { filename, outputSuffix };
	            return rv;
	        } catch (IntrospectionException e) {
	             throw new Error(e.toString());
	        }
	    }        
	    /**
	     * @see java.beans.BeanInfo#getBeanDescriptor()
	     */
	    public BeanDescriptor getBeanDescriptor() {
	        BeanDescriptor bd = new BeanDescriptor(ConvertChangeSetToXml.class);
	        bd.setDisplayName("<html><font color='green'><center>Convert Change Set<br>to XML");
	        return bd;
	    }
}