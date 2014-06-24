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
package org.dwfa.ace.task.tracker;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.bpa.tasks.editor.PropertyNameLabelEditor;

/**
 * The GetTrackerDetailsPanelTaskBeanInfo class describes the visible elements of the
 * Workflow task GetTrackerDetailsPanelTaskBeanInfo so that it can be displayed in the
 * Process Builder.
 * 
 */
public class GetTrackerDetailsPanelTaskBeanInfo extends SimpleBeanInfo {

    public GetTrackerDetailsPanelTaskBeanInfo() {
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

            PropertyDescriptor artfIDPropName;
            artfIDPropName = new PropertyDescriptor("artfIDPropName", getBeanDescriptor().getBeanClass());
            artfIDPropName.setBound(true);
            artfIDPropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            artfIDPropName.setDisplayName("<html><font color='green'>owner uuid prop name:");
            artfIDPropName.setShortDescription("[IN] The property that contains the artf ID / tracker ID.");

            PropertyDescriptor name1PropName;
            name1PropName = new PropertyDescriptor("name1PropName", getBeanDescriptor().getBeanClass());
            name1PropName.setBound(true);
            name1PropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            name1PropName.setDisplayName("<html><font color='green'>member refset UUID prop:");
            name1PropName.setShortDescription("[IN] The property that contains the name1 string.");

            PropertyDescriptor name2PropName;
            name2PropName = new PropertyDescriptor("name2PropName", getBeanDescriptor().getBeanClass());
            name2PropName.setBound(true);
            name2PropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            name2PropName.setDisplayName("<html><font color='green'>comments prop name:");
            name2PropName.setShortDescription("[IN] The property that contains the name2 string.");

            PropertyDescriptor rv[] = { profilePropName, artfIDPropName, name1PropName, name2PropName };
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
        BeanDescriptor bd = new BeanDescriptor(GetTrackerDetailsPanelTask.class);
        bd.setDisplayName("<html><font color='green'><center>Get tracker details<br>from WFD Sheet");
        return bd;
    }

}
