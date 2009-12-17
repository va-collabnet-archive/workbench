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

import org.dwfa.bpa.tasks.editor.ConceptLabelEditor;

public class IsChildOfSearchInfo extends AbstractSeachTestSearchInfo {

    @Override
    protected PropertyDescriptor[] getAdditionalPropertyDescriptors() {
        try {
            PropertyDescriptor parentTerm = new PropertyDescriptor("parentTerm", getBeanDescriptor().getBeanClass());
            parentTerm.setBound(true);
            parentTerm.setPropertyEditorClass(ConceptLabelEditor.class);
            parentTerm.setDisplayName("<html><font color='green'>parent:");
            parentTerm.setShortDescription("The concept to test for parentage of.");

            PropertyDescriptor rv[] = { parentTerm };
            return rv;
        } catch (IntrospectionException e) {
            throw new Error(e.toString());
        }
    }

    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(IsChildOf.class);
        bd.setDisplayName("is child of");
        return bd;
    }
}
