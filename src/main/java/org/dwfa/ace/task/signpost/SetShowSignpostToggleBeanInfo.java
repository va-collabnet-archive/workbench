package org.dwfa.ace.task.signpost;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.bpa.tasks.editor.CheckboxEditor;

public class SetShowSignpostToggleBeanInfo extends SimpleBeanInfo {

    public PropertyDescriptor[] getPropertyDescriptors() {
        try {
            PropertyDescriptor show =
                new PropertyDescriptor("show", getBeanDescriptor().getBeanClass());
            show.setBound(true);
            show.setPropertyEditorClass(CheckboxEditor.class);
            show.setDisplayName("<html><font color='green'>Show signpost toggle");
            show.setShortDescription("Choose whether to show the signpost toggle.");

            PropertyDescriptor rv[] = { show };
            return rv;
        } catch (IntrospectionException e) {
             throw new Error(e.toString());
        }
    }
    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(SetShowSignpostToggle.class);
        bd.setDisplayName("<html><font color='green'><center>Show or Hide <br>"
                + "Signpost Toggle");
        return bd;
    }

}
