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

public class SetNewCapUserPathsForViewBeanInfo extends PreviousNextOrCancelBeanInfo {

    public PropertyDescriptor[] getPropertyDescriptors() {
        try {
            PropertyDescriptor pathsForViewPropName = new PropertyDescriptor("pathsForViewPropName",
                getBeanDescriptor().getBeanClass());
            pathsForViewPropName.setBound(true);
            pathsForViewPropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            pathsForViewPropName.setDisplayName("<html><font color='green'>View paths prop:");
            pathsForViewPropName.setShortDescription("The property name to hold the view paths for new user.");

            PropertyDescriptor rv[] = { pathsForViewPropName };
            return rv;
        } catch (IntrospectionException e) {
            throw new Error(e.toString());
        }
    }


    /** 
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(SetNewCapUserPathsForView.class);
        bd.setDisplayName("<html><font color='green'><center>Set View Path<br>for new intl user");
        return bd;
    }

}
