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
package org.dwfa.ace.task.cs;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.bpa.tasks.editor.CheckboxEditor;
import org.dwfa.bpa.tasks.editor.JTextFieldEditor;

public class ImportAllChangeSetsBeanInfo extends SimpleBeanInfo {
    public PropertyDescriptor[] getPropertyDescriptors() {
        try {
            PropertyDescriptor rootDirStr = new PropertyDescriptor("rootDirStr", getBeanDescriptor().getBeanClass());
            rootDirStr.setBound(true);
            rootDirStr.setPropertyEditorClass(JTextFieldEditor.class);
            rootDirStr.setDisplayName("<html><font color='green'>root dir:");
            rootDirStr.setShortDescription("The directory root to search for change sets. ");

            PropertyDescriptor validateChangeSets = new PropertyDescriptor("validateChangeSets",
                getBeanDescriptor().getBeanClass());
            validateChangeSets.setBound(true);
            validateChangeSets.setPropertyEditorClass(CheckboxEditor.class);
            validateChangeSets.setDisplayName("<html><font color='green'>validate:");
            validateChangeSets.setShortDescription("Select if you want to validate change sets. ");

            PropertyDescriptor validators = new PropertyDescriptor("validators", getBeanDescriptor().getBeanClass());
            validators.setBound(true);
            validators.setPropertyEditorClass(JTextFieldEditor.class);
            validators.setDisplayName("<html><font color='green'>validators:");
            validators.setShortDescription("The validators used on the change sets. ");

            PropertyDescriptor rv[] = { rootDirStr, validateChangeSets, validators };
            return rv;
        } catch (IntrospectionException e) {
            throw new Error(e.toString());
        }
    }

    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(ImportAllChangeSets.class);
        bd.setDisplayName("<html><font color='green'><center>Import All<br>Change Sets");
        return bd;
    }
}
