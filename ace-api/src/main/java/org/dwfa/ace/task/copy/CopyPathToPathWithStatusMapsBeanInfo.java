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
package org.dwfa.ace.task.copy;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.bpa.tasks.editor.PropertyNameLabelEditor;

public class CopyPathToPathWithStatusMapsBeanInfo extends SimpleBeanInfo {

    public PropertyDescriptor[] getPropertyDescriptors() {
        try {

            PropertyDescriptor profilePropName = new PropertyDescriptor("profilePropName",
                getBeanDescriptor().getBeanClass());
            profilePropName.setBound(true);
            profilePropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            profilePropName.setDisplayName("<html><font color='green'>profile prop:");
            profilePropName.setShortDescription("Name of the property to hold the profile from which the view positions are derived. ");

            PropertyDescriptor toPathPropName = new PropertyDescriptor("toPathPropName",
                getBeanDescriptor().getBeanClass());
            toPathPropName.setBound(true);
            toPathPropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            toPathPropName.setDisplayName("<html><font color='green'>to path:");
            toPathPropName.setShortDescription("Name of the property to copy the concept to. ");

            PropertyDescriptor conceptPropName = new PropertyDescriptor("conceptPropName",
                getBeanDescriptor().getBeanClass());
            conceptPropName.setBound(true);
            conceptPropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            conceptPropName.setDisplayName("<html><font color='green'>concept:");
            conceptPropName.setShortDescription("Name of the property holding the concept to copy. ");

            PropertyDescriptor conceptStatusMapPropName = new PropertyDescriptor("conceptStatusMapPropName",
                getBeanDescriptor().getBeanClass());
            conceptStatusMapPropName.setBound(true);
            conceptStatusMapPropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            conceptStatusMapPropName.setDisplayName("<html><font color='green'>concept status map:");
            conceptStatusMapPropName.setShortDescription("Name of the property holding mapping for the new concept status values. ");

            PropertyDescriptor elementStatusMapPropName = new PropertyDescriptor("elementStatusMapPropName",
                getBeanDescriptor().getBeanClass());
            elementStatusMapPropName.setBound(true);
            elementStatusMapPropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            elementStatusMapPropName.setDisplayName("<html><font color='green'>elem status map:");
            elementStatusMapPropName.setShortDescription("Name of the property holding the mapping for the new element status values. ");

            PropertyDescriptor rv[] = { profilePropName, toPathPropName, conceptPropName, conceptStatusMapPropName,
                                       elementStatusMapPropName };
            return rv;
        } catch (IntrospectionException e) {
            throw new Error(e.toString());
        }
    }

    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(CopyPathToPathWithStatusMaps.class);
        bd.setDisplayName("<html><font color='green'><center>copy path to path<br>from views in profile<br>with status maps");
        return bd;
    }
}
