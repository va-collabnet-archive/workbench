/**
 *  Copyright (c) 2009 International Health Terminology Standards Development Organisation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.dwfa.ace.task.gui.batchlist;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.dwfa.bpa.tasks.editor.CheckboxEditor;
import org.dwfa.bpa.tasks.editor.PropertyNameLabelEditor;

public class AddUuidListListToListViewBeanInfo extends SimpleBeanInfo {

    public PropertyDescriptor[] getPropertyDescriptors() {
        try {
            List<PropertyDescriptor> propertyDescriptors = new ArrayList<PropertyDescriptor>();

            PropertyDescriptor uuidListListPropName =
                    new PropertyDescriptor("uuidListListPropName",
                    getBeanDescriptor().getBeanClass());
            uuidListListPropName.setBound(true);
            uuidListListPropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            uuidListListPropName.setDisplayName("<html><font color='green'>Uuid List:");
            uuidListListPropName.setShortDescription("Uuid list.");
            propertyDescriptors.add(uuidListListPropName);

            PropertyDescriptor continueOnError =
                    new PropertyDescriptor("continueOnError", AddUuidListListToListView.class);
            continueOnError.setBound(true);
            continueOnError.setPropertyEditorClass(CheckboxEditor.class);
            continueOnError.setDisplayName("<html><font color='green'>Continue on error:");
            continueOnError.setShortDescription("Don't Fail task if an error is encountered.");
            propertyDescriptors.add(continueOnError);

            return propertyDescriptors.toArray(new PropertyDescriptor[propertyDescriptors.size()]);
        } catch (IntrospectionException e) {
            throw new Error(e.toString());
        }
    }

    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(AddUuidListListToListView.class);
        bd.setDisplayName("<html><font color='green'><center>Add UUID List list <br>to list view");
        return bd;
    }
}
