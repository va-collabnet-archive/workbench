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
package org.dwfa.ace.task.view;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.bpa.tasks.editor.PropertyNameLabelEditor;

public class AddViewsFromUniversalPositionListBeanInfo extends SimpleBeanInfo {

    public PropertyDescriptor[] getPropertyDescriptors() {
        try {
            PropertyDescriptor profilePropName =
                    new PropertyDescriptor("profilePropName",
                        getBeanDescriptor().getBeanClass());
            profilePropName.setBound(true);
            profilePropName
                .setPropertyEditorClass(PropertyNameLabelEditor.class);
            profilePropName
                .setDisplayName("<html><font color='green'>profile property:");
            profilePropName
                .setShortDescription("The property containing the profile to change.");

            PropertyDescriptor positionListPropName =
                    new PropertyDescriptor("positionListPropName",
                        getBeanDescriptor().getBeanClass());
            positionListPropName.setBound(true);
            positionListPropName
                .setPropertyEditorClass(PropertyNameLabelEditor.class);
            positionListPropName
                .setDisplayName("<html><font color='green'>position list prop:");
            positionListPropName
                .setShortDescription("The property that contains universal position list");

            PropertyDescriptor rv[] = { profilePropName, positionListPropName };
            return rv;
        } catch (IntrospectionException e) {
            throw new Error(e.toString());
        }
    }

    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd =
                new BeanDescriptor(AddViewsFromUniversalPositionList.class);
        bd
            .setDisplayName("<html><font color='green'><center>add view positions<br>from position list");
        return bd;
    }
}
