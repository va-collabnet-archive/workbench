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
package org.dwfa.ace.task.refset.members;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.beans.BeanDescriptor;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.bpa.tasks.editor.PropertyNameLabelEditor;
import org.junit.Test;

public final class WriteSingleRefsetDescriptionBeanInfoTest {

    @Test
    public void shouldExtendsSimpleBeanInfo() {
        assertTrue("Should extend SimpleBeaInfo",
            SimpleBeanInfo.class.isAssignableFrom(WriteSingleRefsetDescriptionBeanInfo.class));
    }

    @Test
    public void shouldReturnEditableProperties() {
        PropertyDescriptor[] descriptors = new WriteSingleRefsetDescriptionBeanInfo().getPropertyDescriptors();
        assertThat(descriptors, notNullValue());
        assertThat(descriptors.length, equalTo(2));

        PropertyDescriptor propertyDescriptor = descriptors[0];
        assertThat(propertyDescriptor.getName(), equalTo("directoryKey"));
        assertThat(propertyDescriptor.isBound(), equalTo(true));
        assertThat(propertyDescriptor.getDisplayName(), equalTo("<html><font color='green'>Output directory key:"));
        assertThat(propertyDescriptor.getShortDescription(), equalTo("Output directory key"));
        assertThat(propertyDescriptor.getPropertyEditorClass(), equalTo((Class) PropertyNameLabelEditor.class));

        propertyDescriptor = descriptors[1];
        assertThat(propertyDescriptor.getName(), equalTo("selectedRefsetKey"));
        assertThat(propertyDescriptor.isBound(), equalTo(true));
        assertThat(propertyDescriptor.getDisplayName(), equalTo("<html><font color='green'>Selected Refset key:"));
        assertThat(propertyDescriptor.getShortDescription(), equalTo("Selected Refset key"));
        assertThat(propertyDescriptor.getPropertyEditorClass(), equalTo((Class) PropertyNameLabelEditor.class));
    }

    @Test
    public void shouldReturnABeanDescriptor() {
        BeanDescriptor beanDescriptor = new WriteSingleRefsetDescriptionBeanInfo().getBeanDescriptor();
        assertThat(beanDescriptor, notNullValue());
        assertThat(beanDescriptor.getDisplayName(),
            equalTo("<html><font color='green'><center>Export a single Refset<br>to Disk"));
        assertThat(beanDescriptor.getBeanClass(), equalTo((Class) WriteSingleRefsetDescription.class));
    }
}
