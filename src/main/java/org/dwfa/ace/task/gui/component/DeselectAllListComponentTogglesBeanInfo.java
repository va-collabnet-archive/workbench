package org.dwfa.ace.task.gui.component;

import java.beans.BeanDescriptor;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

public class DeselectAllListComponentTogglesBeanInfo extends SimpleBeanInfo {

    public PropertyDescriptor[] getPropertyDescriptors() {
        PropertyDescriptor rv[] = {  };
        return rv;
    }
    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(DeselectAllListComponentToggles.class);
        bd.setDisplayName("<html><font color='green'><center>Deselect all toggles<br>"
                + "for list component view");
        return bd;
    }

}
