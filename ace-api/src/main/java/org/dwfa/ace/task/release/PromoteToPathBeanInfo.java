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
package org.dwfa.ace.task.release;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.bpa.tasks.editor.PropertyNameLabelEditor;

public class PromoteToPathBeanInfo extends SimpleBeanInfo {

    /**
     * 
     */
    public PromoteToPathBeanInfo() {
        super();
    }

    public PropertyDescriptor[] getPropertyDescriptors() {
        try {
            PropertyDescriptor pathPropName = new PropertyDescriptor("pathPropName", getBeanDescriptor().getBeanClass());
            pathPropName.setBound(true);
            pathPropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            pathPropName.setDisplayName("<html><font color='green'>path concept property:");
            pathPropName.setShortDescription("The path to be updated");

            PropertyDescriptor profilePropName = new PropertyDescriptor("profilePropName",
                getBeanDescriptor().getBeanClass());
            profilePropName.setBound(true);
            profilePropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            profilePropName.setDisplayName("<html><font color='green'>profile property");
            profilePropName.setShortDescription("The property containing the profile to be used");

            PropertyDescriptor rv[] = { pathPropName, profilePropName };
            return rv;

        } catch (IntrospectionException e) {
            throw new Error(e.toString());
        }
    }

    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(PromoteToPath.class);
        bd.setDisplayName("<html><font color='green'><center>Promote users work<br/>to release path");
        return bd;
    }
}
