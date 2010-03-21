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
package org.dwfa.ace.task.signpost;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.bpa.tasks.editor.PropertyNameLabelEditor;

public class SetSignpostDetailAndInstructionBeanInfo extends SimpleBeanInfo {

    public PropertyDescriptor[] getPropertyDescriptors() {
        try {
            PropertyDescriptor instructionHtmlPropName = new PropertyDescriptor("instructionHtmlPropName",
                getBeanDescriptor().getBeanClass());
            instructionHtmlPropName.setBound(true);
            instructionHtmlPropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            instructionHtmlPropName.setDisplayName("<html><font color='green'>instruction html");
            instructionHtmlPropName.setShortDescription("Set the signpost instruction html to value contained in this property.");

            PropertyDescriptor detailHtmlPropName = new PropertyDescriptor("detailHtmlPropName",
                getBeanDescriptor().getBeanClass());
            detailHtmlPropName.setBound(true);
            detailHtmlPropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            detailHtmlPropName.setDisplayName("<html><font color='green'>detail html");
            detailHtmlPropName.setShortDescription("Set the signpost detail html to value contained in this property.");

            PropertyDescriptor rv[] = { detailHtmlPropName, instructionHtmlPropName };
            return rv;
        } catch (IntrospectionException e) {
            throw new Error(e.toString());
        }
    }

    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(SetSignpostDetailAndInstruction.class);
        bd.setDisplayName("<html><font color='green'><center>Set Signpost<br> detail and instruction");
        return bd;
    }

}
