package org.dwfa.ace.task.refset.members;

import org.dwfa.bpa.tasks.editor.PropertyNameLabelEditor;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

public final class WriteSingleRefsetDescriptionBeanInfo extends SimpleBeanInfo {

    @Override
    public PropertyDescriptor[] getPropertyDescriptors() {
        try {
            PropertyDescriptor putputDirPropertyName = new PropertyDescriptor("directoryKey",
                    getBeanDescriptor().getBeanClass());
            putputDirPropertyName.setBound(true);
            putputDirPropertyName.setDisplayName("<html><font color='green'>Output directory key:");
            putputDirPropertyName.setShortDescription("Output directory key");
            putputDirPropertyName.setPropertyEditorClass(PropertyNameLabelEditor.class);

            PropertyDescriptor refsetPropertyName = new PropertyDescriptor("selectedRefsetKey",
                    getBeanDescriptor().getBeanClass());
            refsetPropertyName.setBound(true);
            refsetPropertyName.setDisplayName("<html><font color='green'>Selected Refset key:");
            refsetPropertyName.setShortDescription("Selected Refset key");
            refsetPropertyName.setPropertyEditorClass(PropertyNameLabelEditor.class);

            return new PropertyDescriptor[]{putputDirPropertyName, refsetPropertyName};
        } catch (IntrospectionException e) {
            throw new Error(e.toString());
        }
    }

    @Override
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(WriteSingleRefsetDescription.class);
        bd.setDisplayName("<html><font color='green'><center>Export a single Refsets<br>to Disk");
        return bd;
    }
}
