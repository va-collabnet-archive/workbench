/*
 * Copyright (c) 2010 International Health Terminology Standards Development
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
package org.ihtsdo.translation.tasks.search;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;

import org.dwfa.ace.prop.editor.ConceptLabelPropEditor;
import org.dwfa.ace.task.search.AbstractSeachTestSearchInfo;

/**
 * The Class SearchLangRefsetDescTypeAcceptabilitySearchInfo.
 */
public class SearchLangRefsetDescTypeAcceptabilitySearchInfo extends AbstractSeachTestSearchInfo {

    /* (non-Javadoc)
     * @see org.dwfa.ace.task.search.AbstractSeachTestSearchInfo#getAdditionalPropertyDescriptors()
     */
    @Override
    protected PropertyDescriptor[] getAdditionalPropertyDescriptors() {
        try {

            PropertyDescriptor langRefsetTerm = new PropertyDescriptor("langRefsetTerm", getBeanDescriptor().getBeanClass());
            langRefsetTerm.setBound(true);
            langRefsetTerm.setPropertyEditorClass(ConceptLabelPropEditor.class);
            langRefsetTerm.setDisplayName("<html><font color='green'>lang refset:");
            langRefsetTerm.setShortDescription("The lang refset.");
            
            PropertyDescriptor descTypeTerm = new PropertyDescriptor("descTypeTerm", getBeanDescriptor().getBeanClass());
            descTypeTerm.setBound(true);
            descTypeTerm.setPropertyEditorClass(ConceptLabelPropEditor.class);
            descTypeTerm.setDisplayName("<html><font color='green'>description type:");
            descTypeTerm.setShortDescription("The concept to test for description type.");
            
            PropertyDescriptor acceptabilityTerm = new PropertyDescriptor("acceptabilityTerm", getBeanDescriptor().getBeanClass());
            acceptabilityTerm.setBound(true);
            acceptabilityTerm.setPropertyEditorClass(ConceptLabelPropEditor.class);
            acceptabilityTerm.setDisplayName("<html><font color='green'>acceptability:");
            acceptabilityTerm.setShortDescription("The concept to test for acceptability.");
            
            PropertyDescriptor rv[] = { langRefsetTerm, descTypeTerm, acceptabilityTerm};
            return rv;
        } catch (IntrospectionException e) {
            throw new Error(e.toString());
        }
    }

    /**
     * Gets the bean descriptor.
     *
     * @return the bean descriptor
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(SearchLangRefsetDescTypeAcceptability.class);
        bd.setDisplayName("lang refset,type & accept");
        return bd;
    }
}
