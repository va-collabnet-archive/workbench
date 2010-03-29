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
package org.dwfa.ace.task;

import org.dwfa.bpa.tasks.editor.PropertyNameLabelEditor;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

public final class ChooseDirectoryBeanInfo extends SimpleBeanInfo {

    @Override
    public PropertyDescriptor[] getPropertyDescriptors() {
        try {

            PropertyDescriptor directoryKey = new PropertyDescriptor("directoryKey", getBeanDescriptor().getBeanClass());
            directoryKey.setBound(true);
            directoryKey.setPropertyEditorClass(PropertyNameLabelEditor.class);
            directoryKey.setDisplayName("<html><font color='green'>Name of directory key:");
            directoryKey.setShortDescription("Name of directory key.");

            PropertyDescriptor rv[] = { directoryKey };
            return rv;
        } catch (IntrospectionException e) {
            throw new Error(e.toString());
        }
    }

    @Override
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(ChooseDirectory.class);
        bd.setDisplayName("<html><font color='green'><center>Choose Directory");
        return bd;
    }
}
