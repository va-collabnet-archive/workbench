package org.dwfa.ace.task.refset.spec;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.bpa.tasks.editor.PropertyNameLabelEditor;

public class ShowRefsetSpecTaskBeanInfo extends SimpleBeanInfo {

    /**
    *
    */
    public ShowRefsetSpecTaskBeanInfo() {
        super();
    }

    public PropertyDescriptor[] getPropertyDescriptors() {

        try {

            PropertyDescriptor refsetUuidPropName;
            refsetUuidPropName =
                    new PropertyDescriptor("refsetUuidPropName", getBeanDescriptor().getBeanClass());
            refsetUuidPropName.setBound(true);
            refsetUuidPropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            refsetUuidPropName.setDisplayName("<html><font color='green'>refset UUID prop:");
            refsetUuidPropName.setShortDescription("The property containing the UUID of the refset to display in the refset editor tab.");

            PropertyDescriptor rv[] =
                    { refsetUuidPropName };
            return rv;
        } catch (IntrospectionException e) {
            throw new Error(e.toString());
        }
    }

    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(ShowRefsetSpecTask.class);
        bd.setDisplayName("<html><font color='blue'><center>Show refset<br>spec");
        return bd;
    }

}
