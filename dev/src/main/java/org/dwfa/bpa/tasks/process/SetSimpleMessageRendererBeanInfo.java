package org.dwfa.bpa.tasks.process;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.bpa.tasks.editor.JTextFieldEditor;

public class SetSimpleMessageRendererBeanInfo extends SimpleBeanInfo {

    /**
     * 
     */
    public SetSimpleMessageRendererBeanInfo() {
        super();
    }
    public PropertyDescriptor[] getPropertyDescriptors() {
        try {  
            PropertyDescriptor source =
                new PropertyDescriptor("source", SetSimpleMessageRenderer.class);
            source.setBound(true);
            source.setPropertyEditorClass(JTextFieldEditor.class);
            source.setDisplayName("set message source");
            source.setShortDescription("Sets the message renderer of the process to a SimpleMessageRenderer with the provided source.");


            PropertyDescriptor rv[] =
                {source};
            return rv;
        } catch (IntrospectionException e) {
             throw new Error(e.toString());
        }
     }        
    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(SetSimpleMessageRenderer.class);
        bd.setDisplayName("<html><font color='green'><center>Set Simple<br>Message Renderer");
        return bd;
    }

}

