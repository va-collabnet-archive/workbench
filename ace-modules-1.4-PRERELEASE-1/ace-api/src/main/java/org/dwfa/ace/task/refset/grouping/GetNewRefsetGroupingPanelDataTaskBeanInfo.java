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
package org.dwfa.ace.task.refset.grouping;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.ace.prop.editor.ConceptLabelPropEditor;
import org.dwfa.bpa.tasks.editor.PropertyNameLabelEditor;

public class GetNewRefsetGroupingPanelDataTaskBeanInfo extends SimpleBeanInfo {

    /**
     *
     */
    public GetNewRefsetGroupingPanelDataTaskBeanInfo() {
        super();
    }

    public PropertyDescriptor[] getPropertyDescriptors() {

        try {

            PropertyDescriptor groupingConceptUuidPropName;
            groupingConceptUuidPropName = new PropertyDescriptor("groupingConceptUuidPropName", getBeanDescriptor().getBeanClass());
            groupingConceptUuidPropName.setBound(true);
            groupingConceptUuidPropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            groupingConceptUuidPropName.setDisplayName("<html><font color='green'>new concept UUID prop:");
            groupingConceptUuidPropName.setShortDescription("The property to put the new refset grouping concept UUID into.");

            PropertyDescriptor statusTermEntry;
            statusTermEntry = new PropertyDescriptor("statusTermEntry", getBeanDescriptor().getBeanClass());
            statusTermEntry.setBound(true);
            statusTermEntry.setPropertyEditorClass(ConceptLabelPropEditor.class);
            statusTermEntry.setDisplayName("<html><font color='green'>status concept to use:");
            statusTermEntry.setShortDescription("The status concept to use.");

            PropertyDescriptor rv[] = { groupingConceptUuidPropName, statusTermEntry };
            return rv;
        } catch (IntrospectionException e) {
            throw new Error(e.toString());
        }
    }

    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(GetNewRefsetGroupingPanelDataTask.class);
        bd.setDisplayName("<html><font color='green'><center>Get new refset grouping<br>panel data");
        return bd;
    }

}
