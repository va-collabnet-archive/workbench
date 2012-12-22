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
package org.ihtsdo.project.tasks;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.bpa.tasks.editor.CheckboxEditor;
import org.dwfa.bpa.tasks.editor.PropertyNameLabelEditor;

/**
 * The Class GetDeliveryOrSelfAssignBeanInfo.
 */
public class GetDeliveryOrSelfAssignBeanInfo extends SimpleBeanInfo {

    /**
     * Instantiates a new gets the delivery or self assign bean info.
     */
    public GetDeliveryOrSelfAssignBeanInfo() {
        super();
    }

    /**
     * Returns a list of property descriptors for this task.
     *
     * @return Returns a PropertyDescriptor array containing the properties of
     * this task
     */
    public PropertyDescriptor[] getPropertyDescriptors() {

        try {

            PropertyDescriptor profilePropName =
                    new PropertyDescriptor("profilePropName", getBeanDescriptor().getBeanClass());
            profilePropName.setBound(true);
            profilePropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            profilePropName.setDisplayName("<html><font color='green'>profile prop:");
            profilePropName.setShortDescription("[IN] The property that contains the working profile.");

            PropertyDescriptor selfAssign = new PropertyDescriptor("selfAssign",
                    getBeanDescriptor().getBeanClass());
            selfAssign.setBound(true);
            selfAssign.setPropertyEditorClass(CheckboxEditor.class);
            selfAssign.setDisplayName("Is self assign");
            selfAssign.setShortDescription("Is self assign");
            PropertyDescriptor rv[] = {profilePropName, selfAssign};
            return rv;
        } catch (IntrospectionException e) {
            throw new Error(e.toString());
        }
    }

    /**
     * Return the descriptor for this JavaBean which contains a reference to the
     * JavaBean that implements this task as well as the display name of the
     * task along with formating information.
     *
     * @return Returns the BeanDescriptor for this task
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(GetDeliveryOrSelfAssign.class);
        bd.setDisplayName("<html><font color='green'><center>Get Delivery<br>or Self Assign");
        return bd;
    }
}
