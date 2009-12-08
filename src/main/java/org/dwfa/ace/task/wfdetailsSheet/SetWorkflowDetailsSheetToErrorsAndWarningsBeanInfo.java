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

public class SetWorkflowDetailsSheetToErrorsAndWarningsBeanInfo extends SimpleBeanInfo {

    public PropertyDescriptor[] getPropertyDescriptors() {
        try {

            PropertyDescriptor errorsAndWarningsPropName = new PropertyDescriptor("errorsAndWarningsPropName",
                getBeanDescriptor().getBeanClass());
            errorsAndWarningsPropName.setBound(true);
            errorsAndWarningsPropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            errorsAndWarningsPropName.setDisplayName("<html><font color='green'>error set prop:");
            errorsAndWarningsPropName.setShortDescription("The property that contains the errors and warnings.");

            PropertyDescriptor profilePropName = new PropertyDescriptor("profilePropName",
                getBeanDescriptor().getBeanClass());
            profilePropName.setBound(true);
            profilePropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            profilePropName.setDisplayName("<html><font color='green'>profile prop:");
            profilePropName.setShortDescription("The property that contains the working profile.");

            PropertyDescriptor rv[] = { errorsAndWarningsPropName, profilePropName };
            return rv;
        } catch (IntrospectionException e) {
            throw new Error(e.toString());
        }
    }

    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(SetWorkflowDetailsSheetToErrorsAndWarnings.class);
        bd.setDisplayName("<html><font color='green'><center>Set Workflow Details<br>Sheet to<br>Errors and Warnings");
        return bd;
    }
}
