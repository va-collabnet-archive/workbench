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

public class ExportRefsetToFileBeanInfo extends SimpleBeanInfo {

    public PropertyDescriptor[] getPropertyDescriptors() {
        try {

            PropertyDescriptor refsetConceptPropName = 
                new PropertyDescriptor("refsetConceptPropName", getBeanDescriptor().getBeanClass());
            refsetConceptPropName.setBound(true);
            refsetConceptPropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            refsetConceptPropName.setDisplayName("<html><font color='green'>Refset concept property:");
            refsetConceptPropName.setShortDescription("The property containing the refset concept. ");

            PropertyDescriptor importFileName = 
                new PropertyDescriptor("importFileName", getBeanDescriptor().getBeanClass());
            importFileName.setBound(true);
            importFileName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            importFileName.setDisplayName("<html><font color='green'>File name property:");
            importFileName.setShortDescription("The property containing the name of the file to be imported. ");

            PropertyDescriptor conceptExtValuePropName = 
                new PropertyDescriptor("conceptExtValuePropName", getBeanDescriptor().getBeanClass());
            conceptExtValuePropName.setBound(true);
            conceptExtValuePropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            conceptExtValuePropName.setDisplayName("<html><font color='green'>Extension value concept property:");
            conceptExtValuePropName.setShortDescription("Only extensions with this attribute value will be exported from the refset");

            PropertyDescriptor rv[] = { refsetConceptPropName, importFileName, conceptExtValuePropName };
            return rv;
            
        } catch (IntrospectionException e) {
            throw new Error(e.toString());
        }
    }

    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(ExportRefsetToFile.class);
        bd.setDisplayName("<html><font color='green'><center>Export refset to file");
        return bd;
    }

}
