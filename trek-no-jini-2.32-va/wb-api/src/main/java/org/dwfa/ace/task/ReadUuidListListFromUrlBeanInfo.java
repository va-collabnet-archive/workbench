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
 * Bean info for ReadUuidListListFromUrl class.
 * 
 * @author Susan Castillo
 * 
 */
public class ReadUuidListListFromUrlBeanInfo extends SimpleBeanInfo {

    /**
     *
     */
    public ReadUuidListListFromUrlBeanInfo() {
        super();
    }

    public PropertyDescriptor[] getPropertyDescriptors() {
        try {
            PropertyDescriptor uuidListListPropName =
            // new
            // PropertyDescriptor("luceneDups/dupPotMatchResults/dwfaDups.txt",
            // ReadUuidListListFromUrl.class);
            new PropertyDescriptor("uuidListListPropName", ReadUuidListListFromUrl.class);
            uuidListListPropName.setBound(true);
            uuidListListPropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            uuidListListPropName.setDisplayName("<html><font color='green'>Uuid List:");
            uuidListListPropName.setShortDescription("Uuid list.");

            PropertyDescriptor uuidFileNamePropName = new PropertyDescriptor("uuidFileNamePropName",
                ReadUuidListListFromUrl.class);
            uuidFileNamePropName.setBound(true);
            uuidFileNamePropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            uuidFileNamePropName.setDisplayName("<html><font color='green'>UUID File Name:");
            uuidFileNamePropName.setShortDescription("File Name");

            PropertyDescriptor rv[] = { uuidListListPropName, uuidFileNamePropName };
            return rv;
        } catch (IntrospectionException e) {
            throw new Error(e.toString());
        }
    }

    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(ReadUuidListListFromUrl.class);
        bd.setDisplayName("<html><font color='green'><center>Read UUID List <br> List From File");
        return bd;
    }

}
