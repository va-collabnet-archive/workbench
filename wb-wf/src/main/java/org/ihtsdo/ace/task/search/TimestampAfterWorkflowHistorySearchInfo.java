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

public class TimestampAfterWorkflowHistorySearchInfo extends AbstractWorkflowHistorySearchTestSearchInfo {

    @Override
    protected PropertyDescriptor[] getAdditionalPropertyDescriptors() {
        try {
	        PropertyDescriptor testTimestampAfter = new PropertyDescriptor("testTimestampAfter", getBeanDescriptor().getBeanClass());
	        testTimestampAfter.setBound(true);
	        testTimestampAfter.setPropertyEditorClass(JTextFieldEditorOneLine.class);
	        testTimestampAfter.setDisplayName("<html><font color='green'>WfHx Record inserted After Following Date:");
	        testTimestampAfter.setShortDescription("The concept to test has the proper timestamp After Than.");
	
	        PropertyDescriptor rv[] = { testTimestampAfter };
	        return rv;
        } catch (IntrospectionException e) {
            throw new Error(e.toString());
        }
    }

    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(TimestampAfterWorkflowHistory.class);
        bd.setDisplayName("After Timestamp");
        return bd;
    }

}
