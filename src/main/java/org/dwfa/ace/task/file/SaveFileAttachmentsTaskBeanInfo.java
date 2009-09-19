package org.dwfa.ace.task.file;

import java.beans.BeanDescriptor;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

/**
 * Bean info for ChooseTxtFileTask class.
 * 
 * @author Christine Hill
 * 
 */
public class SaveFileAttachmentsTaskBeanInfo extends SimpleBeanInfo {

    /**
     *
     */
    public SaveFileAttachmentsTaskBeanInfo() {
        super();
    }

    public PropertyDescriptor[] getPropertyDescriptors() {

        PropertyDescriptor rv[] = {};
        return rv;
    }

    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(SaveFileAttachmentsTask.class);
        bd.setDisplayName("<html><font color='green'><center>Prompt to save<br>file attachments");
        return bd;
    }

}
