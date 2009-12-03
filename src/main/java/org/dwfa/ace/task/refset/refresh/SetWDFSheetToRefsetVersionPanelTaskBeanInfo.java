package org.dwfa.ace.task.refset.refresh;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.bpa.tasks.editor.PropertyNameLabelEditor;

/**
 * The SetWDFSheetToRefsetVersionPanelTaskBeanInfo class describes the visible elements of the 
 * Workflow task SetWDFSheetToRefsetVersionPanelTask so that it can be displayed in the 
 * Process Builder. 
 * 
 * @author  Perry Reid
 * @version 1.0, November 2009 
 */
public class SetWDFSheetToRefsetVersionPanelTaskBeanInfo extends SimpleBeanInfo {

    public SetWDFSheetToRefsetVersionPanelTaskBeanInfo() {
        super();
    }

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
            profilePropName.setShortDescription("[IN] The property that will contain the current profile.");
            
            PropertyDescriptor refsetVersionPropName =
                new PropertyDescriptor("refsetVersionPropName", getBeanDescriptor().getBeanClass());
            refsetVersionPropName.setBound(true);
            refsetVersionPropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            refsetVersionPropName.setDisplayName("<html><font color='blue'>Refset position set prop:");
            refsetVersionPropName.setShortDescription("[OUT] The property that will contain the Refset version.");

            PropertyDescriptor refsetUuidPropName;
            refsetUuidPropName = new PropertyDescriptor("refsetUuidPropName", getBeanDescriptor().getBeanClass());
            refsetUuidPropName.setBound(true);
            refsetUuidPropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            refsetUuidPropName.setDisplayName("<html><font color='green'>member refset UUID prop:");
            refsetUuidPropName.setShortDescription("[IN] The property to put the member refset UUID into.");

           PropertyDescriptor rv[] =
               { profilePropName, refsetVersionPropName, refsetUuidPropName };

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
        BeanDescriptor bd = new BeanDescriptor(SetWDFSheetToRefsetVersionPanelTask.class);
        bd.setDisplayName("<html><font color='green'><center>Set WFD Sheet to<br>Refset Version panel");
        return bd;
    }

}


