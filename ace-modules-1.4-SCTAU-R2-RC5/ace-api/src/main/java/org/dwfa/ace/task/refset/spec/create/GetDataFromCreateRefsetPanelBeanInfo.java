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
package org.dwfa.ace.task.refset.spec.create;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.ace.prop.editor.ConceptLabelPropEditor;
import org.dwfa.bpa.tasks.editor.PropertyNameLabelEditor;

/**
 * The GetRefreshRefsetSpecParamsPanelDataTaskBeanInfo class describes the visible elements of the
 * Workflow task GetRefreshRefsetSpecParamsPanelDataTask so that it can be displayed in the
 * Process Builder.
 * 
 * @author Perry Reid
 * @version 1.0, December 2009
 */
public class GetDataFromCreateRefsetPanelBeanInfo extends SimpleBeanInfo {

    public GetDataFromCreateRefsetPanelBeanInfo() {
        super();
    }

    /**
     * Returns a list of property descriptors for this task.
     * 
     * @return Returns a PropertyDescriptor array containing the properties of this task
     * @exception Error Thrown when an exception happens during Introspection
     */
    public PropertyDescriptor[] getPropertyDescriptors() {

        try {

            // The color "green" = denotes an [IN] property
            // The color "blue" = denotes an [OUT] property
            PropertyDescriptor profilePropName =
                    new PropertyDescriptor("profilePropName", getBeanDescriptor().getBeanClass());
            profilePropName.setBound(true);
            profilePropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            profilePropName.setDisplayName("<html><font color='green'>profile prop:");
            profilePropName.setShortDescription("[IN] The property that contains the working profile.");

            PropertyDescriptor refsetNamePropName;
            refsetNamePropName = new PropertyDescriptor("refsetNamePropName", getBeanDescriptor().getBeanClass());
            refsetNamePropName.setBound(true);
            refsetNamePropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            refsetNamePropName.setDisplayName("<html><font color='blue'>refset name prop name:");
            refsetNamePropName.setShortDescription("[OUT] The property to put the refset name into.");

            PropertyDescriptor refsetParentUuidPropName;
            refsetParentUuidPropName =
                    new PropertyDescriptor("refsetParentUuidPropName", getBeanDescriptor().getBeanClass());
            refsetParentUuidPropName.setBound(true);
            refsetParentUuidPropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            refsetParentUuidPropName.setDisplayName("<html><font color='blue'>refset parent uuid prop name:");
            refsetParentUuidPropName.setShortDescription("[OUT] The property to put the refset parent uuid into.");

            PropertyDescriptor statusTermEntry;
            statusTermEntry = new PropertyDescriptor("statusTermEntry", getBeanDescriptor().getBeanClass());
            statusTermEntry.setBound(true);
            statusTermEntry.setPropertyEditorClass(ConceptLabelPropEditor.class);
            statusTermEntry.setDisplayName("<html><font color='blue'>status concept to use:");
            statusTermEntry.setShortDescription("[OUT] The status concept to use.");

            PropertyDescriptor commentsPropName;
            commentsPropName = new PropertyDescriptor("commentsPropName", getBeanDescriptor().getBeanClass());
            commentsPropName.setBound(true);
            commentsPropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            commentsPropName.setDisplayName("<html><font color='blue'>comments prop name:");
            commentsPropName.setShortDescription("[OUT] The property to put the comments into.");

            PropertyDescriptor ownerUuidPropName;
            ownerUuidPropName = new PropertyDescriptor("ownerUuidPropName", getBeanDescriptor().getBeanClass());
            ownerUuidPropName.setBound(true);
            ownerUuidPropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            ownerUuidPropName.setDisplayName("<html><font color='blue'>owner uuid prop name:");
            ownerUuidPropName.setShortDescription("[OUT] The property to put the owner uuid into.");

            PropertyDescriptor requestorPropName;
            requestorPropName = new PropertyDescriptor("requestorPropName", getBeanDescriptor().getBeanClass());
            requestorPropName.setBound(true);
            requestorPropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            requestorPropName.setDisplayName("<html><font color='blue'>requestor prop name:");
            requestorPropName.setShortDescription("[OUT] The property to put the requestor name into.");

            PropertyDescriptor editorUuidPropName;
            editorUuidPropName = new PropertyDescriptor("editorUuidPropName", getBeanDescriptor().getBeanClass());
            editorUuidPropName.setBound(true);
            editorUuidPropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            editorUuidPropName.setDisplayName("<html><font color='blue'>editor uuid prop name:");
            editorUuidPropName.setShortDescription("[OUT] The property to put the editor uuid into.");

            PropertyDescriptor reviewerUuidPropName;
            reviewerUuidPropName = new PropertyDescriptor("reviewerUuidPropName", getBeanDescriptor().getBeanClass());
            reviewerUuidPropName.setBound(true);
            reviewerUuidPropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            reviewerUuidPropName.setDisplayName("<html><font color='blue'>reviewer uuid prop name:");
            reviewerUuidPropName.setShortDescription("[OUT] The property to put the reviewer uuid into.");

            PropertyDescriptor fileAttachmentsPropName;
            fileAttachmentsPropName =
                    new PropertyDescriptor("fileAttachmentsPropName", getBeanDescriptor().getBeanClass());
            fileAttachmentsPropName.setBound(true);
            fileAttachmentsPropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            fileAttachmentsPropName.setDisplayName("<html><font color='blue'>file attachments prop name:");
            fileAttachmentsPropName.setShortDescription("[OUT] The property to put the file attachments into.");

            PropertyDescriptor computeTypeUuidPropName;
            computeTypeUuidPropName =
                    new PropertyDescriptor("computeTypeUuidPropName", getBeanDescriptor().getBeanClass());
            computeTypeUuidPropName.setBound(true);
            computeTypeUuidPropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            computeTypeUuidPropName.setDisplayName("<html><font color='blue'>compute type uuid prop name:");
            computeTypeUuidPropName.setShortDescription("[OUT] The property to put the compute type uuid into.");

            PropertyDescriptor rv[] =
                    { profilePropName, refsetNamePropName, refsetParentUuidPropName, statusTermEntry, commentsPropName,
                     ownerUuidPropName, requestorPropName, editorUuidPropName, reviewerUuidPropName,
                     fileAttachmentsPropName, computeTypeUuidPropName };
            return rv;
        } catch (IntrospectionException e) {
            throw new Error(e.toString());
        }
    }

    /**
     * Return the descriptor for this JavaBean which contains a reference to the JavaBean
     * that implements this task as well as the display name of the task along with
     * formating information.
     * 
     * @see java.beans.BeanInfo#getBeanDescriptor()
     * @return Returns the BeanDescriptor for this task
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(GetDataFromCreateRefsetPanel.class);
        bd.setDisplayName("<html><font color='green'><center>Get Create Refset<br>Panel Data<br>from WFD Sheet");
        return bd;
    }

}
