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
package org.ihtsdo.project.tasks;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.bpa.tasks.editor.PropertyNameLabelEditor;

/**
 * 
 */
public class GenerateUAWAssignmentBeanInfo extends SimpleBeanInfo {

    public GenerateUAWAssignmentBeanInfo() {
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
            
            PropertyDescriptor memberPropName =
            	new PropertyDescriptor("memberPropName", getBeanDescriptor().getBeanClass());
            memberPropName.setBound(true);
            memberPropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            memberPropName.setDisplayName("<html><font color='green'>member prop:");
            memberPropName.setShortDescription("[IN] The property that contains the member.");
            
            PropertyDescriptor processPropName =
            	new PropertyDescriptor("processPropName", getBeanDescriptor().getBeanClass());
            processPropName.setBound(true);
            processPropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            processPropName.setDisplayName("<html><font color='green'>process prop:");
            processPropName.setShortDescription("[IN] The property that contains the process.");
            
            PropertyDescriptor translatorInboxPropName =
            	new PropertyDescriptor("translatorInboxPropName", getBeanDescriptor().getBeanClass());
            translatorInboxPropName.setBound(true);
            translatorInboxPropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            translatorInboxPropName.setDisplayName("<html><font color='green'>translatorInboxPropName:");
            translatorInboxPropName.setShortDescription("translatorInboxPropName");
            
            PropertyDescriptor reviewer1InboxPropName =
            	new PropertyDescriptor("reviewer1InboxPropName", getBeanDescriptor().getBeanClass());
            reviewer1InboxPropName.setBound(true);
            reviewer1InboxPropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            reviewer1InboxPropName.setDisplayName("<html><font color='green'>reviewer1InboxPropName:");
            reviewer1InboxPropName.setShortDescription("reviewer1InboxPropName");
            
            PropertyDescriptor reviewer2InboxPropName =
            	new PropertyDescriptor("reviewer2InboxPropName", getBeanDescriptor().getBeanClass());
            reviewer2InboxPropName.setBound(true);
            reviewer2InboxPropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            reviewer2InboxPropName.setDisplayName("<html><font color='green'>reviewer2InboxPropName:");
            reviewer2InboxPropName.setShortDescription("reviewer2InboxPropName");
            
            PropertyDescriptor smeInboxPropName =
            	new PropertyDescriptor("smeInboxPropName", getBeanDescriptor().getBeanClass());
            smeInboxPropName.setBound(true);
            smeInboxPropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            smeInboxPropName.setDisplayName("<html><font color='green'>smeInboxPropName:");
            smeInboxPropName.setShortDescription("smeInboxPropName");
            
            PropertyDescriptor editorialBoardInboxPropName =
            	new PropertyDescriptor("editorialBoardInboxPropName", getBeanDescriptor().getBeanClass());
            editorialBoardInboxPropName.setBound(true);
            editorialBoardInboxPropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            editorialBoardInboxPropName.setDisplayName("<html><font color='green'>editorialBoardInboxPropName:");
            editorialBoardInboxPropName.setShortDescription("editorialBoardInboxPropName");
            
            PropertyDescriptor rv[] =
                    { profilePropName, memberPropName, processPropName, translatorInboxPropName, 
            		reviewer1InboxPropName, reviewer2InboxPropName, smeInboxPropName, editorialBoardInboxPropName };
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
        BeanDescriptor bd = new BeanDescriptor(GenerateUAWAssignment.class);
        bd.setDisplayName("<html><font color='green'><center>Generate UAW<br>assignment");
        return bd;
    }

}
