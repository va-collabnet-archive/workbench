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
package org.dwfa.ace.task.refset.spec;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.bpa.tasks.editor.CheckboxEditor;
import org.dwfa.bpa.tasks.editor.PropertyNameLabelEditor;

public abstract class AbstractAddRefsetSpecTaskBeanInfo extends SimpleBeanInfo {

    public PropertyDescriptor[] getPropertyDescriptors() {
        try {

            PropertyDescriptor clauseIsTrue =
                    new PropertyDescriptor("clauseIsTrue", getBeanDescriptor().getBeanClass());
            clauseIsTrue.setBound(true);
            clauseIsTrue.setPropertyEditorClass(CheckboxEditor.class);
            clauseIsTrue.setDisplayName("<html><font color='green'>true:");
            clauseIsTrue
                .setShortDescription("If checked, the clause must be true. If not checked, the clause must be false.");

            PropertyDescriptor activeConceptPropName;
            activeConceptPropName = new PropertyDescriptor("activeConceptPropName", getBeanDescriptor().getBeanClass());
            activeConceptPropName.setBound(true);
            activeConceptPropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            activeConceptPropName.setDisplayName("<html><font color='green'>active concept uuid prop name:");
            activeConceptPropName.setShortDescription("The property to put the active concept into.");

            PropertyDescriptor activeDescriptionPropName;
            activeDescriptionPropName =
                    new PropertyDescriptor("activeDescriptionPropName", getBeanDescriptor().getBeanClass());
            activeDescriptionPropName.setBound(true);
            activeDescriptionPropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            activeDescriptionPropName.setDisplayName("<html><font color='green'>active description uuid prop name:");
            activeDescriptionPropName.setShortDescription("The property to put the active description into.");

            PropertyDescriptor rv[] = { clauseIsTrue, activeConceptPropName, activeDescriptionPropName };
            return rv;
        } catch (IntrospectionException e) {
            throw new Error(e.toString());
        }
    }

    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public abstract BeanDescriptor getBeanDescriptor();

}
