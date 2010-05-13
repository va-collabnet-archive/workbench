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
package org.dwfa.ace.task.conflict;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.ace.prop.editor.ConceptLabelPropEditor;
import org.dwfa.bpa.tasks.editor.ConceptLabelEditor;
import org.dwfa.bpa.tasks.editor.PropertyNameLabelEditor;

public class PutCompletedConceptsWithConflictsInListViewBeanInfo extends SimpleBeanInfo {

    public PropertyDescriptor[] getPropertyDescriptors() {
        try {

            PropertyDescriptor profilePropName = new PropertyDescriptor("profilePropName",
                getBeanDescriptor().getBeanClass());
            profilePropName.setBound(true);
            profilePropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            profilePropName.setDisplayName("<html><font color='green'>profile prop:");
            profilePropName.setShortDescription("The property that contains the profile used to determine if conflicts are present.");

            PropertyDescriptor statusTermEntry = new PropertyDescriptor("statusTermEntry",
                getBeanDescriptor().getBeanClass());
            statusTermEntry.setBound(true);
            statusTermEntry.setPropertyEditorClass(ConceptLabelPropEditor.class);
            statusTermEntry.setDisplayName("completion status:");
            statusTermEntry.setShortDescription("<html><font color='green'>The status of the concept when an assignment is complete, and the concept is still active.");

            PropertyDescriptor retiredTermEntry = new PropertyDescriptor("retiredTermEntry",
                getBeanDescriptor().getBeanClass());
            retiredTermEntry.setBound(true);
            retiredTermEntry.setPropertyEditorClass(ConceptLabelPropEditor.class);
            retiredTermEntry.setDisplayName("retired status:");
            retiredTermEntry.setShortDescription("<html><font color='green'>The status of the concept when an assignment is complete, and the concept is flagged for retirement.");

            PropertyDescriptor rv[] = { profilePropName, statusTermEntry, retiredTermEntry };
            return rv;
        } catch (IntrospectionException e) {
            throw new Error(e.toString());
        }
    }

    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(PutCompletedConceptsWithConflictsInListView.class);
        bd.setDisplayName("<html><font color='green'><center>put completed concepts<br>with conflicts<br>in list view");
        return bd;
    }
}
