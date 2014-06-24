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
 * The Class SearchLangRefsetStatusDateUserSearchInfo.
 */
public class SearchLangRefsetStatusDateUserSearchInfo extends AbstractSeachTestSearchInfo {

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
            
            PropertyDescriptor statusTerm = new PropertyDescriptor("statusTerm", getBeanDescriptor().getBeanClass());
            statusTerm.setBound(true);
            statusTerm.setPropertyEditorClass(ConceptLabelPropEditor.class);
            statusTerm.setDisplayName("<html><font color='green'>concept status kind:");
            statusTerm.setShortDescription("The concept to test for status is kind of.");
            
            PropertyDescriptor day = new PropertyDescriptor("day", getBeanDescriptor().getBeanClass());
            day.setBound(true);
            day.setPropertyEditorClass(DayNumberSelectorEditor.class);
            day.setDisplayName("<html><font color='green'>day:");
            day.setShortDescription("day.");
            
            PropertyDescriptor month = new PropertyDescriptor("month", getBeanDescriptor().getBeanClass());
            month.setBound(true);
            month.setPropertyEditorClass(MonthNumberSelectorEditor.class);
            month.setDisplayName("<html><font color='green'>month:");
            month.setShortDescription("month.");
            
            PropertyDescriptor year = new PropertyDescriptor("year", getBeanDescriptor().getBeanClass());
            year.setBound(true);
            year.setPropertyEditorClass(YearNumberSelectorEditor.class);
            year.setDisplayName("<html><font color='green'>year:");
            year.setShortDescription("year.");
            
            PropertyDescriptor authorTerm = new PropertyDescriptor("authorTerm", getBeanDescriptor().getBeanClass());
            authorTerm.setBound(true);
            authorTerm.setPropertyEditorClass(ConceptLabelPropEditor.class);
            authorTerm.setDisplayName("<html><font color='green'>author concept:");
            authorTerm.setShortDescription("The  author concept to test.");

            PropertyDescriptor rv[] = { langRefsetTerm, statusTerm, day, month, year, authorTerm};
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
        BeanDescriptor bd = new BeanDescriptor(SearchLangRefsetStatusDateUser.class);
        bd.setDisplayName("lang refset and status date user");
        return bd;
    }
}
