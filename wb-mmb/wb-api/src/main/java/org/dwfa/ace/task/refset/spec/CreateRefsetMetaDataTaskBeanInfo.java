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
package org.dwfa.ace.task.refset.spec;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.ace.prop.editor.ConceptLabelPropEditor;
import org.dwfa.bpa.tasks.editor.PropertyNameLabelEditor;

/**
 * The CreateRefsetMetaDataTaskBeanInfo class describes the visible elements of
 * the
 * Workflow task CreateRefsetMetaDataTask so that it can be displayed in the
 * Process Builder.
 * The CreateRefsetMetaDataTask task creates meta data required for a new
 * refset..
 * 
 * @author Chrissy Hill
 * @author Perry Reid
 * @version 3, October 2009
 */
public class CreateRefsetMetaDataTaskBeanInfo extends SimpleBeanInfo {

    /**
     * Constructor - calls the parent constructor
     */
    public CreateRefsetMetaDataTaskBeanInfo() {
        super();
    }

    /**
     * Returns a list of property descriptors for this task.
     * 
     * @return Returns a PropertyDescriptor array containing the properties of
     *         this task
     * @exception Error Thrown when an exception happens during Introspection
     */
    public PropertyDescriptor[] getPropertyDescriptors() {

        try {
            PropertyDescriptor newRefsetPropName;
            newRefsetPropName = new PropertyDescriptor("newRefsetPropName", getBeanDescriptor().getBeanClass());
            newRefsetPropName.setBound(true);
            newRefsetPropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            newRefsetPropName.setDisplayName("<html><font color='green'>refset Name:");
            newRefsetPropName.setShortDescription("The property to put the refset name into.");

            PropertyDescriptor newRefsetUUIDPropName;
            newRefsetUUIDPropName = new PropertyDescriptor("newRefsetUUIDPropName", getBeanDescriptor().getBeanClass());
            newRefsetUUIDPropName.setBound(true);
            newRefsetUUIDPropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            newRefsetUUIDPropName.setDisplayName("<html><font color='green'>refset UUID:");
            newRefsetUUIDPropName.setShortDescription("The property to put the refset UUID into.");

            PropertyDescriptor newRefsetSpecUUIDPropName;
            newRefsetSpecUUIDPropName = new PropertyDescriptor("newRefsetSpecUUIDPropName",
                getBeanDescriptor().getBeanClass());
            newRefsetSpecUUIDPropName.setBound(true);
            newRefsetSpecUUIDPropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            newRefsetSpecUUIDPropName.setDisplayName("<html><font color='green'>refset spec UUID:");
            newRefsetSpecUUIDPropName.setShortDescription("The property to put the refset spec UUID into.");

            PropertyDescriptor statusTermEntry;
            statusTermEntry = new PropertyDescriptor("statusTermEntry", getBeanDescriptor().getBeanClass());
            statusTermEntry.setBound(true);
            statusTermEntry.setPropertyEditorClass(ConceptLabelPropEditor.class);
            statusTermEntry.setDisplayName("<html><font color='green'>status concept to use:");
            statusTermEntry.setShortDescription("The status concept to use.");

            PropertyDescriptor reviewerUuidPropName;
            reviewerUuidPropName = new PropertyDescriptor("reviewerUuidPropName", getBeanDescriptor().getBeanClass());
            reviewerUuidPropName.setBound(true);
            reviewerUuidPropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            reviewerUuidPropName.setDisplayName("<html><font color='green'>reviewer prop name:");
            reviewerUuidPropName.setShortDescription("The property to put the reviewer uuid  into.");

            PropertyDescriptor ownerUuidPropName;
            ownerUuidPropName = new PropertyDescriptor("ownerUuidPropName", getBeanDescriptor().getBeanClass());
            ownerUuidPropName.setBound(true);
            ownerUuidPropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            ownerUuidPropName.setDisplayName("<html><font color='green'>owner uuid prop name:");
            ownerUuidPropName.setShortDescription("The property to put the owner uuid into.");

            PropertyDescriptor editorUuidPropName;
            editorUuidPropName = new PropertyDescriptor("editorUuidPropName", getBeanDescriptor().getBeanClass());
            editorUuidPropName.setBound(true);
            editorUuidPropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            editorUuidPropName.setDisplayName("<html><font color='green'>editor UUID prop name:");
            editorUuidPropName.setShortDescription("The property to put the editor UUID into.");

            PropertyDescriptor rv[] = { newRefsetPropName, newRefsetUUIDPropName, newRefsetSpecUUIDPropName,
                                       statusTermEntry, reviewerUuidPropName, ownerUuidPropName, editorUuidPropName };
            return rv;
        } catch (IntrospectionException e) {
            throw new Error(e.toString());
        }
    }

    /**
     * Return the descriptor for this JavaBean which contains a reference to the
     * JavaBean
     * that implements this task as well as the display name of the task along
     * with formating information.
     * 
     * @see java.beans.BeanInfo#getBeanDescriptor()
     * @return Returns the BeanDescriptor for this task
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(CreateRefsetMetaDataTask.class);
        bd.setDisplayName("<html><font color='green'><center>Create refset<br>meta data");
        return bd;
    }

}
