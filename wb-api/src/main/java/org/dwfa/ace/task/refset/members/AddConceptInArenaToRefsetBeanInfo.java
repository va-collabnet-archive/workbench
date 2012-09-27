/**
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

import org.dwfa.ace.prop.editor.ConceptLabelPropEditor;

public class AddConceptInArenaToRefsetBeanInfo extends SimpleBeanInfo {

    public PropertyDescriptor[] getPropertyDescriptors() {
        try {

            PropertyDescriptor refset = new PropertyDescriptor("refset",
            		getBeanDescriptor().getBeanClass()); 
            refset.setBound(true);
            refset.setPropertyEditorClass(ConceptLabelPropEditor.class);
            refset.setDisplayName("<html><font color='green'>Refset concept:");
            refset.setShortDescription("Selects the refset to which the concept will be added. ");


            PropertyDescriptor memberType = new PropertyDescriptor("memberType",
            		getBeanDescriptor().getBeanClass()); 
            memberType.setBound(true);
            memberType.setPropertyEditorClass(ConceptLabelPropEditor.class);
            memberType.setDisplayName("<html><font color='green'>Member Type:");
            memberType.setShortDescription("Selects the member type for the concecpt.");

            PropertyDescriptor rv[] = { refset, memberType };
            return rv;
        } catch (IntrospectionException e) {
            throw new Error(e.toString());
        }
    }

    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(AddConceptInArenaToRefset.class); 
        bd.setDisplayName("<html><font color='green'><center>Add Concept in Arena to Refset"); 
        return bd;
    }

}