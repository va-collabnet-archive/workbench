package org.dwfa.ace.task.refset.refresh;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.bpa.tasks.editor.PropertyNameLabelEditor;

/**
 * The GetRefreshRefsetSpecParamsPanelDataTaskBeanInfo class describes the visible elements of the 
 * Workflow task GetRefreshRefsetSpecParamsPanelDataTask so that it can be displayed in the 
 * Process Builder. 
 * 
 * @author  Perry Reid
 * @version 1.0, November 2009 
 */
public class GetRefreshRefsetSpecParamsPanelDataTaskBeanInfo extends SimpleBeanInfo {

     public GetRefreshRefsetSpecParamsPanelDataTaskBeanInfo() {
        super();
    }

	/**
	 * Returns a list of property descriptors for this task.   
	 * @return  	Returns a PropertyDescriptor array containing the properties of this task  
	 * @exception  	Error Thrown when an exception happens during Introspection
	 */
    public PropertyDescriptor[] getPropertyDescriptors() {

        try {

            PropertyDescriptor profilePropName =
                new PropertyDescriptor("profilePropName", getBeanDescriptor().getBeanClass());
            profilePropName.setBound(true);
            profilePropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            profilePropName.setDisplayName("<html><font color='green'>profile prop:");
            profilePropName.setShortDescription("The property that contains the working profile.");

            PropertyDescriptor nextUserTermEntryPropName;
            nextUserTermEntryPropName =
                    new PropertyDescriptor("nextUserTermEntryPropName", getBeanDescriptor().getBeanClass());
            nextUserTermEntryPropName.setBound(true);
            nextUserTermEntryPropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            nextUserTermEntryPropName.setDisplayName("<html><font color='green'>next person:");
            nextUserTermEntryPropName.setShortDescription("The next person the BP will go to.");

            PropertyDescriptor commentsPropName;
            commentsPropName = new PropertyDescriptor("commentsPropName", getBeanDescriptor().getBeanClass());
            commentsPropName.setBound(true);
            commentsPropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            commentsPropName.setDisplayName("<html><font color='green'>comments prop name:");
            commentsPropName.setShortDescription("The property to put the comments into.");

            PropertyDescriptor refsetUuidPropName;
            refsetUuidPropName = new PropertyDescriptor("refsetUuidPropName", getBeanDescriptor().getBeanClass());
            refsetUuidPropName.setBound(true);
            refsetUuidPropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            refsetUuidPropName.setDisplayName("<html><font color='green'>member refset UUID prop:");
            refsetUuidPropName.setShortDescription("The property to put the member refset UUID into.");

            PropertyDescriptor editorUuidPropName;
            editorUuidPropName = new PropertyDescriptor("editorUuidPropName", getBeanDescriptor().getBeanClass());
            editorUuidPropName.setBound(true);
            editorUuidPropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            editorUuidPropName.setDisplayName("<html><font color='green'>editor UUID prop name:");
            editorUuidPropName.setShortDescription("The property to put the editor UUID into.");

            PropertyDescriptor ownerUuidPropName;
            ownerUuidPropName = new PropertyDescriptor("ownerUuidPropName", getBeanDescriptor().getBeanClass());
            ownerUuidPropName.setBound(true);
            ownerUuidPropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            ownerUuidPropName.setDisplayName("<html><font color='green'>owner uuid prop name:");
            ownerUuidPropName.setShortDescription("The property to put the owner uuid into.");


            PropertyDescriptor rv[] =
                    { profilePropName, nextUserTermEntryPropName, commentsPropName, 
            		  refsetUuidPropName, editorUuidPropName, ownerUuidPropName };
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
        BeanDescriptor bd = new BeanDescriptor(GetRefreshRefsetSpecParamsPanelDataTask.class);
        bd.setDisplayName("<html><font color='green'><center>Get Refresh Refset Spec<br>Params panel data<br>from WFD Sheet");
        return bd;
    }

}
