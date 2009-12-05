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

import org.dwfa.bpa.tasks.editor.PropertyNameLabelEditor;

public class ConvertEditPathsBeanInfo extends SimpleBeanInfo {

    public PropertyDescriptor[] getPropertyDescriptors() {
        try {

            PropertyDescriptor inputFilePropName =
                    new PropertyDescriptor("inputFilePropName",
                        getBeanDescriptor().getBeanClass());
            inputFilePropName.setBound(true);
            inputFilePropName
                .setPropertyEditorClass(PropertyNameLabelEditor.class);
            inputFilePropName
                .setDisplayName("<html><font color='green'>input file prop:");
            inputFilePropName
                .setShortDescription("The property that contains the file name of the input file.");

            PropertyDescriptor conceptMapPropName =
                    new PropertyDescriptor("conceptMapPropName",
                        getBeanDescriptor().getBeanClass());
            conceptMapPropName.setBound(true);
            conceptMapPropName
                .setPropertyEditorClass(PropertyNameLabelEditor.class);
            conceptMapPropName
                .setDisplayName("<html><font color='green'>concept map prop:");
            conceptMapPropName
                .setShortDescription("The property that contains the resulting concept map.");

            PropertyDescriptor outputFilePropName =
                    new PropertyDescriptor("outputFilePropName",
                        getBeanDescriptor().getBeanClass());
            outputFilePropName.setBound(true);
            outputFilePropName
                .setPropertyEditorClass(PropertyNameLabelEditor.class);
            outputFilePropName
                .setDisplayName("<html><font color='green'>output file prop:");
            outputFilePropName
                .setShortDescription("The property that contains the file name of the output file.");

            PropertyDescriptor rv[] =
                    { inputFilePropName, conceptMapPropName, outputFilePropName };

            return rv;
        } catch (IntrospectionException e) {
            throw new Error(e.toString());
        }
    }

    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(ConvertEditPaths.class);
        bd
            .setDisplayName("<html><font color='green'><center>Convert Edit Paths<br>Using Map");
        return bd;
    }
}
