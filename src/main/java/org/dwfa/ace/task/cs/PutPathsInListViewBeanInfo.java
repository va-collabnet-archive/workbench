package org.dwfa.ace.task.cs;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.bpa.tasks.editor.PropertyNameLabelEditor;

public class PutPathsInListViewBeanInfo extends SimpleBeanInfo {
    

    public PropertyDescriptor[] getPropertyDescriptors() {
        try {  

            PropertyDescriptor inputFilePropName =
                new PropertyDescriptor("inputFilePropName", getBeanDescriptor().getBeanClass());
            inputFilePropName.setBound(true);
            inputFilePropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            inputFilePropName.setDisplayName("<html><font color='green'>input file prop:");
            inputFilePropName.setShortDescription("The property that contains the file name of the input file.");

            PropertyDescriptor rv[] =
                { inputFilePropName };
            return rv;
        } catch (IntrospectionException e) {
             throw new Error(e.toString());
        }
     }        
    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(PutPathsInListView.class);
        bd.setDisplayName("<html><font color='green'><center>Put Edit Paths<br>from Change Set<br>in List View");
        return bd;
    }
}
