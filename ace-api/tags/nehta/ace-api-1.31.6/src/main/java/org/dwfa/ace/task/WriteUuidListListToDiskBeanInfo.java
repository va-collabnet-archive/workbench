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

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.bpa.tasks.editor.JTextFieldEditor;
import org.dwfa.bpa.tasks.editor.PropertyNameLabelEditor;

public class WriteUuidListListToDiskBeanInfo extends SimpleBeanInfo {

    public PropertyDescriptor[] getPropertyDescriptors() {
        try {
            PropertyDescriptor uuidListListPropName =
                    new PropertyDescriptor("uuidListListPropName",
                        getBeanDescriptor().getBeanClass());
            uuidListListPropName.setBound(true);
            uuidListListPropName
                .setPropertyEditorClass(PropertyNameLabelEditor.class);
            uuidListListPropName
                .setDisplayName("<html><font color='green'>Uuid List:");
            uuidListListPropName.setShortDescription("Uuid list.");

            PropertyDescriptor fileName =
                    new PropertyDescriptor("fileName", getBeanDescriptor()
                        .getBeanClass());
            fileName.setBound(true);
            fileName.setPropertyEditorClass(JTextFieldEditor.class);
            fileName.setDisplayName("<html><font color='green'>Uuid File:");
            fileName.setShortDescription("File Name of UUID list list file.");

            PropertyDescriptor rv[] = { uuidListListPropName, fileName };
            return rv;
        } catch (IntrospectionException e) {
            throw new Error(e.toString());
        }
    }

    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(WriteUuidListListToDisk.class);
        bd
            .setDisplayName("<html><font color='green'><center>Write UUID List <br> List to File");
        return bd;
    }

}
