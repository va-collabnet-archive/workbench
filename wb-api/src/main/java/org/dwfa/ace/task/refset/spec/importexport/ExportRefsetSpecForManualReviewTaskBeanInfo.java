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
package org.dwfa.ace.task.refset.spec.importexport;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.ace.prop.editor.ConceptLabelPropEditor;
import org.dwfa.bpa.tasks.editor.IncrementEditor;
import org.dwfa.bpa.tasks.editor.PropertyNameLabelEditor;

public class ExportRefsetSpecForManualReviewTaskBeanInfo extends SimpleBeanInfo {

    /**
     *
     */
    public ExportRefsetSpecForManualReviewTaskBeanInfo() {
        super();
    }

    public PropertyDescriptor[] getPropertyDescriptors() {
        PropertyDescriptor outputFilePropName;
        try {
            outputFilePropName = new PropertyDescriptor("outputFilePropName", getBeanDescriptor().getBeanClass());
            outputFilePropName.setBound(true);
            outputFilePropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            outputFilePropName.setDisplayName("<html><font color='green'>Output file property:");
            outputFilePropName.setShortDescription("Name of the property containing the filename to export to. ");

            PropertyDescriptor refsetSpecUuidPropName = new PropertyDescriptor("refsetSpecUuidPropName",
                getBeanDescriptor().getBeanClass());
            refsetSpecUuidPropName.setBound(true);
            refsetSpecUuidPropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            refsetSpecUuidPropName.setDisplayName("<html><font color='green'>refset spec uuid property:");
            refsetSpecUuidPropName.setShortDescription("Name of the property containing the refset spec uuid. ");

            PropertyDescriptor descriptionTypeTermEntry;
            descriptionTypeTermEntry = new PropertyDescriptor("descriptionTypeTermEntry",
                getBeanDescriptor().getBeanClass());
            descriptionTypeTermEntry.setBound(true);
            descriptionTypeTermEntry.setPropertyEditorClass(ConceptLabelPropEditor.class);
            descriptionTypeTermEntry.setDisplayName("<html><font color='green'>desc type concept to use:");
            descriptionTypeTermEntry.setShortDescription("The desc type concept to use.");

            PropertyDescriptor maxLineCount;
            maxLineCount = new PropertyDescriptor("maxLineCount", getBeanDescriptor().getBeanClass());
            maxLineCount.setBound(true);
            maxLineCount.setPropertyEditorClass(IncrementEditor.class);
            maxLineCount.setDisplayName("<html><font color='green'>max line count in exported file:");
            maxLineCount.setShortDescription("The max line count in exported file.");

            PropertyDescriptor rv[] = { outputFilePropName, descriptionTypeTermEntry, maxLineCount,
                                       refsetSpecUuidPropName };
            return rv;
        } catch (IntrospectionException e) {
            throw new Error(e.toString());
        }
    }

    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(ExportRefsetSpecForManualReviewTask.class);
        bd.setDisplayName("<html><font color='green'><center>Export refset spec for<br>manual review");
        return bd;
    }

}
