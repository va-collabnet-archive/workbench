package org.dwfa.bpa.tasks.file;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.bpa.tasks.editor.JTextFieldEditor;
import org.dwfa.bpa.tasks.editor.PropertyNameLabelEditor;

public class SelectInputFileBeanInfo extends SimpleBeanInfo {

    /**
     * 
     */
    public PropertyDescriptor[] getPropertyDescriptors() {
        try {  
            PropertyDescriptor prompt =
                new PropertyDescriptor("prompt", getBeanDescriptor().getBeanClass());
            prompt.setBound(true);
            prompt.setPropertyEditorClass(JTextFieldEditor.class);
            prompt.setDisplayName("prompt");
            prompt.setShortDescription("A message to present to the user in a dialog.");

            PropertyDescriptor extension =
                new PropertyDescriptor("extension", getBeanDescriptor().getBeanClass());
            extension.setBound(true);
            extension.setPropertyEditorClass(JTextFieldEditor.class);
            extension.setDisplayName("extension");
            extension.setShortDescription("The file extension of the output file.");

            PropertyDescriptor inputFilePropName =
                new PropertyDescriptor("inputFilePropName", getBeanDescriptor().getBeanClass());
            inputFilePropName.setBound(true);
            inputFilePropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            inputFilePropName.setDisplayName("<html><font color='blue'>inputFilePropName:");
            inputFilePropName.setShortDescription("Name of the property to write the input file name to... ");

            PropertyDescriptor rv[] =
                {prompt, extension, inputFilePropName};
            return rv;
        } catch (IntrospectionException e) {
             throw new Error(e.toString());
        }
     }        
    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(SelectInputFile.class);
        bd.setDisplayName("<html><font color='green'><center>Specify<br>Input File");
        return bd;
    }

}
