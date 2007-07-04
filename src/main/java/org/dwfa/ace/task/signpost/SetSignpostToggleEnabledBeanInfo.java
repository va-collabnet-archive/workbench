package org.dwfa.ace.task.signpost;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.bpa.tasks.editor.CheckboxEditor;

public class SetSignpostToggleEnabledBeanInfo extends SimpleBeanInfo {

    public PropertyDescriptor[] getPropertyDescriptors() {
        try {
            PropertyDescriptor enabled =
                new PropertyDescriptor("enabled", getBeanDescriptor().getBeanClass());
            enabled.setBound(true);
            enabled.setPropertyEditorClass(CheckboxEditor.class);
            enabled.setDisplayName("<html><font color='green'>Enable signpost toggle");
            enabled.setShortDescription("Choose whether to enable the signpost toggle.");

            PropertyDescriptor rv[] = { enabled };
            return rv;
        } catch (IntrospectionException e) {
             throw new Error(e.toString());
        }
    }
    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(SetSignpostToggleEnabled.class);
        bd.setDisplayName("<html><font color='green'><center>Set Signpost <br>"
                + "toggle enabled");
        return bd;
    }

}
