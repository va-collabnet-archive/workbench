package org.dwfa.bpa.tasks.web;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.bpa.tasks.editor.JTextFieldEditor;

public class FetchFromWebBeanInfo extends SimpleBeanInfo {

    /**
     * 
     */
    public FetchFromWebBeanInfo() {
        super();
    }
    public PropertyDescriptor[] getPropertyDescriptors() {
        try {  
            PropertyDescriptor webURLString =
                new PropertyDescriptor("webURLString", FetchFromWeb.class);
            webURLString.setBound(true);
            webURLString.setPropertyEditorClass(JTextFieldEditor.class);
            webURLString.setDisplayName("webURLString");
            webURLString.setShortDescription("A webURL to fetch strings from to present to the user.");


            PropertyDescriptor rv[] =
                {webURLString};
            return rv;
        } catch (IntrospectionException e) {
             throw new Error(e.toString());
        }
     }        
    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(FetchFromWeb.class);
        bd.setDisplayName("<html><font color='green'><center>Fetch string from web");
        return bd;
    }

}
