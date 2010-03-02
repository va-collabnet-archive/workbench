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
 * Bean info for GetDetailHtmlDirUuidSetHtmlFileName class.
 * 
 * @author Susan Castillo
 * 
 */
public class GetDetailHtmlDirUuidSetHtmlFileNameBeanInfo extends SimpleBeanInfo {

    /**
     *
     */
    public GetDetailHtmlDirUuidSetHtmlFileNameBeanInfo() {
        super();
    }

    public PropertyDescriptor[] getPropertyDescriptors() {
        try {
            PropertyDescriptor detailHtmlFileNameProp = new PropertyDescriptor("detailHtmlFileNameProp",
                GetDetailHtmlDirUuidSetHtmlFileName.class);
            detailHtmlFileNameProp.setBound(true);
            detailHtmlFileNameProp.setPropertyEditorClass(PropertyNameLabelEditor.class);
            detailHtmlFileNameProp.setDisplayName("<html><font color='green'>File Name <br> Dup Details");
            detailHtmlFileNameProp.setShortDescription("Dup Html <br> Detail File Name");

            PropertyDescriptor uuidListPropName = new PropertyDescriptor("uuidListPropName",
                GetDetailHtmlDirUuidSetHtmlFileName.class);
            uuidListPropName.setBound(true);
            uuidListPropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            uuidListPropName.setDisplayName("<html><font color='green'>Uuid List of Concept");
            uuidListPropName.setShortDescription("Uuid of Concept");

            PropertyDescriptor htmlDirPropName = new PropertyDescriptor("htmlDirPropName",
                GetDetailHtmlDirUuidSetHtmlFileName.class);
            htmlDirPropName.setBound(true);
            htmlDirPropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            htmlDirPropName.setDisplayName("<html><font color='green'>Dir Html File");
            htmlDirPropName.setShortDescription("Detail Html File Name");

            PropertyDescriptor rv[] = { detailHtmlFileNameProp, uuidListPropName, htmlDirPropName };
            return rv;
        } catch (IntrospectionException e) {
            throw new Error(e.toString());
        }
    }

    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(GetDetailHtmlDirUuidSetHtmlFileName.class);
        bd.setDisplayName("<html><font color='green'><center>Get Detail <br>Html Dir and Uuid <br> Set Html Detail File Name");
        return bd;
    }

}
