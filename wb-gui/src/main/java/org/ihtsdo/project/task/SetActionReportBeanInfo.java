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
package org.ihtsdo.project.task;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

/**
 * The Class SetActionReportBeanInfo.
 */
public class SetActionReportBeanInfo extends SimpleBeanInfo {

    /* (non-Javadoc)
     * @see java.beans.SimpleBeanInfo#getPropertyDescriptors()
     */
    public PropertyDescriptor[] getPropertyDescriptors() {
        try {

            PropertyDescriptor type = new PropertyDescriptor("actionReport", getBeanDescriptor().getBeanClass());
            type.setBound(true);
            type.setPropertyEditorClass(ActionReportEditor.class);
            type.setDisplayName("<html><font color='green'>Action Report:");
            type.setShortDescription("The action to report.");


            PropertyDescriptor rv[] = {type};
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
        BeanDescriptor bd = new BeanDescriptor(SetActionReport.class);
        bd.setDisplayName("<html><font color='green'><center>Set Action Report");
        return bd;
    }
}
