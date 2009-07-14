package org.dwfa.ace.task.refset.spec.importexport;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.bpa.tasks.editor.PropertyNameLabelEditor;

public class ImportRefsetSpecTaskBeanInfo extends SimpleBeanInfo {

    /**
    *
    */
    public ImportRefsetSpecTaskBeanInfo() {
        super();
    }

    public PropertyDescriptor[] getPropertyDescriptors() {
        PropertyDescriptor inputFilePropName;
        try {
            inputFilePropName = new PropertyDescriptor("inputFilePropName",
                    ImportRefsetSpecTask.class);
            inputFilePropName.setBound(true);
            inputFilePropName
                    .setPropertyEditorClass(PropertyNameLabelEditor.class);
            inputFilePropName
                    .setDisplayName("<html><font color='green'>Input file property:");
            inputFilePropName
                    .setShortDescription("Name of the property containing the filename to export from. ");
            PropertyDescriptor rv[] = { inputFilePropName };
            return rv;
        } catch (IntrospectionException e) {
            throw new Error(e.toString());
        }
    }

    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(ImportRefsetSpecTask.class);
        bd
                .setDisplayName("<html><font color='green'><center>Import refset spec");
        return bd;
    }

}
