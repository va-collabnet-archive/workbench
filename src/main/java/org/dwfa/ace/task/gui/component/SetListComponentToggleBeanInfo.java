package org.dwfa.ace.task.gui.component;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.bpa.tasks.editor.CheckboxEditor;

public class SetListComponentToggleBeanInfo extends SimpleBeanInfo {

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
                        
            PropertyDescriptor rv[] = { selectToggle, toggle };
            return rv;
        } catch (IntrospectionException e) {
            throw new Error(e.toString());
       }
    }
    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(SetListComponentToggle.class);
        bd.setDisplayName("<html><font color='green'><center>Set List <br>Component Toggle");
        return bd;
    }

}

