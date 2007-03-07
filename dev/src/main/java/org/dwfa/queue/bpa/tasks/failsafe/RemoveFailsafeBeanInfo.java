/*
 * Created on Jun 9, 2005
 *
 * Copyright 2005 by Informatics, Inc. 
 */
package org.dwfa.queue.bpa.tasks.failsafe;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.bpa.tasks.editor.DataIdEditorOneLine;
import org.dwfa.bpa.tasks.editor.RelativeTimeEditor;

public class RemoveFailsafeBeanInfo extends SimpleBeanInfo {

    public RemoveFailsafeBeanInfo() {
        super();
    }
    public PropertyDescriptor[] getPropertyDescriptors() {
        try {  
            PropertyDescriptor relativeTimeProp =
                new PropertyDescriptor("relativeTimeInMins", RemoveFailsafe.class);
            relativeTimeProp.setBound(true);
            relativeTimeProp.setPropertyEditorClass(RelativeTimeEditor.class);
            relativeTimeProp.setDisplayName("Removal deadline");
            relativeTimeProp.setShortDescription("The deadline by which the failsafe should have removed the failsafe (relative time).");

            PropertyDescriptor failsafeDataId =
                new PropertyDescriptor("failsafeDataId", RemoveFailsafe.class);
            failsafeDataId.setBound(true);
            failsafeDataId.setPropertyEditorClass(DataIdEditorOneLine.class);
            failsafeDataId.setDisplayName("UUID Data Id:");
            failsafeDataId.setShortDescription("The String representation of the failsafe UUID.");


            PropertyDescriptor rv[] =
                {relativeTimeProp, failsafeDataId};
            return rv;
        } catch (IntrospectionException e) {
             throw new Error(e.toString());
        }
     }        
    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(RemoveFailsafe.class);
        bd.setDisplayName("<html><font color='red'><center>Remove Failsafe");
        return bd;
    }

}
