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
package org.ihtsdo.ace.task.gui.batchlist;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.bpa.tasks.editor.PropertyNameLabelEditor;



/* 
* @author Varsha Parekh
* 
*/
public class AddRefsetToListViewBeanInfo extends SimpleBeanInfo {

    public PropertyDescriptor[] getPropertyDescriptors() {

        try {

            PropertyDescriptor conceptPropName = new PropertyDescriptor("conceptPropName",
                getBeanDescriptor().getBeanClass());
            conceptPropName.setBound(true);
            conceptPropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            conceptPropName.setDisplayName("<html><font color='green'>concept prop:");
            conceptPropName.setShortDescription("The property to hold the concept.");

            PropertyDescriptor rv[] = { conceptPropName };
            return rv;
        } catch (IntrospectionException e) {
            throw new Error(e.toString());
        }
    }

    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(AddRefsetToListView.class);
        bd.setDisplayName("<html><font color='green'><center>Add Refset Members<br>to List View");
        return bd;
    }

}
