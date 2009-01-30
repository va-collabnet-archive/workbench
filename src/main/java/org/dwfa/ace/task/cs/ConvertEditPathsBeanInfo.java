package org.dwfa.ace.task.cs;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.bpa.tasks.editor.PropertyNameLabelEditor;

public class ConvertEditPathsBeanInfo extends SimpleBeanInfo {
    

    public PropertyDescriptor[] getPropertyDescriptors() {
        try {  

            PropertyDescriptor inputFilePropName =
                new PropertyDescriptor("inputFilePropName", getBeanDescriptor().getBeanClass());
            inputFilePropName.setBound(true);
            inputFilePropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            inputFilePropName.setDisplayName("<html><font color='green'>input file prop:");
            inputFilePropName.setShortDescription("The property that contains the file name of the input file.");

            PropertyDescriptor conceptMapPropName =
                new PropertyDescriptor("conceptMapPropName", getBeanDescriptor().getBeanClass());
            conceptMapPropName.setBound(true);
            conceptMapPropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            conceptMapPropName.setDisplayName("<html><font color='green'>concept map prop:");
            conceptMapPropName.setShortDescription("The property that contains the resulting concept map.");

            PropertyDescriptor outputFilePropName =
                new PropertyDescriptor("outputFilePropName", getBeanDescriptor().getBeanClass());
            outputFilePropName.setBound(true);
            outputFilePropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            outputFilePropName.setDisplayName("<html><font color='green'>output file prop:");
            outputFilePropName.setShortDescription("The property that contains the file name of the output file.");

           PropertyDescriptor rv[] =
                { inputFilePropName, conceptMapPropName, outputFilePropName };
           
            return rv;
        } catch (IntrospectionException e) {
             throw new Error(e.toString());
        }
     }        
    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(ConvertEditPaths.class);
        bd.setDisplayName("<html><font color='green'><center>Convert Edit Paths<br>Using Map");
        return bd;
    }
}
