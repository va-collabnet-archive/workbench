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
package org.ihtsdo.arena.task;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.bpa.tasks.editor.PropertyNameLabelEditor;

public class CreateReportBeanInfo extends SimpleBeanInfo {

    /**
     * Bean info for CreateReport class.
     * 
     * @author akf
     * 
     */
    public CreateReportBeanInfo() {
        super();
    }

//    public PropertyDescriptor[] getPropertyDescriptors() {
//        try {
//            PropertyDescriptor begDateProp = new PropertyDescriptor("begDateProp",
//                CreateReport.class);
//            PropertyDescriptor endDateProp = new PropertyDescriptor("endDateProp",
//                CreateReport.class);
//            
//            begDateProp.setBound(true);
//            begDateProp.setDisplayName("<html><font color='green'>Start Date:");
//            begDateProp.setShortDescription("html file");
//            begDateProp.setPropertyEditorClass(PropertyNameLabelEditor.class);
//            
//            endDateProp.setBound(true);
//            endDateProp.setDisplayName("<html><font color='green'>End Date:");
//            endDateProp.setShortDescription("html file");
//            endDateProp.setPropertyEditorClass(PropertyNameLabelEditor.class);
//
//            PropertyDescriptor rv[] = { begDateProp, endDateProp };
//            return rv;
//        } catch (IntrospectionException e) {
//            throw new Error(e.toString());
//        }
//    }

    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(CreateReport.class);
        bd.setDisplayName("<html><font color='green'><center>Create Report");
        return bd;
    }

}
