package org.dwfa.ace.task;

import org.dwfa.bpa.tasks.editor.PropertyNameLabelEditor;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

public final class ChooseDirectoryBeanInfo extends SimpleBeanInfo {

    @Override
    public PropertyDescriptor[] getPropertyDescriptors() {
        try {

            PropertyDescriptor directoryKey =
                    new PropertyDescriptor("directoryKey", getBeanDescriptor().getBeanClass());
            directoryKey.setBound(true);
            directoryKey.setPropertyEditorClass(PropertyNameLabelEditor.class);
            directoryKey.setDisplayName("<html><font color='green'>Name of directory key:");
            directoryKey.setShortDescription("Name of directory key.");

            PropertyDescriptor rv[] = {directoryKey};
            return rv;
        } catch (IntrospectionException e) {
            throw new Error(e.toString());
        }
    }

    @Override
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(ChooseDirectory.class);
        bd.setDisplayName("<html><font color='green'><center>Choose Directory");
        return bd;
    }
}
