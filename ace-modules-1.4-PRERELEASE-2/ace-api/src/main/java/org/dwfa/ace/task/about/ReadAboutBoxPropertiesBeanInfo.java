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
package org.dwfa.ace.task.about;

import java.beans.SimpleBeanInfo;
import java.beans.PropertyDescriptor;
import java.beans.IntrospectionException;
import java.beans.BeanDescriptor;

import org.dwfa.bpa.tasks.editor.PropertyNameLabelEditor;

/**
 * @author Luke
 *         Date: Nov 3, 2009
 *         Time: 9:28:29 AM
 */
public class ReadAboutBoxPropertiesBeanInfo extends SimpleBeanInfo {

    /**
     *
     */
    public ReadAboutBoxPropertiesBeanInfo() {
        super();
    }

    public PropertyDescriptor[] getPropertyDescriptors() {
        try {
            PropertyDescriptor aboutBoxPropertiesFile = new PropertyDescriptor("aboutBoxPropertiesFilePropName",
                getBeanDescriptor().getBeanClass());
            aboutBoxPropertiesFile.setBound(true);
            aboutBoxPropertiesFile.setPropertyEditorClass(PropertyNameLabelEditor.class);
            aboutBoxPropertiesFile.setDisplayName("<html><font color='green'>About Box Properties File:");
            aboutBoxPropertiesFile.setShortDescription("The location of the About Box properties file ");

            PropertyDescriptor aboutBoxHtml = new PropertyDescriptor("aboutBoxHtmlPropName",
                getBeanDescriptor().getBeanClass());
            aboutBoxHtml.setBound(true);
            aboutBoxHtml.setPropertyEditorClass(PropertyNameLabelEditor.class);
            aboutBoxHtml.setDisplayName("<html><font color='green'>About Box HTML:");
            aboutBoxHtml.setShortDescription("A HTML string version of the custom About Box. ");

            PropertyDescriptor aboutBoxTitle = new PropertyDescriptor("aboutBoxTitlePropName",
                getBeanDescriptor().getBeanClass());
            aboutBoxTitle.setBound(true);
            aboutBoxTitle.setPropertyEditorClass(PropertyNameLabelEditor.class);
            aboutBoxTitle.setDisplayName("<html><font color='green'>About Box Title:");
            aboutBoxTitle.setShortDescription("The title for the custom About Box. ");

            PropertyDescriptor rv[] = { aboutBoxPropertiesFile, aboutBoxHtml, aboutBoxTitle };
            return rv;
        } catch (IntrospectionException e) {
            throw new Error(e.toString());
        }
    }

    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(ReadAboutBoxProperties.class);
        bd.setDisplayName("<html><font color='green'><center>Read About Box<br>Properties");
        return bd;
    }
}
