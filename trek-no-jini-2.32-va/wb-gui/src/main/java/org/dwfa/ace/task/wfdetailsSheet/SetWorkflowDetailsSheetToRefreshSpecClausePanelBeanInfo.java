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

public class SetWorkflowDetailsSheetToRefreshSpecClausePanelBeanInfo extends SimpleBeanInfo {

    public PropertyDescriptor[] getPropertyDescriptors() {
        try {

            PropertyDescriptor refsetPositionSetPropName = new PropertyDescriptor("refsetPositionSetPropName",
                getBeanDescriptor().getBeanClass());
            refsetPositionSetPropName.setBound(true);
            refsetPositionSetPropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            refsetPositionSetPropName.setDisplayName("<html><font color='green'>refset position set prop:");
            refsetPositionSetPropName.setShortDescription("The property that contains the refset position set.");

            PropertyDescriptor refsetUuidPropName = new PropertyDescriptor("refsetUuidPropName",
                getBeanDescriptor().getBeanClass());
            refsetUuidPropName.setBound(true);
            refsetUuidPropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            refsetUuidPropName.setDisplayName("<html><font color='green'>refset uuid prop:");
            refsetUuidPropName.setShortDescription("The property that contains the uuid of the refset spec being updated.");

            PropertyDescriptor snomedPositionSetPropName = new PropertyDescriptor("snomedPositionSetPropName",
                getBeanDescriptor().getBeanClass());
            snomedPositionSetPropName.setBound(true);
            snomedPositionSetPropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            snomedPositionSetPropName.setDisplayName("<html><font color='green'>SNOMED position set prop:");
            snomedPositionSetPropName.setShortDescription("The property that contains the SNOMED position set.");

            PropertyDescriptor clauseToUpdateMemberUuidPropName = new PropertyDescriptor(
                "clausesToUpdateMemberUuidPropName", getBeanDescriptor().getBeanClass());
            clauseToUpdateMemberUuidPropName.setBound(true);
            clauseToUpdateMemberUuidPropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            clauseToUpdateMemberUuidPropName.setDisplayName("<html><font color='green'>clauses prop:");
            clauseToUpdateMemberUuidPropName.setShortDescription("The property that contains the List<Collectin<UUID>> of member uuids of the clauses being updated.");

            PropertyDescriptor profilePropName = new PropertyDescriptor("profilePropName",
                getBeanDescriptor().getBeanClass());
            profilePropName.setBound(true);
            profilePropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            profilePropName.setDisplayName("<html><font color='green'>profile prop:");
            profilePropName.setShortDescription("The property that contains the working profile.");

            PropertyDescriptor rv[] = { refsetUuidPropName, refsetPositionSetPropName, snomedPositionSetPropName,
                                       clauseToUpdateMemberUuidPropName, profilePropName };
            return rv;
        } catch (IntrospectionException e) {
            throw new Error(e.toString());
        }
    }

    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(SetWorkflowDetailsSheetToRefreshSpecClausePanel.class);
        bd.setDisplayName("<html><font color='green'><center>Set Workflow Details<br>Sheet to<br>Refresh Spec<br>Clause Panel");
        return bd;
    }
}
