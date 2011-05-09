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
package org.ihtsdo.ace.task.workflow.search;

import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

public abstract class AbstractWorkflowHistorySearchTestSearchInfo extends SimpleBeanInfo {

    public PropertyDescriptor[] getPropertyDescriptors() {
//        try {
        	
/*
            PropertyDescriptor term = new PropertyDescriptor("term", getBeanDescriptor().getBeanClass());
            term.setBound(true);
            term.setPropertyEditorClass(JTextFieldEditor.class);
            term.setDisplayName("<html><font color='green'>Search Term:");
            term.setShortDescription("If checked, excludes concepts that match this criteria");
*/
            PropertyDescriptor[] childDescriptors = getAdditionalPropertyDescriptors();

            PropertyDescriptor[] rv = new PropertyDescriptor[childDescriptors.length];

            //rv[0] = term;
            for (int i = 0; i < rv.length; i++) {
                rv[i] = childDescriptors[i];
            }

            return rv;
//        } catch (IntrospectionException e) {
  //          throw new Error(e.toString());
    //    }
    }

    protected abstract PropertyDescriptor[] getAdditionalPropertyDescriptors();
}
