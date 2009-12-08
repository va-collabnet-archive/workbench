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
package org.dwfa.ace.task.rel;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.ace.prop.editor.ConceptLabelPropEditor;
import org.dwfa.bpa.tasks.editor.PropertyNameLabelEditor;

public class CreateRelationshipBeanInfo extends SimpleBeanInfo {

    /**
     * 
     */
    public CreateRelationshipBeanInfo() {
        super();
    }

    public PropertyDescriptor[] getPropertyDescriptors() {
        try {
            PropertyDescriptor relParentPropName = new PropertyDescriptor("relParentPropName", CreateRelationship.class);
            relParentPropName.setBound(true);
            relParentPropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            relParentPropName.setDisplayName("<html><font color='green'>Rel parent:");
            relParentPropName.setShortDescription("The property containing the new parent value for the relationship.");

            PropertyDescriptor activeConceptPropName = new PropertyDescriptor("activeConceptPropName",
                CreateRelationship.class);
            activeConceptPropName.setBound(true);
            activeConceptPropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            activeConceptPropName.setDisplayName("<html><font color='green'>Concept property:");
            activeConceptPropName.setShortDescription("Name of the property containing the concept to add the rel to. ");

            PropertyDescriptor relType = new PropertyDescriptor("relType", CreateRelationship.class);
            relType.setBound(true);
            relType.setPropertyEditorClass(ConceptLabelPropEditor.class);
            relType.setDisplayName("Rel type:");
            relType.setShortDescription("The relationship type for the new relationship.");

            PropertyDescriptor relCharacteristic = new PropertyDescriptor("relCharacteristic", CreateRelationship.class);
            relCharacteristic.setBound(true);
            relCharacteristic.setPropertyEditorClass(ConceptLabelPropEditor.class);
            relCharacteristic.setDisplayName("Rel characteristic:");
            relCharacteristic.setShortDescription("The characteristic for the new relationship.");

            PropertyDescriptor relRefinability = new PropertyDescriptor("relRefinability", CreateRelationship.class);
            relRefinability.setBound(true);
            relRefinability.setPropertyEditorClass(ConceptLabelPropEditor.class);
            relRefinability.setDisplayName("Rel refinability:");
            relRefinability.setShortDescription("The refinability of the new relationship.");

            PropertyDescriptor relStatus = new PropertyDescriptor("relStatus", CreateRelationship.class);
            relStatus.setBound(true);
            relStatus.setPropertyEditorClass(ConceptLabelPropEditor.class);
            relStatus.setDisplayName("Rel status:");
            relStatus.setShortDescription("The status of the new relationship.");

            PropertyDescriptor rv[] = { relParentPropName, activeConceptPropName, relType, relCharacteristic,
                                       relRefinability, relStatus };
            return rv;
        } catch (IntrospectionException e) {
            throw new Error(e.toString());
        }
    }

    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(CreateRelationship.class);
        bd.setDisplayName("<html><font color='green'><center>Create Relationship<br>from Selected Parent");
        return bd;
    }
}
