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
package org.dwfa.ace.task.refset.members;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.bpa.tasks.editor.PropertyNameLabelEditor;

public class RefreshMemberRefsetUsingListViewConceptsBeanInfo extends SimpleBeanInfo {

    public PropertyDescriptor[] getPropertyDescriptors() {
        try {

            PropertyDescriptor refsetConceptPropName = new PropertyDescriptor("refsetConceptPropName",
                getBeanDescriptor().getBeanClass());
            refsetConceptPropName.setBound(true);
            refsetConceptPropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            refsetConceptPropName.setDisplayName("<html><font color='green'>Refset concept property:");
            refsetConceptPropName.setShortDescription("The property containing the refset concept. ");

            PropertyDescriptor conceptExtValuePropName = new PropertyDescriptor("conceptExtValuePropName",
                getBeanDescriptor().getBeanClass());
            conceptExtValuePropName.setBound(true);
            conceptExtValuePropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            conceptExtValuePropName.setDisplayName("<html><font color='green'>Extension value concept property:");
            conceptExtValuePropName.setShortDescription("The property containing the value for the new concept extension. ");

            PropertyDescriptor rv[] = { refsetConceptPropName, conceptExtValuePropName };
            return rv;
        } catch (IntrospectionException e) {
            throw new Error(e.toString());
        }
    }

    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(RefreshMemberRefsetUsingListViewConcepts.class);
        bd.setDisplayName("<html><font color='green'><center>Refresh member refset<br>using list view concepts");
        return bd;
    }

}
