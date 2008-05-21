package org.dwfa.ace.task.cs;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.bpa.tasks.editor.CheckboxEditor;
import org.dwfa.bpa.tasks.editor.JTextFieldEditor;

public class ConvertAllChangeSetsToXmlBeanInfo extends SimpleBeanInfo {
	   public PropertyDescriptor[] getPropertyDescriptors() {
	        try {  
	        	
	            PropertyDescriptor rootDirStr =
	                new PropertyDescriptor("rootDirStr", getBeanDescriptor().getBeanClass());
	            rootDirStr.setBound(true);
	            rootDirStr.setPropertyEditorClass(JTextFieldEditor.class);
	            rootDirStr.setDisplayName("<html><font color='green'>root dir:");
	            rootDirStr.setShortDescription("The directory root to search for change sets. ");
	            
	            PropertyDescriptor recurseSubdirectories =
	                new PropertyDescriptor("recurseSubdirectories", getBeanDescriptor().getBeanClass());
	            recurseSubdirectories.setBound(true);
	            recurseSubdirectories.setPropertyEditorClass(CheckboxEditor.class);
	            recurseSubdirectories.setDisplayName("<html><font color='green'>recurse:");
	            recurseSubdirectories.setShortDescription("Select if you want to recurse subdirectories. ");

	            PropertyDescriptor outputSuffix =
	                new PropertyDescriptor("outputSuffix", getBeanDescriptor().getBeanClass());
	            outputSuffix.setBound(true);
	            outputSuffix.setPropertyEditorClass(JTextFieldEditor.class);
	            outputSuffix.setDisplayName("<html><font color='green'>output file suffix:");
	            outputSuffix.setShortDescription("The generated output file suffix. ");
	            
	            PropertyDescriptor inputSuffix =
	                new PropertyDescriptor("inputSuffix", getBeanDescriptor().getBeanClass());
	            inputSuffix.setBound(true);
	            inputSuffix.setPropertyEditorClass(JTextFieldEditor.class);
	            inputSuffix.setDisplayName("<html><font color='green'>output file suffix:");
	            inputSuffix.setShortDescription("The input file suffix, used when scanning for files. ");
	            
	            PropertyDescriptor rv[] = { rootDirStr, recurseSubdirectories, outputSuffix, inputSuffix };
	            return rv;
	        } catch (IntrospectionException e) {
	             throw new Error(e.toString());
	        }
	    }        
	    /**
	     * @see java.beans.BeanInfo#getBeanDescriptor()
	     */
	    public BeanDescriptor getBeanDescriptor() {
	        BeanDescriptor bd = new BeanDescriptor(ConvertAllChangeSetsToXml.class);
	        bd.setDisplayName("<html><font color='green'><center>Convert all Change Sets<br>to XML");
	        return bd;
	    }
}