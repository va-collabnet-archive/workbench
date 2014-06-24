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
package org.dwfa.ace.task.list;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.bpa.tasks.editor.PropertyNameLabelEditor;

public class IcsListMaintenanceBeanInfo extends SimpleBeanInfo {

    /**
     * Bean info for IcsListMaintenanceBeanInfo class.
     * 
     * @author akf
     * 
     */
    public IcsListMaintenanceBeanInfo() {
        super();
    }

    public PropertyDescriptor[] getPropertyDescriptors() {
        try {
            PropertyDescriptor icsListFileNameProp = new PropertyDescriptor("icsListFileNameProp",
                IcsListMaintenance.class);
            icsListFileNameProp.setBound(true);
            icsListFileNameProp.setDisplayName("<html><font color='green'>Update File:");
            icsListFileNameProp.setShortDescription("update file");
            icsListFileNameProp.setPropertyEditorClass(PropertyNameLabelEditor.class);

            PropertyDescriptor rv[] = { icsListFileNameProp };
            return rv;
        } catch (IntrospectionException e) {
            throw new Error(e.toString());
        }
    }

    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(IcsListMaintenance.class);
        bd.setDisplayName("<html><font color='green'><center>Update<br>ICS List");
        return bd;
    }

}
