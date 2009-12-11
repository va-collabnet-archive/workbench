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
 * The SetWFDSheetToRefreshRefsetSummaryPanelTaskBeanInfo class describes the visible elements of the 
 * Workflow task SetWFDSheetToRefreshRefsetSummaryPanelTask so that it can be displayed in the 
 * Process Builder. 
 * 
 * @author  Perry Reid
 * @version 1.0, November 2009 
 */
public class SetWFDSheetToRefreshRefsetSummaryPanelTaskBeanInfo extends
        SimpleBeanInfo {

    public SetWFDSheetToRefreshRefsetSummaryPanelTaskBeanInfo() {
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
                    new PropertyDescriptor("profilePropName",
                        getBeanDescriptor().getBeanClass());
            profilePropName.setBound(true);
            profilePropName
                .setPropertyEditorClass(PropertyNameLabelEditor.class);
            profilePropName
                .setDisplayName("<html><font color='green'>profile prop:");
            profilePropName
                .setShortDescription("[IN] The property that contains the working profile.");

            PropertyDescriptor refsetUuidPropName;
            refsetUuidPropName =
                    new PropertyDescriptor("refsetUuidPropName",
                        getBeanDescriptor().getBeanClass());
            refsetUuidPropName.setBound(true);
            refsetUuidPropName
                .setPropertyEditorClass(PropertyNameLabelEditor.class);
            refsetUuidPropName
                .setDisplayName("<html><font color='green'>member refset UUID prop:");
            refsetUuidPropName
                .setShortDescription("[IN] The property that contains the member refset UUID.");

            PropertyDescriptor ownerUuidPropName;
            ownerUuidPropName =
                    new PropertyDescriptor("ownerUuidPropName",
                        getBeanDescriptor().getBeanClass());
            ownerUuidPropName.setBound(true);
            ownerUuidPropName
                .setPropertyEditorClass(PropertyNameLabelEditor.class);
            ownerUuidPropName
                .setDisplayName("<html><font color='green'>owner uuid prop name:");
            ownerUuidPropName
                .setShortDescription("[IN] The property that contains the owner UUID.");

            PropertyDescriptor editorUuidPropName;
            editorUuidPropName =
                    new PropertyDescriptor("editorUuidPropName",
                        getBeanDescriptor().getBeanClass());
            editorUuidPropName.setBound(true);
            editorUuidPropName
                .setPropertyEditorClass(PropertyNameLabelEditor.class);
            editorUuidPropName
                .setDisplayName("<html><font color='green'>editor UUID prop name:");
            editorUuidPropName
                .setShortDescription("[IN] The property that contains the editor UUID.");

            PropertyDescriptor nextUserTermEntryPropName;
            nextUserTermEntryPropName =
                    new PropertyDescriptor("nextUserTermEntryPropName",
                        getBeanDescriptor().getBeanClass());
            nextUserTermEntryPropName.setBound(true);
            nextUserTermEntryPropName
                .setPropertyEditorClass(PropertyNameLabelEditor.class);
            nextUserTermEntryPropName
                .setDisplayName("<html><font color='green'>editor inbox address:");
            nextUserTermEntryPropName
                .setShortDescription("[IN] The property that contains the editor's inbox address.");

            PropertyDescriptor refsetSpecVersionPropName =
                    new PropertyDescriptor("refsetSpecVersionPropName",
                        getBeanDescriptor().getBeanClass());
            refsetSpecVersionPropName.setBound(true);
            refsetSpecVersionPropName
                .setPropertyEditorClass(PropertyNameLabelEditor.class);
            refsetSpecVersionPropName
                .setDisplayName("<html><font color='green'>Refset Spec version prop:");
            refsetSpecVersionPropName
                .setShortDescription("[IN] The property that contains the Refset Spec version.");

            PropertyDescriptor snomedVersionPropName =
                    new PropertyDescriptor("snomedVersionPropName",
                        getBeanDescriptor().getBeanClass());
            snomedVersionPropName.setBound(true);
            snomedVersionPropName
                .setPropertyEditorClass(PropertyNameLabelEditor.class);
            snomedVersionPropName
                .setDisplayName("<html><font color='green'>SNOMED version prop:");
            snomedVersionPropName
                .setShortDescription("[IN] The property that contains the SNOMED version.");

            PropertyDescriptor commentsPropName;
            commentsPropName =
                    new PropertyDescriptor("commentsPropName",
                        getBeanDescriptor().getBeanClass());
            commentsPropName.setBound(true);
            commentsPropName
                .setPropertyEditorClass(PropertyNameLabelEditor.class);
            commentsPropName
                .setDisplayName("<html><font color='green'>comments prop name:");
            commentsPropName
                .setShortDescription("[IN] The property that contains the comments.");

            PropertyDescriptor fileAttachmentsPropName;
            fileAttachmentsPropName =
                    new PropertyDescriptor("fileAttachmentsPropName",
                        getBeanDescriptor().getBeanClass());
            fileAttachmentsPropName.setBound(true);
            fileAttachmentsPropName
                .setPropertyEditorClass(PropertyNameLabelEditor.class);
            fileAttachmentsPropName
                .setDisplayName("<html><font color='green'>file attachments prop name:");
            fileAttachmentsPropName
                .setShortDescription("[IN] The property that contains the file attachments.");

            PropertyDescriptor editorCommentsPropName =
                    new PropertyDescriptor("editorCommentsPropName",
                        getBeanDescriptor().getBeanClass());
            editorCommentsPropName.setBound(true);
            editorCommentsPropName
                .setPropertyEditorClass(PropertyNameLabelEditor.class);
            editorCommentsPropName
                .setDisplayName("<html><font color='green'>editor comments prop name:");
            editorCommentsPropName
                .setShortDescription("[IN] The property that contains the editor's comments.");

            PropertyDescriptor rv[] =
                    { profilePropName, refsetUuidPropName, ownerUuidPropName,
                     editorUuidPropName, nextUserTermEntryPropName,
                     refsetSpecVersionPropName, snomedVersionPropName,
                     commentsPropName, fileAttachmentsPropName,
                     editorCommentsPropName };

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
        BeanDescriptor bd =
                new BeanDescriptor(
                    SetWFDSheetToRefreshRefsetSummaryPanelTask.class);
        bd
            .setDisplayName("<html><font color='green'><center>Set WFD Sheet to<br>Refresh Refset Spec<br>Summary panel");
        return bd;
    }

}
