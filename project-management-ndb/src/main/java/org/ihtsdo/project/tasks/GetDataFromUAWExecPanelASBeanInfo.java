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
package org.ihtsdo.project.tasks;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.bpa.tasks.editor.PropertyNameLabelEditor;

/**
 * 
 */
public class GetDataFromUAWExecPanelASBeanInfo extends SimpleBeanInfo {

    public GetDataFromUAWExecPanelASBeanInfo() {
        super();
    }

    /**
     * Returns a list of property descriptors for this task.
     * 
     * @return Returns a PropertyDescriptor array containing the properties of this task
     * @exception Error Thrown when an exception happens during Introspection
     */
    public PropertyDescriptor[] getPropertyDescriptors() {

        try {

            PropertyDescriptor profilePropName =
                    new PropertyDescriptor("profilePropName", getBeanDescriptor().getBeanClass());
            profilePropName.setBound(true);
            profilePropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            profilePropName.setDisplayName("<html><font color='green'>profile prop:");
            profilePropName.setShortDescription("[IN] The property that contains the working profile.");
            
            PropertyDescriptor memberPropName =
            	new PropertyDescriptor("memberPropName", getBeanDescriptor().getBeanClass());
            memberPropName.setBound(true);
            memberPropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            memberPropName.setDisplayName("<html><font color='green'>member prop:");
            memberPropName.setShortDescription("[IN] The property that contains the member.");

            PropertyDescriptor projectPropName =
            	new PropertyDescriptor("projectPropName", getBeanDescriptor().getBeanClass());
            projectPropName.setBound(true);
            projectPropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            projectPropName.setDisplayName("<html><font color='green'>project prop:");
            projectPropName.setShortDescription("[IN] The property that contains the project.");
            
            PropertyDescriptor rv[] =
                    { profilePropName, memberPropName,projectPropName };
            return rv;
        } catch (IntrospectionException e) {
            throw new Error(e.toString());
        }
    }

    /**
     * Return the descriptor for this JavaBean which contains a reference to the JavaBean
     * that implements this task as well as the display name of the task along with
     * formating information.
     * 
     * @see java.beans.BeanInfo#getBeanDescriptor()
     * @return Returns the BeanDescriptor for this task
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(GetDataFromUAWExecPanelAS.class);
        bd.setDisplayName("<html><font color='green'><center>Get data<br>from UAW panel<br>continue or exec AS");
        return bd;
    }

}
