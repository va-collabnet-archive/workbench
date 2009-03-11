/*
 * Created on May 22, 2005
 *
 * Copyright 2005 by Informatics, Inc. 
 */
package org.dwfa.bpa.tasks.ws;

import java.beans.BeanDescriptor;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.bpa.tasks.editor.CheckboxEditor;


/**
 * @author kec
 *
 */
public class SetWorkspaceVisibleBeanInfo extends SimpleBeanInfo {
    public PropertyDescriptor[] getPropertyDescriptors() {
        try {  
            PropertyDescriptor visible =
                new PropertyDescriptor("visible", SetWorkspaceVisible.class);
            visible.setBound(true);
            visible.setPropertyEditorClass(CheckboxEditor.class);
            visible.setDisplayName("visible");
            visible.setShortDescription("Shows or hides this component depending on the selected value.");



            PropertyDescriptor rv[] =
                {visible};
            return rv;
        } catch (Exception e) {
             throw new Error(e.toString());
        } 
     }        
    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(SetWorkspaceVisible.class);
        bd.setDisplayName("<html><font color='green'><center>Set Workspace Visible");
        return bd;
    }

}
