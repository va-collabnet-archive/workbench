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

import org.dwfa.bpa.tasks.editor.JTextFieldEditor;

public class ConvertChangeSetBeanInfo extends SimpleBeanInfo {
    public PropertyDescriptor[] getPropertyDescriptors() {
        try {

            PropertyDescriptor filename = new PropertyDescriptor("filename", getBeanDescriptor().getBeanClass());
            filename.setBound(true);
            filename.setPropertyEditorClass(JTextFieldEditor.class);
            filename.setDisplayName("<html><font color='green'>changeset:");
            filename.setShortDescription("The file to convert. ");

            PropertyDescriptor outputSuffix = new PropertyDescriptor("outputSuffix", getBeanDescriptor().getBeanClass());
            outputSuffix.setBound(true);
            outputSuffix.setPropertyEditorClass(JTextFieldEditor.class);
            outputSuffix.setDisplayName("<html><font color='green'>output file suffix:");
            outputSuffix.setShortDescription("The generated output file suffix. ");

            PropertyDescriptor changeSetTransformer = new PropertyDescriptor("changeSetTransformer",
                getBeanDescriptor().getBeanClass());
            changeSetTransformer.setBound(true);
            changeSetTransformer.setPropertyEditorClass(JTextFieldEditor.class);
            changeSetTransformer.setDisplayName("<html><font color='green'>transformer class name:");
            changeSetTransformer.setShortDescription("The fully qualified name of the class to use to convert the change set. ");

            PropertyDescriptor rv[] = { filename, outputSuffix, changeSetTransformer };
            return rv;
        } catch (IntrospectionException e) {
            throw new Error(e.toString());
        }
    }

    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(ConvertChangeSet.class);
        bd.setDisplayName("<html><font color='green'><center>Convert Change Set");
        return bd;
    }
}
