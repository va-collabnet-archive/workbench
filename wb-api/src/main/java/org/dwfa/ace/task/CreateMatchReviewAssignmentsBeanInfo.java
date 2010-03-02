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
package org.dwfa.ace.task;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.bpa.tasks.editor.PropertyNameLabelEditor;

/**
 * Bean info for CreateMatchReviewAssignments class.
 * 
 * @author Eric Mays (EKM)
 * 
 */
public class CreateMatchReviewAssignmentsBeanInfo extends SimpleBeanInfo {

    /**
     *
     */
    public CreateMatchReviewAssignmentsBeanInfo() {
        super();
    }

    public PropertyDescriptor[] getPropertyDescriptors() {
        try {
            PropertyDescriptor inputFileNamePropName = new PropertyDescriptor("inputFileNamePropName",
                CreateMatchReviewAssignments.class);
            inputFileNamePropName.setBound(true);
            inputFileNamePropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            inputFileNamePropName.setDisplayName("<html><font color='green'>Input File Name:");
            inputFileNamePropName.setShortDescription("File Name");

            PropertyDescriptor bpFileNamePropName = new PropertyDescriptor("bpFileNamePropName",
                CreateMatchReviewAssignments.class);
            bpFileNamePropName.setBound(true);
            bpFileNamePropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            bpFileNamePropName.setDisplayName("<html><font color='green'>BP Input File Name:");
            bpFileNamePropName.setShortDescription("File Name");

            PropertyDescriptor rv[] = { inputFileNamePropName, bpFileNamePropName };
            return rv;
        } catch (IntrospectionException e) {
            throw new Error(e.toString());
        }
    }

    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(CreateMatchReviewAssignments.class);
        bd.setDisplayName("<html><font color='green'><center>Create Match Review <br> Assignments");
        return bd;
    }

}
