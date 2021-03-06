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
 * Bean info for TakeFirstItemInAttachmentListReturnUUID class.
 * 
 * @author Susan Castillo
 * 
 */
public class TakeFirstItemInAttachmentListListReturnUUIDBeanInfo extends SimpleBeanInfo {

    /**
     *
     */
    public TakeFirstItemInAttachmentListListReturnUUIDBeanInfo() {
        super();
    }

    public PropertyDescriptor[] getPropertyDescriptors() {
        try {
            PropertyDescriptor listListNamePropName = new PropertyDescriptor("listListNamePropName",
                TakeFirstItemInAttachmentListListReturnUUID.class);
            listListNamePropName.setBound(true);
            listListNamePropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            listListNamePropName.setDisplayName("<html><font color='green'>Name list of lists:");
            listListNamePropName.setShortDescription("Name of list of lists.");

            PropertyDescriptor uuidListPropName = new PropertyDescriptor("uuidListPropName",
                TakeFirstItemInAttachmentListListReturnUUID.class);
            uuidListPropName.setBound(true);
            uuidListPropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            uuidListPropName.setDisplayName("<html><font color='green'>Concept UUID:");
            uuidListPropName.setShortDescription("Concept UUID.");

            PropertyDescriptor rv[] = { listListNamePropName, uuidListPropName };
            return rv;
        } catch (IntrospectionException e) {
            throw new Error(e.toString());
        }
    }

    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(TakeFirstItemInAttachmentListListReturnUUID.class);
        bd.setDisplayName("<html><font color='green'><center>Take 1st Item<br>In Attachment<br> List of Lists<br> Return  UUID");
        return bd;
    }

}
