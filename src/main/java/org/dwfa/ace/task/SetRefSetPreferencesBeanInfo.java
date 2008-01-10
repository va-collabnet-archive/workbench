package org.dwfa.ace.task;
//package org.dwfa.ace.task.signpost;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.ace.task.gui.component.ComponentToggleEditor;

public class SetRefSetPreferencesBeanInfo extends SimpleBeanInfo {

	public SetRefSetPreferencesBeanInfo() {
        super();
     }
	
    public PropertyDescriptor[] getPropertyDescriptors() {
        try {
            PropertyDescriptor toggle =
            	new PropertyDescriptor("toggle", SetRefSetPreferences.class);
            toggle.setBound(true);
            toggle.setPropertyEditorClass(ComponentToggleEditor.class);
            toggle.setDisplayName("<html><font color='green'>Toggle:");
            toggle.setShortDescription("Toggle to set refSet status of. ");
            
                        
            PropertyDescriptor rv[] = { toggle };
            return rv;
        } catch (IntrospectionException e) {
             throw new Error(e.toString());
        }
    }
    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(SetRefSetPreferences.class);
        bd.setDisplayName("<html><font color='green'><center>Set refset <br> preferences");
        return bd;
    }

}