package org.dwfa.ace.task.gui.component;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.bpa.tasks.editor.IncrementEditor;

public class DeselectAllComponentTogglesBeanInfo extends SimpleBeanInfo {

    public PropertyDescriptor[] getPropertyDescriptors() {

        try {
            PropertyDescriptor index =
                new PropertyDescriptor("index", getBeanDescriptor().getBeanClass());
            index.setBound(true);
            index.setPropertyEditorClass(IncrementEditor.class);
            index.setDisplayName("<html><font color='green'>Concept tab:");
            index.setShortDescription("Index of tab deselect all toggles. ");
            PropertyDescriptor rv[] = { index };
            return rv;
        } catch (IntrospectionException e) {
            throw new Error(e.toString());
       }
    }
    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(DeselectAllComponentToggles.class);
        bd.setDisplayName("<html><font color='green'><center>Deselect all toggles<br>"
                + "for concept tab");
        return bd;
    }

}
