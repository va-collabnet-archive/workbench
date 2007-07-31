package org.dwfa.ace.task.gui.component;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.bpa.tasks.editor.CheckboxEditor;
import org.dwfa.bpa.tasks.editor.IncrementEditor;

public class SetComponentToggleBeanInfo extends SimpleBeanInfo {

    public PropertyDescriptor[] getPropertyDescriptors() {

        try {
            PropertyDescriptor selectToggle =
                new PropertyDescriptor("selectToggle", getBeanDescriptor().getBeanClass());
            selectToggle.setBound(true);
            selectToggle.setPropertyEditorClass(CheckboxEditor.class);
            selectToggle.setDisplayName("<html><font color='green'>Selected:");
            selectToggle.setShortDescription("State to set toggle to. ");
            
            PropertyDescriptor toggle =
                new PropertyDescriptor("toggle", getBeanDescriptor().getBeanClass());
            toggle.setBound(true);
            toggle.setPropertyEditorClass(ComponentToggleEditor.class);
            toggle.setDisplayName("<html><font color='green'>Toggle:");
            toggle.setShortDescription("Toggle to set status of. ");
            
            PropertyDescriptor index =
                new PropertyDescriptor("index", getBeanDescriptor().getBeanClass());
            index.setBound(true);
            index.setPropertyEditorClass(IncrementEditor.class);
            index.setDisplayName("<html><font color='green'>Concept tab:");
            index.setShortDescription("Index of tab to modify. ");
            
            
            
            PropertyDescriptor rv[] = { selectToggle, toggle, index };
            return rv;
        } catch (IntrospectionException e) {
            throw new Error(e.toString());
       }
    }
    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(SetComponentToggle.class);
        bd.setDisplayName("<html><font color='green'><center>Set Component<br>"
                + "Toggle");
        return bd;
    }

}

