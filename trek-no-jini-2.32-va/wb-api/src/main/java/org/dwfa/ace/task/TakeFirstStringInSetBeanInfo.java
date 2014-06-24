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
package org.dwfa.ace.task;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.bpa.tasks.editor.PropertyNameLabelEditor;

public class TakeFirstStringInSetBeanInfo extends SimpleBeanInfo {

    /**
     *Bean info for TakeFirstStringInSet class.
     * 
     * @author akf
     */
    public TakeFirstStringInSetBeanInfo() {
        super();
    }

    public PropertyDescriptor[] getPropertyDescriptors() {
            
        try{   
            PropertyDescriptor submissionLineProp = new PropertyDescriptor("submissionLineProp", TakeFirstStringInSet.class);
            submissionLineProp.setBound(true);
            submissionLineProp.setPropertyEditorClass(PropertyNameLabelEditor.class);
            submissionLineProp.setDisplayName("<html><font color='green'>Submission String:");
            submissionLineProp.setShortDescription("Name of the property to put the submission into. ");
            
            PropertyDescriptor msStringSetProp = new PropertyDescriptor("msStringSetProp", TakeFirstStringInSet.class);
            msStringSetProp.setBound(true);
            msStringSetProp.setPropertyEditorClass(PropertyNameLabelEditor.class);
            msStringSetProp.setDisplayName("<html><font color='green'>Set:");
            msStringSetProp.setShortDescription("Name of the string set property. ");

            PropertyDescriptor rv[] = { submissionLineProp, msStringSetProp };
            return rv;
        } catch (IntrospectionException e) {
            throw new Error(e.toString());
        }
    }

    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(TakeFirstStringInSet.class);
        bd.setDisplayName("<html><font color='green'><center>Take First String<br>In Set");
        return bd;
    }

}
