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
package org.dwfa.bpa.tasks.file;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.bpa.tasks.editor.JTextFieldEditor;
import org.dwfa.bpa.tasks.editor.PropertyNameLabelEditor;

public class SpecifyOutputFileBeanInfo extends SimpleBeanInfo {

    /**
     * 
     */
    public PropertyDescriptor[] getPropertyDescriptors() {
        try {
            PropertyDescriptor prompt = new PropertyDescriptor("prompt", getBeanDescriptor().getBeanClass());
            prompt.setBound(true);
            prompt.setPropertyEditorClass(JTextFieldEditor.class);
            prompt.setDisplayName("prompt");
            prompt.setShortDescription("A message to present to the user in a dialog.");

            PropertyDescriptor extension = new PropertyDescriptor("extension", getBeanDescriptor().getBeanClass());
            extension.setBound(true);
            extension.setPropertyEditorClass(JTextFieldEditor.class);
            extension.setDisplayName("extension");
            extension.setShortDescription("The file extension of the output file.");

            PropertyDescriptor outputFilePropName = new PropertyDescriptor("outputFilePropName",
                getBeanDescriptor().getBeanClass());
            outputFilePropName.setBound(true);
            outputFilePropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            outputFilePropName.setDisplayName("<html><font color='blue'>outputFilePropName:");
            outputFilePropName.setShortDescription("Name of the property to write the output file name to... ");

            PropertyDescriptor rv[] = { prompt, extension, outputFilePropName };
            return rv;
        } catch (IntrospectionException e) {
            throw new Error(e.toString());
        }
    }

    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(SpecifyOutputFile.class);
        bd.setDisplayName("<html><font color='green'><center>Specify<br>Output File");
        return bd;
    }

}
