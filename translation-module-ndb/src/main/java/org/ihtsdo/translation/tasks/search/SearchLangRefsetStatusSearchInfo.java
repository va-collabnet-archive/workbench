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
package org.ihtsdo.translation.tasks.search;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;

import org.dwfa.ace.prop.editor.ConceptLabelPropEditor;
import org.dwfa.ace.task.search.AbstractSeachTestSearchInfo;

public class SearchLangRefsetStatusSearchInfo extends AbstractSeachTestSearchInfo {

    @Override
    protected PropertyDescriptor[] getAdditionalPropertyDescriptors() {
        try {

            PropertyDescriptor langRefsetTerm = new PropertyDescriptor("langRefsetTerm", getBeanDescriptor().getBeanClass());
            langRefsetTerm.setBound(true);
            langRefsetTerm.setPropertyEditorClass(ConceptLabelPropEditor.class);
            langRefsetTerm.setDisplayName("<html><font color='green'>lang refset:");
            langRefsetTerm.setShortDescription("The lang refset.");
            
            PropertyDescriptor statusTerm = new PropertyDescriptor("statusTerm", getBeanDescriptor().getBeanClass());
            statusTerm.setBound(true);
            statusTerm.setPropertyEditorClass(ConceptLabelPropEditor.class);
            statusTerm.setDisplayName("<html><font color='green'>translation status:");
            statusTerm.setShortDescription("The concept to test for status is kind of.");
            
            PropertyDescriptor rv[] = { langRefsetTerm, statusTerm};
            return rv;
        } catch (IntrospectionException e) {
            throw new Error(e.toString());
        }
    }

    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(SearchLangRefsetStatus.class);
        bd.setDisplayName("lang refset and status");
        return bd;
    }
}
