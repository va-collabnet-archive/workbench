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
package org.dwfa.ace.task;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;
import java.util.ArrayList;
import java.util.List;
import org.dwfa.bpa.tasks.editor.PropertyNameLabelEditor;

/**
 * BeanInfo class for {@code WriteListToFile} Task
 * @author Matthew Edwards
 */
public class WriteListToFileBeanInfo extends SimpleBeanInfo {

    private static final String DESCRIPTOR_DISPLAY_NAME =
            "<html><font color='green'><center>Write a List of Objects<br> to a File";
    private final BeanDescriptor beanDescriptor;

    public WriteListToFileBeanInfo() {
        beanDescriptor = new BeanDescriptor(ReadUuidListListFromUrl.class);
        beanDescriptor.setDisplayName(DESCRIPTOR_DISPLAY_NAME);
    }

    @Override
    public BeanDescriptor getBeanDescriptor() {
        return beanDescriptor;
    }

    @Override
    public PropertyDescriptor[] getPropertyDescriptors() {
        try {

            List<PropertyDescriptor> propertyDescriptors = new ArrayList<PropertyDescriptor>();

            PropertyDescriptor outputFilePropertyName =
                    new PropertyDescriptor("outputFile",
                    WriteListToFile.class);
            outputFilePropertyName.setBound(true);
            outputFilePropertyName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            outputFilePropertyName.setDisplayName("<html><font color='green'>Output file:");
            outputFilePropertyName.setShortDescription("The file to output the list to.");
            propertyDescriptors.add(outputFilePropertyName);

            PropertyDescriptor listPropertyName =
                    new PropertyDescriptor("objectListPropertyName",
                    WriteListToFile.class);
            listPropertyName.setBound(true);
            listPropertyName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            listPropertyName.setDisplayName("<html><font color='green'>List Property Name:");
            listPropertyName.setShortDescription("The name of the Object List property.");
            propertyDescriptors.add(listPropertyName);

            PropertyDescriptor messageKey =
                    new PropertyDescriptor("messageKey",
                    WriteListToFile.class);
            messageKey.setBound(true);
            messageKey.setPropertyEditorClass(PropertyNameLabelEditor.class);
            messageKey.setDisplayName("<html><font color='green'>Message Property Name:");
            messageKey.setShortDescription("The name of the Message property.");
            propertyDescriptors.add(messageKey);

            PropertyDescriptor fileWrittenOutputMessage =
                    new PropertyDescriptor("fileWrittenOutputMessage",
                    WriteListToFile.class);
            fileWrittenOutputMessage.setBound(true);
            fileWrittenOutputMessage.setPropertyEditorClass(PropertyNameLabelEditor.class);
            fileWrittenOutputMessage.setDisplayName("<html><font color='green'>File Written Message:");
            fileWrittenOutputMessage.setShortDescription("The Text to display if the file was written.");
            propertyDescriptors.add(fileWrittenOutputMessage);

            PropertyDescriptor fileNotWrittenOutputMessage =
                    new PropertyDescriptor("fileNotWrittenOutputMessage",
                    WriteListToFile.class);
            fileNotWrittenOutputMessage.setBound(true);
            fileNotWrittenOutputMessage.setPropertyEditorClass(PropertyNameLabelEditor.class);
            fileNotWrittenOutputMessage.setDisplayName("<html><font color='green'>File Not Written Message:");
            fileNotWrittenOutputMessage.setShortDescription("The Text to display if the file was Not written.");
            propertyDescriptors.add(fileNotWrittenOutputMessage);

            return propertyDescriptors.toArray(new PropertyDescriptor[propertyDescriptors.size()]);
        } catch (IntrospectionException e) {
            throw new Error(e.toString());
        }
    }
}
