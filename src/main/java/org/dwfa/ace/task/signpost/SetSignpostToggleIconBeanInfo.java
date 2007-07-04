package org.dwfa.ace.task.signpost;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.bpa.tasks.editor.JTextFieldEditor;

public class SetSignpostToggleIconBeanInfo extends SimpleBeanInfo {

    public PropertyDescriptor[] getPropertyDescriptors() {
        try {
            PropertyDescriptor iconResource =
                new PropertyDescriptor("iconResource", getBeanDescriptor().getBeanClass());
            iconResource.setBound(true);
            iconResource.setPropertyEditorClass(JTextFieldEditor.class);
            iconResource.setDisplayName("<html><font color='green'>Signpost toggle icon");
            iconResource.setShortDescription("Set the signpost toggle icon from classpath resource.");

            PropertyDescriptor rv[] = { iconResource };
            return rv;
        } catch (IntrospectionException e) {
             throw new Error(e.toString());
        }
    }
    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(SetSignpostToggleIcon.class);
        bd.setDisplayName("<html><font color='green'><center>Set Signpost <br>"
                + "toggle icon");
        return bd;
    }

}
