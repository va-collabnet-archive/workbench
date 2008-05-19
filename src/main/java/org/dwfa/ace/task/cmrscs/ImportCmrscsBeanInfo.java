package org.dwfa.ace.task.cmrscs;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.bpa.tasks.editor.JTextFieldEditor;

public class ImportCmrscsBeanInfo extends SimpleBeanInfo {
	   public PropertyDescriptor[] getPropertyDescriptors() {
	        try {  
	            PropertyDescriptor rootDirStr =
	                new PropertyDescriptor("rootDirStr", getBeanDescriptor().getBeanClass());
	            rootDirStr.setBound(true);
	            rootDirStr.setPropertyEditorClass(JTextFieldEditor.class);
	            rootDirStr.setDisplayName("<html><font color='green'>root dir:");
	            rootDirStr.setShortDescription("The directory root to search for CMRSCS change sets. ");
	            	            
	            PropertyDescriptor rv[] = { rootDirStr };
	            return rv;
	        } catch (IntrospectionException e) {
	             throw new Error(e.toString());
	        }
	    }        
	    /**
	     * @see java.beans.BeanInfo#getBeanDescriptor()
	     */
	    public BeanDescriptor getBeanDescriptor() {
	        BeanDescriptor bd = new BeanDescriptor(ImportCmrscs.class);
	        bd.setDisplayName("<html><font color='green'><center>Import All<br>CMRSCS Change Sets");
	        return bd;
	    }
}