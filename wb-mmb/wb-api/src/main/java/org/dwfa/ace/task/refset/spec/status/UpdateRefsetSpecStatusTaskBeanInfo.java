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
package org.dwfa.ace.task.refset.spec.status;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.ace.prop.editor.ConceptLabelPropEditor;
import org.dwfa.bpa.tasks.editor.CheckboxEditor;
import org.dwfa.bpa.tasks.editor.PropertyNameLabelEditor;

public class UpdateRefsetSpecStatusTaskBeanInfo extends SimpleBeanInfo {

    /**
     *
     */
    public UpdateRefsetSpecStatusTaskBeanInfo() {
        super();
    }

    public PropertyDescriptor[] getPropertyDescriptors() {

        try {

            PropertyDescriptor refsetSpecUuidPropName;
            refsetSpecUuidPropName = new PropertyDescriptor("refsetSpecUuidPropName",
                getBeanDescriptor().getBeanClass());
            refsetSpecUuidPropName.setBound(true);
            refsetSpecUuidPropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            refsetSpecUuidPropName.setDisplayName("<html><font color='green'>Refset spec UUID prop:");
            refsetSpecUuidPropName.setShortDescription("The refset spec UUID prop.");

            PropertyDescriptor statusTermEntry;
            statusTermEntry = new PropertyDescriptor("statusTermEntry", getBeanDescriptor().getBeanClass());
            statusTermEntry.setBound(true);
            statusTermEntry.setPropertyEditorClass(ConceptLabelPropEditor.class);
            statusTermEntry.setDisplayName("<html><font color='green'>status concept to use:");
            statusTermEntry.setShortDescription("The status concept to use.");

            PropertyDescriptor includeMetaDataConcepts;
            includeMetaDataConcepts = new PropertyDescriptor("includeMetaDataConcepts",
                getBeanDescriptor().getBeanClass());
            includeMetaDataConcepts.setBound(true);
            includeMetaDataConcepts.setPropertyEditorClass(CheckboxEditor.class);
            includeMetaDataConcepts.setDisplayName("<html><font color='green'>include meta data concepts:");
            includeMetaDataConcepts.setShortDescription("include meta data concepts.");

            PropertyDescriptor includeMetaDataRelationships;
            includeMetaDataRelationships = new PropertyDescriptor("includeMetaDataRelationships",
                getBeanDescriptor().getBeanClass());
            includeMetaDataRelationships.setBound(true);
            includeMetaDataRelationships.setPropertyEditorClass(CheckboxEditor.class);
            includeMetaDataRelationships.setDisplayName("<html><font color='green'>include meta data rels:");
            includeMetaDataRelationships.setShortDescription("include meta data rels.");

            PropertyDescriptor includeMetaDataDescriptions;
            includeMetaDataDescriptions = new PropertyDescriptor("includeMetaDataDescriptions",
                getBeanDescriptor().getBeanClass());
            includeMetaDataDescriptions.setBound(true);
            includeMetaDataDescriptions.setPropertyEditorClass(CheckboxEditor.class);
            includeMetaDataDescriptions.setDisplayName("<html><font color='green'>include meta data descs:");
            includeMetaDataDescriptions.setShortDescription("include meta data descs.");

            PropertyDescriptor includeSpecExtensions;
            includeSpecExtensions = new PropertyDescriptor("includeSpecExtensions", getBeanDescriptor().getBeanClass());
            includeSpecExtensions.setBound(true);
            includeSpecExtensions.setPropertyEditorClass(CheckboxEditor.class);
            includeSpecExtensions.setDisplayName("<html><font color='green'>include spec exts:");
            includeSpecExtensions.setShortDescription("include spec exts.");

            PropertyDescriptor includeMemberExtensions;
            includeMemberExtensions = new PropertyDescriptor("includeMemberExtensions",
                getBeanDescriptor().getBeanClass());
            includeMemberExtensions.setBound(true);
            includeMemberExtensions.setPropertyEditorClass(CheckboxEditor.class);
            includeMemberExtensions.setDisplayName("<html><font color='green'>include member exts:");
            includeMemberExtensions.setShortDescription("include member exts.");

            PropertyDescriptor includeParentExtensions;
            includeParentExtensions = new PropertyDescriptor("includeParentExtensions",
                getBeanDescriptor().getBeanClass());
            includeParentExtensions.setBound(true);
            includeParentExtensions.setPropertyEditorClass(CheckboxEditor.class);
            includeParentExtensions.setDisplayName("<html><font color='green'>include parent exts:");
            includeParentExtensions.setShortDescription("include parent exts.");

            PropertyDescriptor includeCommentExtensions;
            includeCommentExtensions = new PropertyDescriptor("includeCommentExtensions",
                getBeanDescriptor().getBeanClass());
            includeCommentExtensions.setBound(true);
            includeCommentExtensions.setPropertyEditorClass(CheckboxEditor.class);
            includeCommentExtensions.setDisplayName("<html><font color='green'>include comment exts:");
            includeCommentExtensions.setShortDescription("include comment exts.");

            PropertyDescriptor includePromotionExtensions;
            includePromotionExtensions = new PropertyDescriptor("includePromotionExtensions",
                getBeanDescriptor().getBeanClass());
            includePromotionExtensions.setBound(true);
            includePromotionExtensions.setPropertyEditorClass(CheckboxEditor.class);
            includePromotionExtensions.setDisplayName("<html><font color='green'>include promotion exts:");
            includePromotionExtensions.setShortDescription("include promotion exts.");

            PropertyDescriptor rv[] = { refsetSpecUuidPropName, statusTermEntry, includeMetaDataConcepts,
                                       includeMetaDataRelationships, includeMetaDataDescriptions,
                                       includeSpecExtensions, includeMemberExtensions, includeParentExtensions,
                                       includeCommentExtensions, includePromotionExtensions };
            return rv;
        } catch (IntrospectionException e) {
            throw new Error(e.toString());
        }
    }

    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(UpdateRefsetSpecStatusTask.class);
        bd.setDisplayName("<html><font color='green'><center>Update refset spec<br>status");
        return bd;
    }

}
