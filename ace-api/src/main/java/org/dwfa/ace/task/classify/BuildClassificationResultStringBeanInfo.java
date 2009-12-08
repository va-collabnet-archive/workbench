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
package org.dwfa.ace.task.classify;

import java.beans.BeanDescriptor;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.bpa.tasks.editor.PropertyNameLabelEditor;

/**
 * c
 * 
 * @author Ming Zhang
 * 
 */
public class BuildClassificationResultStringBeanInfo extends SimpleBeanInfo {

    public PropertyDescriptor[] getPropertyDescriptors() {
        try {
            PropertyDescriptor stringName = new PropertyDescriptor("stringName", getBeanDescriptor().getBeanClass());
            stringName.setBound(true);
            stringName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            stringName.setDisplayName("<html><font color='green'>output string");
            stringName.setShortDescription("Input String");
            PropertyDescriptor rv[] = { stringName };
            return rv;
        } catch (Exception e) {
            throw new Error(e.toString());
        }
    }

    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(BuildClassificationResultString.class);
        bd.setDisplayName("<html><font color='green'><center>Build Classify result");
        return bd;
    }
}
