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
package org.dwfa.ace.task.svn;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.bpa.tasks.editor.JTextFieldEditor;
import org.dwfa.bpa.tasks.editor.JTextFieldEditorOneLine;
import org.dwfa.bpa.tasks.editor.PropertyNameLabelEditor;

public class AddSubversionEntryBeanInfo extends SimpleBeanInfo {

    public PropertyDescriptor[] getPropertyDescriptors() {
        try {
            PropertyDescriptor repoUrl = new PropertyDescriptor("repoUrl", getBeanDescriptor().getBeanClass());
            repoUrl.setBound(true);
            repoUrl.setPropertyEditorClass(JTextFieldEditor.class);
            repoUrl.setDisplayName("<html><font color='green'>repoUrl:");
            repoUrl.setShortDescription("The URL of the repository to checkout.");

            PropertyDescriptor workingCopy = new PropertyDescriptor("workingCopy", getBeanDescriptor().getBeanClass());
            workingCopy.setBound(true);
            workingCopy.setPropertyEditorClass(JTextFieldEditor.class);
            workingCopy.setDisplayName("<html><font color='green'>working copy:");
            workingCopy.setShortDescription("The local directory to hold the working copy.");

            PropertyDescriptor prompt = new PropertyDescriptor("prompt", getBeanDescriptor().getBeanClass());
            prompt.setBound(true);
            prompt.setPropertyEditorClass(JTextFieldEditor.class);
            prompt.setDisplayName("<html><font color='green'>prompt:");
            prompt.setShortDescription("The prompt to tell the user what type of subversion entry they are making.");

            PropertyDescriptor keyName = new PropertyDescriptor("keyName", getBeanDescriptor().getBeanClass());
            keyName.setBound(true);
            keyName.setPropertyEditorClass(JTextFieldEditorOneLine.class);
            keyName.setDisplayName("<html><font color='green'>profile key:");
            keyName.setShortDescription("The key for the subversion entry.");

            PropertyDescriptor profilePropName = new PropertyDescriptor("profilePropName",
                getBeanDescriptor().getBeanClass());
            profilePropName.setBound(true);
            profilePropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            profilePropName.setDisplayName("<html><font color='green'>profile prop:");
            profilePropName.setShortDescription("The property that contains the profile to write to disk.");

            PropertyDescriptor rv[] = { prompt, keyName, repoUrl, workingCopy, profilePropName };
            return rv;
        } catch (IntrospectionException e) {
            throw new Error(e.toString());
        }
    }

    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(AddSubversionEntry.class);
        bd.setDisplayName("<html><font color='green'><center>add subversion entry");
        return bd;
    }
}
