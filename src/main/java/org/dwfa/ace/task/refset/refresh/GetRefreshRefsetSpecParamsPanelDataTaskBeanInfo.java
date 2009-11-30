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

        	// The color "green" = denotes an [IN] property 
        	// The color "blue"  = denotes an [OUT] property 
            PropertyDescriptor profilePropName =
                new PropertyDescriptor("profilePropName", getBeanDescriptor().getBeanClass());
            profilePropName.setBound(true);
            profilePropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            profilePropName.setDisplayName("<html><font color='green'>profile prop:");
            profilePropName.setShortDescription("[IN] The property that contains the working profile.");

            PropertyDescriptor nextUserTermEntryPropName;
            nextUserTermEntryPropName =
                    new PropertyDescriptor("nextUserTermEntryPropName", getBeanDescriptor().getBeanClass());
            nextUserTermEntryPropName.setBound(true);
            nextUserTermEntryPropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            nextUserTermEntryPropName.setDisplayName("<html><font color='blue'>editor inbox prop name:");
            nextUserTermEntryPropName.setShortDescription("[OUT] The property to put the editor's inbox address into.");

            PropertyDescriptor commentsPropName;
            commentsPropName = new PropertyDescriptor("commentsPropName", getBeanDescriptor().getBeanClass());
            commentsPropName.setBound(true);
            commentsPropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            commentsPropName.setDisplayName("<html><font color='blue'>comments prop name:");
            commentsPropName.setShortDescription("[OUT] The property to put the comments into.");

            PropertyDescriptor refsetUuidPropName;
            refsetUuidPropName = new PropertyDescriptor("refsetUuidPropName", getBeanDescriptor().getBeanClass());
            refsetUuidPropName.setBound(true);
            refsetUuidPropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            refsetUuidPropName.setDisplayName("<html><font color='blue'>member refset UUID prop:");
            refsetUuidPropName.setShortDescription("[OUT] The property to put the member refset UUID into.");

            PropertyDescriptor editorUuidPropName;
            editorUuidPropName = new PropertyDescriptor("editorUuidPropName", getBeanDescriptor().getBeanClass());
            editorUuidPropName.setBound(true);
            editorUuidPropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            editorUuidPropName.setDisplayName("<html><font color='blue'>editor UUID prop name:");
            editorUuidPropName.setShortDescription("[OUT] The property to put the editor UUID into.");

            PropertyDescriptor ownerUuidPropName;
            ownerUuidPropName = new PropertyDescriptor("ownerUuidPropName", getBeanDescriptor().getBeanClass());
            ownerUuidPropName.setBound(true);
            ownerUuidPropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            ownerUuidPropName.setDisplayName("<html><font color='blue'>owner UUID prop name:");
            ownerUuidPropName.setShortDescription("[OUT] The property to put the owner uuid into.");

            PropertyDescriptor fileAttachmentsPropName;
            fileAttachmentsPropName = new PropertyDescriptor("fileAttachmentsPropName", getBeanDescriptor().getBeanClass());
            fileAttachmentsPropName.setBound(true);
            fileAttachmentsPropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            fileAttachmentsPropName.setDisplayName("<html><font color='blue'>file attachments prop name:");
            fileAttachmentsPropName.setShortDescription("[OUT] The property to put the file attachments into.");

            PropertyDescriptor ownerInboxPropName;
            ownerInboxPropName = new PropertyDescriptor("ownerInboxPropName", getBeanDescriptor().getBeanClass());
            ownerInboxPropName.setBound(true);
            ownerInboxPropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            ownerInboxPropName.setDisplayName("<html><font color='blue'>owner inbox prop name:");
            ownerInboxPropName.setShortDescription("[OUT] The property to put the owner's inbox address into.");


            PropertyDescriptor rv[] =
                    { profilePropName, nextUserTermEntryPropName, commentsPropName, 
            		  refsetUuidPropName, editorUuidPropName, ownerUuidPropName, fileAttachmentsPropName, ownerInboxPropName};
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
