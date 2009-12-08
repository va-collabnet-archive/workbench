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
package org.dwfa.ace.task.file;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.bpa.tasks.editor.PropertyNameLabelEditor;

public class WriteConceptListFileBeanInfo extends SimpleBeanInfo {

    /**
     * 
     */
    public WriteConceptListFileBeanInfo() {
        super();
    }

    public PropertyDescriptor[] getPropertyDescriptors() {
        try {
            PropertyDescriptor outputFilePropName = new PropertyDescriptor("outputFilePropName",
                WriteConceptListFile.class);
            outputFilePropName.setBound(true);
            outputFilePropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            outputFilePropName.setDisplayName("<html><font color='green'>Output file property:");
            outputFilePropName.setShortDescription("Name of the property containing the filename to export to. ");

            PropertyDescriptor conceptListPropName = new PropertyDescriptor("conceptListPropName",
                WriteConceptListFile.class);
            conceptListPropName.setBound(true);
            conceptListPropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            conceptListPropName.setDisplayName("<html><font color='green'>Concept list property:");
            conceptListPropName.setShortDescription("Name of the property containing the concept list to export. ");

            PropertyDescriptor errorMessagePropName = new PropertyDescriptor("errorMessagePropName",
                WriteConceptListFile.class);
            errorMessagePropName.setBound(true);
            errorMessagePropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            errorMessagePropName.setDisplayName("<html><font color='green'>Error message property:");
            errorMessagePropName.setShortDescription("Name of the property set by this task containing an error message if the export failed. ");

            PropertyDescriptor rv[] = { outputFilePropName, conceptListPropName, errorMessagePropName };
            return rv;
        } catch (IntrospectionException e) {
            throw new Error(e.toString());
        }
    }

    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(WriteConceptListFile.class);
        bd.setDisplayName("<html><font color='green'><center>Write concept list file");
        return bd;
    }

}
