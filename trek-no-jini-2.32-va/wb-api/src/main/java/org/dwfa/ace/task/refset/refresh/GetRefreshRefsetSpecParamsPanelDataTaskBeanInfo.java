/**
 * Copyright (c) 2009 International Health Terminology Standards Development
 * Organisation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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

            PropertyDescriptor ownerUuidPropName;
            ownerUuidPropName = new PropertyDescriptor("ownerUuidPropName", getBeanDescriptor().getBeanClass());
            ownerUuidPropName.setBound(true);
            ownerUuidPropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            ownerUuidPropName.setDisplayName("<html><font color='blue'>owner UUID prop name:");
            ownerUuidPropName.setShortDescription("[OUT] The property to put the owner uuid into.");

            PropertyDescriptor ownerInboxPropName;
            ownerInboxPropName = new PropertyDescriptor("ownerInboxPropName", getBeanDescriptor().getBeanClass());
            ownerInboxPropName.setBound(true);
            ownerInboxPropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            ownerInboxPropName.setDisplayName("<html><font color='blue'>owner inbox prop name:");
            ownerInboxPropName.setShortDescription("[OUT] The property to put the owner's inbox address into.");

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

            PropertyDescriptor editorInboxPropName;
            editorInboxPropName =
                    new PropertyDescriptor("editorInboxPropName", getBeanDescriptor().getBeanClass());
            editorInboxPropName.setBound(true);
            editorInboxPropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            editorInboxPropName.setDisplayName("<html><font color='blue'>editor inbox prop name:");
            editorInboxPropName.setShortDescription("[OUT] The property to put the editor's inbox address into.");

            PropertyDescriptor reviewerUuidPropName;
            reviewerUuidPropName = new PropertyDescriptor("reviewerUuidPropName", getBeanDescriptor().getBeanClass());
            reviewerUuidPropName.setBound(true);
            reviewerUuidPropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            reviewerUuidPropName.setDisplayName("<html><font color='blue'>reviewer UUID prop name:");
            reviewerUuidPropName.setShortDescription("[OUT] The property to put the reviewer UUID into.");

            PropertyDescriptor reviewerInboxPropName;
            reviewerInboxPropName =
                    new PropertyDescriptor("reviewerInboxPropName", getBeanDescriptor().getBeanClass());
            reviewerInboxPropName.setBound(true);
            reviewerInboxPropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            reviewerInboxPropName.setDisplayName("<html><font color='blue'>reviewer inbox prop name:");
            reviewerInboxPropName.setShortDescription("[OUT] The property to put the reviewer's inbox address into.");

            PropertyDescriptor commentsPropName;
            commentsPropName = new PropertyDescriptor("commentsPropName", getBeanDescriptor().getBeanClass());
            commentsPropName.setBound(true);
            commentsPropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            commentsPropName.setDisplayName("<html><font color='blue'>comments prop name:");
            commentsPropName.setShortDescription("[OUT] The property to put the comments into.");

            PropertyDescriptor fileAttachmentsPropName;
            fileAttachmentsPropName = new PropertyDescriptor("fileAttachmentsPropName", getBeanDescriptor().getBeanClass());
            fileAttachmentsPropName.setBound(true);
            fileAttachmentsPropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            fileAttachmentsPropName.setDisplayName("<html><font color='blue'>file attachments prop name:");
            fileAttachmentsPropName.setShortDescription("[OUT] The property to put the file attachments into.");


            PropertyDescriptor rv[] =
                    { profilePropName, ownerUuidPropName, ownerInboxPropName, refsetUuidPropName, 
            		editorUuidPropName, editorInboxPropName, reviewerUuidPropName, reviewerInboxPropName, 
            		commentsPropName, fileAttachmentsPropName };
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
