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
 * Bean info for CopyHtmlFileToProperty class.
 * 
 * @author Susan Castillo
 * 
 */
public class CopyHtmlFileToPropertyBeanInfo extends SimpleBeanInfo {

    public CopyHtmlFileToPropertyBeanInfo() {
        super();
    }

    public PropertyDescriptor[] getPropertyDescriptors() {
        try {
            PropertyDescriptor detailHtmlFileNameProp = new PropertyDescriptor("detailHtmlFileNameProp",
                CopyHtmlFileToProperty.class);
            detailHtmlFileNameProp.setBound(true);
            detailHtmlFileNameProp.setDisplayName("<html><font color='green'>Html File Name:");
            detailHtmlFileNameProp.setShortDescription("File Name");
            detailHtmlFileNameProp.setPropertyEditorClass(PropertyNameLabelEditor.class);

            PropertyDescriptor htmlDataPropName = new PropertyDescriptor("htmlDataPropName",
                CopyHtmlFileToProperty.class);
            htmlDataPropName.setBound(true);
            htmlDataPropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            htmlDataPropName.setDisplayName("<html><font color='green'>Html data key:");
            htmlDataPropName.setShortDescription("Html data key.");

            PropertyDescriptor rv[] = { detailHtmlFileNameProp, htmlDataPropName };
            return rv;
        } catch (IntrospectionException e) {
            throw new Error(e.toString());
        }
    }

    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(CopyHtmlFileToProperty.class);
        bd.setDisplayName("<html><font color='green'><center>Copy Html File<br>to Property");
        return bd;
    }
}
