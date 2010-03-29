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
package org.dwfa.ace.task.search;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;

import org.dwfa.ace.prop.editor.ConceptLabelPropEditor;
import org.dwfa.bpa.tasks.editor.CheckboxEditor;

public class RelSubsumptionMatchSearchInfo extends AbstractSeachTestSearchInfo {

    @Override
    protected PropertyDescriptor[] getAdditionalPropertyDescriptors() {
        try {
            PropertyDescriptor applySubsumption = new PropertyDescriptor("applySubsumption",
                getBeanDescriptor().getBeanClass());
            applySubsumption.setBound(true);
            applySubsumption.setPropertyEditorClass(CheckboxEditor.class);
            applySubsumption.setDisplayName("<html><font color='green'>use subsumption:");
            applySubsumption.setShortDescription("If checked, uses subsumption to test rel type and rel restriction.");

            PropertyDescriptor relTypeTerm = new PropertyDescriptor("relTypeTerm", getBeanDescriptor().getBeanClass());
            relTypeTerm.setBound(true);
            relTypeTerm.setPropertyEditorClass(ConceptLabelPropEditor.class);
            relTypeTerm.setDisplayName("<html><font color='green'>type kind:");
            relTypeTerm.setShortDescription("The concept to test for type is kind of.");

            PropertyDescriptor relRestrictionTerm = new PropertyDescriptor("relRestrictionTerm",
                getBeanDescriptor().getBeanClass());
            relRestrictionTerm.setBound(true);
            relRestrictionTerm.setPropertyEditorClass(ConceptLabelPropEditor.class);
            relRestrictionTerm.setDisplayName("<html><font color='green'>restriction kind:");
            relRestrictionTerm.setShortDescription("The concept to test for restriction is kind of.");

            PropertyDescriptor[] rv = { applySubsumption, relTypeTerm, relRestrictionTerm };
            return rv;
        } catch (IntrospectionException e) {
            throw new Error(e.toString());
        }
    }

    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(RelSubsumptionMatch.class);
        bd.setDisplayName("rel kind");
        return bd;
    }
}
