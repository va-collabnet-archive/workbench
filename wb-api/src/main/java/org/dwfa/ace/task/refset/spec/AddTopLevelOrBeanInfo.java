package org.dwfa.ace.task.refset.spec;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.bpa.tasks.editor.PropertyNameLabelEditor;

public class AddTopLevelOrBeanInfo extends SimpleBeanInfo {

    public PropertyDescriptor[] getPropertyDescriptors() {
        try {

            PropertyDescriptor refsetPropName;
            refsetPropName =
                    new PropertyDescriptor("refsetPropName", getBeanDescriptor().getBeanClass());
            refsetPropName.setBound(true);
            refsetPropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            refsetPropName.setDisplayName("<html><font color='green'>refset uuid prop name:");
            refsetPropName.setShortDescription("The property to put the refset uuid into.");

            PropertyDescriptor rv[] = { refsetPropName };
            return rv;
        } catch (IntrospectionException e) {
            throw new Error(e.toString());
        }
    }


    @Override
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(AddTopLevelOr.class);
        bd.setDisplayName("<html><font color='green'><center>Add Top Level<br>OR Clause<br>to RefSet Spec");
        return bd;
    }
}