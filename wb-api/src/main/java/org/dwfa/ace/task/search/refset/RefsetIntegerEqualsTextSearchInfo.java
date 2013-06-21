/*
 * Copyright 2012 International Health Terminology Standards Development Organisation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dwfa.ace.task.search.refset;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import org.dwfa.ace.task.search.AbstractSeachTestSearchInfo;
import org.dwfa.bpa.tasks.editor.JTextFieldEditorOneLine;

/**
 *
 * @author kec
 */
public class RefsetIntegerEqualsTextSearchInfo extends AbstractSeachTestSearchInfo {

    @Override
    protected PropertyDescriptor[] getAdditionalPropertyDescriptors() {
        try {

            PropertyDescriptor relRestrictionTerm = new PropertyDescriptor("text",
                getBeanDescriptor().getBeanClass());
            relRestrictionTerm.setBound(true);
            relRestrictionTerm.setPropertyEditorClass(JTextFieldEditorOneLine.class);
            relRestrictionTerm.setDisplayName("<html><font color='green'>text equals integer field:");
            relRestrictionTerm.setShortDescription("The integer text to test for equality in refset member integer field.");

            PropertyDescriptor[] rv = { relRestrictionTerm };
            return rv;
        } catch (IntrospectionException e) {
            throw new Error(e.toString());
        }
    }

    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    @Override
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(RefsetIntegerEqualsText.class);
        bd.setDisplayName("text equals integer field");
        return bd;
    }
}
