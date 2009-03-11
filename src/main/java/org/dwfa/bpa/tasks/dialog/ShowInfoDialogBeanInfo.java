/*
 * Created on Jun 1, 2005
 *
 * Copyright 2005 by Informatics, Inc. 
 */
package org.dwfa.bpa.tasks.dialog;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.bpa.tasks.editor.JTextFieldEditor;


/**
 * @author kec
 *
 */
public class ShowInfoDialogBeanInfo extends SimpleBeanInfo {

    /**
     * 
     */
    public ShowInfoDialogBeanInfo() {
        super();
    }
    public PropertyDescriptor[] getPropertyDescriptors() {
        try {  
            PropertyDescriptor message =
                new PropertyDescriptor("message", ShowInfoDialog.class);
            message.setBound(true);
            message.setPropertyEditorClass(JTextFieldEditor.class);
            message.setDisplayName("message");
            message.setShortDescription("A message to present to the user in a dialog.");


            PropertyDescriptor rv[] =
                {message};
            return rv;
        } catch (IntrospectionException e) {
             throw new Error(e.toString());
        }
     }        
    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(ShowInfoDialog.class);
        bd.setDisplayName("<html><font color='green'><center>Show Dialog");
        return bd;
    }

}
