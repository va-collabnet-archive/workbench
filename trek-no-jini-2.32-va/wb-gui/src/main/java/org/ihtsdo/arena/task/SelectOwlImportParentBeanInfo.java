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

public class SelectOwlImportParentBeanInfo extends SimpleBeanInfo {

    /**
     * Bean info for ImportOwlConcepts class.
     * 
     * @author akf
     * 
     */
    public SelectOwlImportParentBeanInfo() {
        super();
    }

    @Override
    public PropertyDescriptor[] getPropertyDescriptors() {
        try {
            PropertyDescriptor parentConceptPropName = new PropertyDescriptor("parentConceptPropName",
                SelectOwlImportParent.class);
            parentConceptPropName.setBound(true);
            parentConceptPropName.setDisplayName("<html><font color='green'>Parent concept:");
            parentConceptPropName.setShortDescription("parent concept");
            parentConceptPropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            
            PropertyDescriptor inputFilePropName = new PropertyDescriptor("inputFilePropName",
                SelectOwlImportParent.class);
            inputFilePropName.setBound(true);
            inputFilePropName.setDisplayName("<html><font color='green'>File path:");
            inputFilePropName.setShortDescription("file path");
            inputFilePropName.setPropertyEditorClass(PropertyNameLabelEditor.class);

            PropertyDescriptor rv[] = { parentConceptPropName, inputFilePropName };
            return rv;
        } catch (IntrospectionException e) {
            throw new Error(e.toString());
        }
    }

    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(SelectOwlImportParent.class);
        bd.setDisplayName("<html><font color='green'><center>Select Import Parent Concept");
        return bd;
    }

}
