package org.dwfa.ace.task.refset.refresh;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.bpa.tasks.editor.PropertyNameLabelEditor;

/**
 * The GetSnomedVersionPanelDataTaskBeanInfo class describes the visible elements of the 
 * Workflow task GetSnomedVersionPanelDataTask so that it can be displayed in the 
 * Process Builder. 
 * 
 * @author  Perry Reid
 * @version 1.0, November 2009 
 */
public class GetSnomedVersionPanelDataTaskBeanInfo extends SimpleBeanInfo {


	/**
	 * Returns a list of property descriptors for this task.   
	 * @return  	Returns a PropertyDescriptor array containing the properties of this task  
	 * @exception  	Error Thrown when an exception happens during Introspection
	 */
	public PropertyDescriptor[] getPropertyDescriptors() {
         try {  

        	 // The color "green" = denotes an [IN] property 
        	 // The color "blue"  = denotes an [OUT] property 
             PropertyDescriptor profilePropName =
                 new PropertyDescriptor("profilePropName", getBeanDescriptor().getBeanClass());
             profilePropName.setBound(true);
             profilePropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
             profilePropName.setDisplayName("<html><font color='green'>profile prop:");
             profilePropName.setShortDescription("[IN] The property that contains the working profile.");

             PropertyDescriptor positionSetPropName =
                 new PropertyDescriptor("positionSetPropName", getBeanDescriptor().getBeanClass());
             positionSetPropName.setBound(true);
             positionSetPropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
             positionSetPropName.setDisplayName("<html><font color='blue'>SNOMED position set prop:");
             positionSetPropName.setShortDescription("[OUT] The property that will contain the SNOMED position set.");

            PropertyDescriptor rv[] =
                { profilePropName, positionSetPropName };
            return rv;
        } catch (IntrospectionException e) {
             throw new Error(e.toString());
        }
    }

	/** 
     * Return the descriptor for this JavaBean which contains a reference to the JavaBean 
	 * that implements this task as well as the display name of the task along with 
	 * formating information.
     * @see java.beans.BeanInfo#getBeanDescriptor()
	 * @return	Returns the BeanDescriptor for this task      
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(GetSnomedVersionPanelDataTask.class);
        bd.setDisplayName("<html><font color='green'><center>Get SNOMED Version<br>panel data from<br>WFD Sheet");
        return bd;
    }
}
