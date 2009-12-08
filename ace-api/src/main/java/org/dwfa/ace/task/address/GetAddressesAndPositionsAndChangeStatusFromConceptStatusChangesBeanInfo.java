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
package org.dwfa.ace.task.address;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.bpa.tasks.editor.PropertyNameLabelEditor;

public class GetAddressesAndPositionsAndChangeStatusFromConceptStatusChangesBeanInfo extends SimpleBeanInfo {

    public PropertyDescriptor[] getPropertyDescriptors() {
        try {
            PropertyDescriptor positionListPropName = new PropertyDescriptor("positionListPropName",
                getBeanDescriptor().getBeanClass());
            positionListPropName.setBound(true);
            positionListPropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            positionListPropName.setDisplayName("<html><font color='green'>Position list prop:");
            positionListPropName.setShortDescription("The property name to hold the list of positions.");

            PropertyDescriptor activeConceptPropName = new PropertyDescriptor("activeConceptPropName",
                getBeanDescriptor().getBeanClass());
            activeConceptPropName.setBound(true);
            activeConceptPropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            activeConceptPropName.setDisplayName("<html><font color='green'>concept prop:");
            activeConceptPropName.setShortDescription("The property name to hold the concept to check for status changes.");

            PropertyDescriptor profilePropName = new PropertyDescriptor("profilePropName",
                getBeanDescriptor().getBeanClass());
            profilePropName.setBound(true);
            profilePropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            profilePropName.setDisplayName("<html><font color='green'>profile prop:");
            profilePropName.setShortDescription("The profile containing the view paths and allowed status values to check for status changes.");

            PropertyDescriptor addressListPropName = new PropertyDescriptor("addressListPropName",
                getBeanDescriptor().getBeanClass());
            addressListPropName.setBound(true);
            addressListPropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            addressListPropName.setDisplayName("<html><font color='green'>address list prop:");
            addressListPropName.setShortDescription("The property name to hold the generated address list.");

            PropertyDescriptor statusValuePropName = new PropertyDescriptor("statusValuePropName",
                getBeanDescriptor().getBeanClass());
            statusValuePropName.setBound(true);
            statusValuePropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            statusValuePropName.setDisplayName("<html><font color='green'>new status prop:");
            statusValuePropName.setShortDescription("The property name containing the new status for the concepts.");

            PropertyDescriptor rv[] = { activeConceptPropName, profilePropName, addressListPropName,
                                       positionListPropName, statusValuePropName };
            return rv;
        } catch (IntrospectionException e) {
            throw new Error(e.toString());
        }
    }

    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(GetAddressesAndPositionsAndChangeStatusFromConceptStatusChanges.class);
        bd.setDisplayName("<html><font color='green'><center>get addresses<br>and positions<br>and change status<br>from status changes");
        return bd;
    }

}
