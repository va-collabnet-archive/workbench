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
package org.dwfa.ace.task.profile.cap;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;

import org.dwfa.ace.task.wfpanel.PreviousNextOrCancelBeanInfo;
import org.dwfa.bpa.tasks.editor.PropertyNameLabelEditor;

public class SetNewCapUserDevPathAsOriginBeanInfo extends PreviousNextOrCancelBeanInfo {

    public PropertyDescriptor[] getPropertyDescriptors() {
        try {
            PropertyDescriptor addToPathOriginPropName = new PropertyDescriptor("addToPathOriginPropName",
                getBeanDescriptor().getBeanClass());
            addToPathOriginPropName.setBound(true);
            addToPathOriginPropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            addToPathOriginPropName.setDisplayName("<html><font color='green'>Add new path as origin:");
            addToPathOriginPropName.setShortDescription("The property name to add new dev path as origin.");

            PropertyDescriptor newProfilePropName = new PropertyDescriptor("newProfilePropName",
                    getBeanDescriptor().getBeanClass());
                newProfilePropName.setBound(true);
                newProfilePropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
                newProfilePropName.setDisplayName("<html><font color='green'>new profile prop:");
                newProfilePropName.setShortDescription("The property that contains the new profile.");


            PropertyDescriptor rv[] = { addToPathOriginPropName, newProfilePropName };
            return rv;
        } catch (IntrospectionException e) {
            throw new Error(e.toString());
        }
    }


    /** 
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(SetNewCapUserDevPathAsOrigin.class);
        bd.setDisplayName("<html><font color='green'><center>Add New Dev Path as<br>for new intl user");
        return bd;
    }

}
