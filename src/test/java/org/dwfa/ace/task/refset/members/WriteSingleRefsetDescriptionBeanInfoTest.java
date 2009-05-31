package org.dwfa.ace.task.refset.members;

import org.dwfa.bpa.tasks.editor.PropertyNameLabelEditor;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

import java.beans.BeanDescriptor;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

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
        assertThat(propertyDescriptor.getPropertyEditorClass(),
                equalTo((Class) PropertyNameLabelEditor.class));

        propertyDescriptor = descriptors[1];
        assertThat(propertyDescriptor.getName(), equalTo("selectedRefsetKey"));
        assertThat(propertyDescriptor.isBound(), equalTo(true));
        assertThat(propertyDescriptor.getDisplayName(), equalTo("<html><font color='green'>Selected Refset key:"));
        assertThat(propertyDescriptor.getShortDescription(), equalTo("Selected Refset key"));
        assertThat(propertyDescriptor.getPropertyEditorClass(),
                equalTo((Class) PropertyNameLabelEditor.class));
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
