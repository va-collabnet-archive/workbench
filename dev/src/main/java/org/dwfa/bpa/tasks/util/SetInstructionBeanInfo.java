/*
 * Created on Apr 3, 2005
 *
 * Copyright 2005 by Informatics, Inc. 
 */
package org.dwfa.bpa.tasks.util;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.bpa.tasks.editor.JTextFieldEditor;

/**
 * @author kec
 *
 */
public class SetInstructionBeanInfo extends SimpleBeanInfo {

    /**
     * 
     */
    public SetInstructionBeanInfo() {
        super();
     }

    public PropertyDescriptor[] getPropertyDescriptors() {
        try {  
            PropertyDescriptor instruction =
                new PropertyDescriptor("instruction", SetInstruction.class);
            instruction.setBound(true);
            instruction.setPropertyEditorClass(JTextFieldEditor.class);
            instruction.setDisplayName("User Instructions");
            instruction.setShortDescription("A html string that contains instructions to present to the user.");


            PropertyDescriptor rv[] =
                {instruction};
            return rv;
        } catch (IntrospectionException e) {
             throw new Error(e.toString());
        }
     }        
    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(SetInstruction.class);
        bd.setDisplayName("<html><font color='green'><center>Instruct User");
        return bd;
    }

}
