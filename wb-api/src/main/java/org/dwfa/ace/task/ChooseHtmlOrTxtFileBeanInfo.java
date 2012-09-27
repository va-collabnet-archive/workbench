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

import org.dwfa.bpa.tasks.editor.PropertyNameLabelEditor;

/**
 * Bean info to ChooseHtmlOrTxtFile class.
 * 
 * @author Susan Castillo
 * 
 */
public class ChooseHtmlOrTxtFileBeanInfo extends SimpleBeanInfo {

    /**
     *
     */
    public ChooseHtmlOrTxtFileBeanInfo() {
        super();
    }

    public PropertyDescriptor[] getPropertyDescriptors() {
        try {

            PropertyDescriptor instructionFileNamePropName = new PropertyDescriptor("instructionFileNamePropName",
                getBeanDescriptor().getBeanClass());
            instructionFileNamePropName.setBound(true);
            instructionFileNamePropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            instructionFileNamePropName.setDisplayName("<html><font color='green'>Instruction File:");
            instructionFileNamePropName.setShortDescription("Instruction File");

            PropertyDescriptor directoryPropName = new PropertyDescriptor("directoryPropName",
                getBeanDescriptor().getBeanClass());
            directoryPropName.setBound(true);
            directoryPropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            directoryPropName.setDisplayName("<html><font color='green'>Instruction File:");
            directoryPropName.setShortDescription("Instruction File");

            PropertyDescriptor rv[] = { instructionFileNamePropName, directoryPropName };
            return rv;
        } catch (IntrospectionException e) {
            throw new Error(e.toString());
        }
    }

    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(ChooseHtmlOrTxtFile.class);
        bd.setDisplayName("<html><font color='green'><center>Choose Instruction File");
        return bd;
    }

}
