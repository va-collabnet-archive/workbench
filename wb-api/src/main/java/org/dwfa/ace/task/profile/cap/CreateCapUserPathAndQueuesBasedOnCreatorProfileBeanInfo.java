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
package org.dwfa.ace.task.profile.cap;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.bpa.tasks.editor.PropertyNameLabelEditor;

public class CreateCapUserPathAndQueuesBasedOnCreatorProfileBeanInfo extends SimpleBeanInfo {

    public PropertyDescriptor[] getPropertyDescriptors() {
        try {

            PropertyDescriptor creatorProfilePropName = new PropertyDescriptor("creatorProfilePropName",
                getBeanDescriptor().getBeanClass());
            creatorProfilePropName.setBound(true);
            creatorProfilePropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            creatorProfilePropName.setDisplayName("<html><font color='green'>creator profile prop:");
            creatorProfilePropName.setShortDescription("The property that contains the creator's profile.");

            PropertyDescriptor newProfilePropName = new PropertyDescriptor("newProfilePropName",
                getBeanDescriptor().getBeanClass());
            newProfilePropName.setBound(true);
            newProfilePropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            newProfilePropName.setDisplayName("<html><font color='green'>new profile prop:");
            newProfilePropName.setShortDescription("The property that contains the new profile.");

            PropertyDescriptor errorsAndWarningsPropName = new PropertyDescriptor("errorsAndWarningsPropName",
                    getBeanDescriptor().getBeanClass());
                errorsAndWarningsPropName.setBound(true);
                errorsAndWarningsPropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
                errorsAndWarningsPropName.setDisplayName("<html><font color='green'>error & warn prop:");
                errorsAndWarningsPropName.setShortDescription("The property that contains errors and warnings found prior to commit.");

            PropertyDescriptor parentConceptForUserPropName = new PropertyDescriptor("parentConceptForUserPropName",
                    getBeanDescriptor().getBeanClass());
                parentConceptForUserPropName.setBound(true);
                parentConceptForUserPropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
                parentConceptForUserPropName.setDisplayName("<html><font color='green'>Parent of user concept prop:");
                parentConceptForUserPropName.setShortDescription("The property name to hold the parent concept for the concept representing the new user.");

            PropertyDescriptor parentConceptForPathPropName = new PropertyDescriptor("parentConceptForPathPropName",
                    getBeanDescriptor().getBeanClass());
                parentConceptForPathPropName.setBound(true);
                parentConceptForPathPropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
                parentConceptForPathPropName.setDisplayName("<html><font color='green'>Parent of user concept prop:");
                parentConceptForPathPropName.setShortDescription("The property name to hold the parent concept for the concept representing the new user's path.");

            PropertyDescriptor addToPathOriginPropName = new PropertyDescriptor("addToPathOriginPropName",
                    getBeanDescriptor().getBeanClass());
                addToPathOriginPropName.setBound(true);
                addToPathOriginPropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
                addToPathOriginPropName.setDisplayName("<html><font color='green'>Add to path origin prop:");
                addToPathOriginPropName.setShortDescription("The property name to hold add new dev path as origin to existing path.");

            PropertyDescriptor pathsForViewPropName = new PropertyDescriptor("pathsForViewPropName",
                    getBeanDescriptor().getBeanClass());
                pathsForViewPropName.setBound(true);
                pathsForViewPropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
                pathsForViewPropName.setDisplayName("<html><font color='green'>Edit paths prop:");
                pathsForViewPropName.setShortDescription("The property name to hold the view paths for new user.");

            PropertyDescriptor pathsForOriginPropName = new PropertyDescriptor("pathsForOriginPropName",
                    getBeanDescriptor().getBeanClass());
                pathsForOriginPropName.setBound(true);
                pathsForOriginPropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
                pathsForOriginPropName.setDisplayName("<html><font color='green'>Origin paths prop:");
                pathsForOriginPropName.setShortDescription("The property name to hold the origin paths for new user.");

            PropertyDescriptor releaseDatePropName = new PropertyDescriptor("releaseDatePropName",
                    getBeanDescriptor().getBeanClass());
            	releaseDatePropName.setBound(true);
                releaseDatePropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
                releaseDatePropName.setDisplayName("<html><font color='green'>Release date prop:");
                releaseDatePropName.setShortDescription("The property name to hold the release date for naming dev paths.");

            PropertyDescriptor rv[] = { creatorProfilePropName, newProfilePropName, errorsAndWarningsPropName, parentConceptForUserPropName, parentConceptForPathPropName, pathsForOriginPropName, pathsForOriginPropName, pathsForOriginPropName, releaseDatePropName };
            return rv;
        } catch (IntrospectionException e) {
            throw new Error(e.toString());
        }
    }

    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(CreateCapUserPathAndQueuesBasedOnCreatorProfile.class);
        bd.setDisplayName("<html><font color='green'><center>Create CAP User Path,<br>User Concept, <br>and User Queues<br>Based on Creator");
        return bd;
    }
}
