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

import org.dwfa.ace.task.ShowFileInWebBrowser;
import org.dwfa.bpa.tasks.editor.PropertyNameLabelEditor;

public class DialectListMaintenanceBeanInfo extends SimpleBeanInfo {

    /**
     * Bean info for DialectListMaintenanceBeanInfo class.
     * 
     * @author akf
     * 
     */
    public DialectListMaintenanceBeanInfo() {
        super();
    }

    public PropertyDescriptor[] getPropertyDescriptors() {
        try {
            PropertyDescriptor dialectListFileNameProp = new PropertyDescriptor("dialectListFileNameProp",
                DialectListMaintenance.class);
            dialectListFileNameProp.setBound(true);
            dialectListFileNameProp.setDisplayName("<html><font color='green'>Lisy File:");
            dialectListFileNameProp.setShortDescription("list file");
            dialectListFileNameProp.setPropertyEditorClass(PropertyNameLabelEditor.class);

            PropertyDescriptor rv[] = { dialectListFileNameProp };
            return rv;
        } catch (IntrospectionException e) {
            throw new Error(e.toString());
        }
    }

    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(DialectListMaintenance.class);
        bd.setDisplayName("<html><font color='green'><center>Update<br>dialect List");
        return bd;
    }

}
