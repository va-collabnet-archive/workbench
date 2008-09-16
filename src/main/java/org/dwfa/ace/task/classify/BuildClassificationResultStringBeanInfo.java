package org.dwfa.ace.task.classify;



import java.beans.BeanDescriptor;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.bpa.tasks.editor.PropertyNameLabelEditor;


/**c

 * @author Ming Zhang
 * 
 */
public class BuildClassificationResultStringBeanInfo extends SimpleBeanInfo {

	 public PropertyDescriptor[] getPropertyDescriptors() {
	        try {          
	        	PropertyDescriptor stringName =
	                new PropertyDescriptor("stringName", getBeanDescriptor().getBeanClass());
	            stringName.setBound(true);
	            stringName.setPropertyEditorClass(PropertyNameLabelEditor.class);
	            stringName.setDisplayName("<html><font color='green'>output string");
	            stringName.setShortDescription("Input String");
	            PropertyDescriptor rv[] ={stringName};
	            return rv;
	        } catch (Exception e) {
	             throw new Error(e.toString());
	        }
	     }        
	 public BeanDescriptor getBeanDescriptor() {
	        BeanDescriptor bd = new BeanDescriptor(BuildClassificationResultString.class);
	        bd.setDisplayName("<html><font color='green'><center>Build Classify result");
	        return bd;
	    }
}
