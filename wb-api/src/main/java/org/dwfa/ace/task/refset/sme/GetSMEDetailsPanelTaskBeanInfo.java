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
package org.dwfa.ace.task.refset.sme;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.bpa.tasks.editor.PropertyNameLabelEditor;

/**
 * The GetSMEDetailsPanelTaskBeanInfo class describes the visible elements of the
 * Workflow task GetSMEDetailsPanelTask so that it can be displayed in the
 * Process Builder.
 * 
 */
public class GetSMEDetailsPanelTaskBeanInfo extends SimpleBeanInfo {

    public GetSMEDetailsPanelTaskBeanInfo() {
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

            PropertyDescriptor commentsPropName;
            commentsPropName = new PropertyDescriptor("commentsPropName", getBeanDescriptor().getBeanClass());
            commentsPropName.setBound(true);
            commentsPropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            commentsPropName.setDisplayName("<html><font color='blue'>comments prop name:");
            commentsPropName.setShortDescription("[OUT] The property to put the comments into.");

            PropertyDescriptor rv[] =
                    { profilePropName, ownerUuidPropName, ownerInboxPropName, refsetUuidPropName, commentsPropName };
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
        BeanDescriptor bd = new BeanDescriptor(GetSMEDetailsPanelTask.class);
        bd.setDisplayName("<html><font color='green'><center>Get SME details<br>from WFD Sheet");
        return bd;
    }

}
