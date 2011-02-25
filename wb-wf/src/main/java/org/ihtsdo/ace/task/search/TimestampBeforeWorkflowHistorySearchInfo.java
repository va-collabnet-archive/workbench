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
package org.ihtsdo.ace.task.search;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;

import org.dwfa.bpa.tasks.editor.JTextFieldEditorOneLine;

public class TimestampBeforeWorkflowHistorySearchInfo extends AbstractWorkflowHistorySearchTestSearchInfo {

    @Override
    protected PropertyDescriptor[] getAdditionalPropertyDescriptors() {
        try {
	        PropertyDescriptor testTimestampBefore = new PropertyDescriptor("testTimestampBefore", getBeanDescriptor().getBeanClass());
	        testTimestampBefore.setBound(true);
	        testTimestampBefore.setPropertyEditorClass(JTextFieldEditorOneLine.class);
	        testTimestampBefore.setDisplayName("<html><font color='green'>Date (mm/dd/yyyy):");
	        testTimestampBefore.setShortDescription("The concept to test has the proper timestamp Before Than.");
	
	        PropertyDescriptor rv[] = { testTimestampBefore };
	        return rv;
        } catch (IntrospectionException e) {
            throw new Error(e.toString());
        }
    }

    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(TimestampBeforeWorkflowHistory.class);
        bd.setDisplayName("Timestamp on or before");
        return bd;
    }

}
