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
package org.dwfa.ace.task.wfdetailsSheet;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.bpa.tasks.editor.PropertyNameLabelEditor;

public class SetWorkflowDetailsSheetToGrantPanelWithSpecifiedUserBeanInfo extends SimpleBeanInfo {

    public PropertyDescriptor[] getPropertyDescriptors() {
        try {

            PropertyDescriptor commitProfilePropName =
                    new PropertyDescriptor("commitProfilePropName", getBeanDescriptor().getBeanClass());
            commitProfilePropName.setBound(true);
            commitProfilePropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            commitProfilePropName.setDisplayName("<html><font color='green'>commit profile prop:");
            commitProfilePropName.setShortDescription("The property that contains the commit profile.");

            PropertyDescriptor userUuidPropName =
                    new PropertyDescriptor("userUuidPropName", getBeanDescriptor().getBeanClass());
            userUuidPropName.setBound(true);
            userUuidPropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            userUuidPropName.setDisplayName("<html><font color='green'>user uuid prop:");
            userUuidPropName.setShortDescription("The property that contains the user uuid.");

            PropertyDescriptor profilePropName =
                    new PropertyDescriptor("profilePropName", getBeanDescriptor().getBeanClass());
            profilePropName.setBound(true);
            profilePropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            profilePropName.setDisplayName("<html><font color='green'>profile prop:");
            profilePropName.setShortDescription("The property that contains the working profile.");

            PropertyDescriptor rv[] = { commitProfilePropName, profilePropName, userUuidPropName };
            return rv;
        } catch (IntrospectionException e) {
            throw new Error(e.toString());
        }
    }

    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(SetWorkflowDetailsSheetToGrantPanelWithSpecifiedUser.class);
        bd
            .setDisplayName("<html><font color='green'><center>Set Workflow Details<br>Sheet to<br>Grant Panel<br>with specified user");
        return bd;
    }
}
