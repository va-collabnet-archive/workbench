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

public class InstructAndWaitDoBeanInfo extends SimpleBeanInfo {
    public PropertyDescriptor[] getPropertyDescriptors() {
        try {
            PropertyDescriptor instruction = new PropertyDescriptor("instruction", InstructAndWaitDo.class);
            instruction.setBound(true);
            instruction.setPropertyEditorClass(JTextFieldEditor.class);
            instruction.setDisplayName("<html><font color='green'>Instruction:");
            instruction.setShortDescription("Instructions to present to the user in the workflow panel. ");

            PropertyDescriptor termPropName = new PropertyDescriptor("termPropName", InstructAndWaitDo.class);
            termPropName.setBound(true);
            termPropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            termPropName.setDisplayName("<html><font color='green'>Term:");
            termPropName.setShortDescription("Term");

            PropertyDescriptor rv[] = { instruction, termPropName };
            return rv;
        } catch (IntrospectionException e) {
            throw new Error(e.toString());
        }
    }

    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(InstructAndWaitDo.class);
        bd.setDisplayName("<html><font color='green'><center>Instruct and Wait<br>until Done");
        return bd;
    }
}
