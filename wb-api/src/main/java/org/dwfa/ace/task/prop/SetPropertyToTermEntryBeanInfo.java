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
package org.dwfa.ace.task.prop;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.ace.prop.editor.ConceptLabelPropEditor;
import org.dwfa.bpa.tasks.editor.CheckboxEditor;
import org.dwfa.bpa.tasks.editor.PropertyNameLabelEditor;

public class SetPropertyToTermEntryBeanInfo extends SimpleBeanInfo {

    public PropertyDescriptor[] getPropertyDescriptors() {
        try {

            PropertyDescriptor termPropName = new PropertyDescriptor("termPropName", getBeanDescriptor().getBeanClass());
            termPropName.setBound(true);
            termPropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            termPropName.setDisplayName("<html><font color='green'>profile prop:");
            termPropName.setShortDescription("The property that contains the profile to modify.");

            PropertyDescriptor termEntry = new PropertyDescriptor("termEntry", getBeanDescriptor().getBeanClass());
            termEntry.setBound(true);
            termEntry.setPropertyEditorClass(ConceptLabelPropEditor.class);
            termEntry.setDisplayName("<html><font color='green'>term:");
            termEntry.setShortDescription("The concept to add to the property.");

            PropertyDescriptor convertToConcept = new PropertyDescriptor("convertToConcept",
                getBeanDescriptor().getBeanClass());
            convertToConcept.setBound(true);
            convertToConcept.setPropertyEditorClass(CheckboxEditor.class);
            convertToConcept.setDisplayName("<html><font color='green'>Convert to concept");
            convertToConcept.setShortDescription("If checked the property will be set as a concept bean rather than as a term entry");

            PropertyDescriptor rv[] = { termPropName, termEntry, convertToConcept };
            return rv;
        } catch (IntrospectionException e) {
            throw new Error(e.toString());
        }
    }

    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(SetPropertyToTermEntry.class);
        bd.setDisplayName("<html><font color='green'><center>set property<br>to term entry");
        return bd;
    }
}
