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
package org.ihtsdo.translation.tasks;

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
 * @author Perry Reid
 * @version 1.0, December 2009
 */
public class GetDataFromWFUserSelectionPanelBeanInfo extends SimpleBeanInfo {

    public GetDataFromWFUserSelectionPanelBeanInfo() {
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
            
            PropertyDescriptor processPropName =
            	new PropertyDescriptor("processPropName", getBeanDescriptor().getBeanClass());
            processPropName.setBound(true);
            processPropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            processPropName.setDisplayName("<html><font color='green'>process prop:");
            processPropName.setShortDescription("process prop");
            
            PropertyDescriptor worklistNamePropName =
            	new PropertyDescriptor("worklistNamePropName", getBeanDescriptor().getBeanClass());
            worklistNamePropName.setBound(true);
            worklistNamePropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            worklistNamePropName.setDisplayName("<html><font color='green'>worklist name prop:");
            worklistNamePropName.setShortDescription("worklist name prop");

            PropertyDescriptor translatorInboxPropName =
            	new PropertyDescriptor("translatorInboxPropName", getBeanDescriptor().getBeanClass());
            translatorInboxPropName.setBound(true);
            translatorInboxPropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            translatorInboxPropName.setDisplayName("<html><font color='green'>translatorInboxPropName:");
            translatorInboxPropName.setShortDescription("translatorInboxPropName");
            
            PropertyDescriptor fastTrackTranslatorInboxPropName =
            	new PropertyDescriptor("fastTrackTranslatorInboxPropName", getBeanDescriptor().getBeanClass());
            fastTrackTranslatorInboxPropName.setBound(true);
            fastTrackTranslatorInboxPropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            fastTrackTranslatorInboxPropName.setDisplayName("<html><font color='green'>fastTrackTranslatorInboxPropName:");
            fastTrackTranslatorInboxPropName.setShortDescription("fastTrackTranslatorInboxPropName");
            
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
            
            PropertyDescriptor superSmeInboxPropName =
            	new PropertyDescriptor("superSmeInboxPropName", getBeanDescriptor().getBeanClass());
            superSmeInboxPropName.setBound(true);
            superSmeInboxPropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            superSmeInboxPropName.setDisplayName("<html><font color='green'>superSmeInboxPropName:");
            superSmeInboxPropName.setShortDescription("superSmeInboxPropName");
            
            PropertyDescriptor editorialBoardInboxPropName =
            	new PropertyDescriptor("editorialBoardInboxPropName", getBeanDescriptor().getBeanClass());
            editorialBoardInboxPropName.setBound(true);
            editorialBoardInboxPropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            editorialBoardInboxPropName.setDisplayName("<html><font color='green'>editorialBoardInboxPropName:");
            editorialBoardInboxPropName.setShortDescription("editorialBoardInboxPropName");
            

            PropertyDescriptor rv[] =
                    { profilePropName, processPropName, worklistNamePropName, translatorInboxPropName, 
            		fastTrackTranslatorInboxPropName,
            		reviewer1InboxPropName, reviewer2InboxPropName, 
            		smeInboxPropName, superSmeInboxPropName, editorialBoardInboxPropName};
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
        BeanDescriptor bd = new BeanDescriptor(GetDataFromWFUserSelectionPanel.class);
        bd.setDisplayName("<html><font color='green'><center>Get WF User<br>Selection Data<br>from WFD Sheet");
        return bd;
    }

}
