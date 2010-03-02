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
package org.dwfa.ace.task.refset.members;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.bpa.tasks.editor.PropertyNameLabelEditor;

public final class WriteSingleRefsetDescriptionBeanInfo extends SimpleBeanInfo {

    @Override
    public PropertyDescriptor[] getPropertyDescriptors() {
        try {
            PropertyDescriptor putputDirPropertyName = new PropertyDescriptor("directoryKey",
                getBeanDescriptor().getBeanClass());
            putputDirPropertyName.setBound(true);
            putputDirPropertyName.setDisplayName("<html><font color='green'>Output directory key:");
            putputDirPropertyName.setShortDescription("Output directory key");
            putputDirPropertyName.setPropertyEditorClass(PropertyNameLabelEditor.class);

            PropertyDescriptor refsetPropertyName = new PropertyDescriptor("selectedRefsetKey",
                getBeanDescriptor().getBeanClass());
            refsetPropertyName.setBound(true);
            refsetPropertyName.setDisplayName("<html><font color='green'>Selected Refset key:");
            refsetPropertyName.setShortDescription("Selected Refset key");
            refsetPropertyName.setPropertyEditorClass(PropertyNameLabelEditor.class);

            return new PropertyDescriptor[] { putputDirPropertyName, refsetPropertyName };
        } catch (IntrospectionException e) {
            throw new Error(e.toString());
        }
    }

    @Override
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(WriteSingleRefsetDescription.class);
        bd.setDisplayName("<html><font color='green'><center>Export a single Refset<br>to Disk");
        return bd;
    }
}
