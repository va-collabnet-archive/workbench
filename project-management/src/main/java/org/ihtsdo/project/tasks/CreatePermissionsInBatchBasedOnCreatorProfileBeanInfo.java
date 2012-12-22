/**
 * Copyright (c) 2010 International Health Terminology Standards Development
 * Organisation
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.ihtsdo.project.tasks;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;
import org.dwfa.ace.prop.editor.ConceptLabelPropEditor;
//import org.dwfa.bpa.tasks.editor.JTextFieldEditor;

/**
 * The Class CreateIssueBeanInfo.
 */
public class CreatePermissionsInBatchBasedOnCreatorProfileBeanInfo extends SimpleBeanInfo {

    /**
     * Instantiates a new creates the issue bean info.
     */
    public CreatePermissionsInBatchBasedOnCreatorProfileBeanInfo() {
        super();
    }

    /* (non-Javadoc)
     * @see java.beans.SimpleBeanInfo#getPropertyDescriptors()
     */
    public PropertyDescriptor[] getPropertyDescriptors() {

        PropertyDescriptor rv[] = {};
        return rv;

    }

    /* (non-Javadoc)
     * @see java.beans.SimpleBeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(CreatePermissionsInBatchBasedOnCreatorProfile.class);
        bd.setDisplayName("<html><font color='green'><center>Create user permissions<br>based on profile");
        return bd;
    }
}
