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
package org.dwfa.ace.task.commit;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.bpa.tasks.editor.CheckboxEditor;
import org.dwfa.bpa.tasks.editor.PropertyNameLabelEditor;

public class TestForUneditedDefaultsBeanInfo extends SimpleBeanInfo {

    public PropertyDescriptor[] getPropertyDescriptors() {
        try {
            PropertyDescriptor showAlertOnFailure = new PropertyDescriptor("showAlertOnFailure",
                getBeanDescriptor().getBeanClass());
            showAlertOnFailure.setBound(true);
            showAlertOnFailure.setPropertyEditorClass(CheckboxEditor.class);
            showAlertOnFailure.setDisplayName("<html><font color='green'>Show alerts:");
            showAlertOnFailure.setShortDescription("Show alerts on failure...");

            PropertyDescriptor forCommit = new PropertyDescriptor("forCommit", getBeanDescriptor().getBeanClass());
            forCommit.setBound(true);
            forCommit.setPropertyEditorClass(CheckboxEditor.class);
            forCommit.setDisplayName("<html><font color='green'>Show alerts:");
            forCommit.setShortDescription("Show alerts on failure...");

            PropertyDescriptor profilePropName = new PropertyDescriptor("profilePropName",
                getBeanDescriptor().getBeanClass());
            profilePropName.setBound(true);
            profilePropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            profilePropName.setDisplayName("<html><font color='green'>profile prop:");
            profilePropName.setShortDescription("The property that contains the profile.");

            PropertyDescriptor componentPropName = new PropertyDescriptor("componentPropName",
                getBeanDescriptor().getBeanClass());
            componentPropName.setBound(true);
            componentPropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            componentPropName.setDisplayName("<html><font color='green'>component prop:");
            componentPropName.setShortDescription("The property that contains the component to test.");

            PropertyDescriptor rv[] = { showAlertOnFailure, forCommit, profilePropName, componentPropName };
            return rv;
        } catch (IntrospectionException e) {
            throw new Error(e.toString());
        }
    }

    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(TestForUneditedDefaults.class);
        bd.setDisplayName("<html><font color='green'><center>Test For<br>Unedited Defaults");
        return bd;
    }
}
