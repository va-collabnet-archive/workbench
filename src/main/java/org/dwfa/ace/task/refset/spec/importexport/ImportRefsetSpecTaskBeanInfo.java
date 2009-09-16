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
        PropertyDescriptor outputFilePropName;
        PropertyDescriptor pathUuidPropName;
        try {
            inputFilePropName = new PropertyDescriptor("inputFilePropName", ImportRefsetSpecTask.class);
            inputFilePropName.setBound(true);
            inputFilePropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            inputFilePropName.setDisplayName("<html><font color='green'>Input file property:");
            inputFilePropName.setShortDescription("Name of the property containing the filename to export from. ");

            outputFilePropName = new PropertyDescriptor("outputFilePropName", ImportRefsetSpecTask.class);
            outputFilePropName.setBound(true);
            outputFilePropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            outputFilePropName.setDisplayName("<html><font color='green'>Report file property:");
            outputFilePropName.setShortDescription("Name of the property containing the report file. ");

            pathUuidPropName = new PropertyDescriptor("pathUuidPropName", ImportRefsetSpecTask.class);
            pathUuidPropName.setBound(true);
            pathUuidPropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            pathUuidPropName.setDisplayName("<html><font color='green'>Alternate path UUID property:");
            pathUuidPropName.setShortDescription("Name of the property containing the alternative path to import on. ");
            PropertyDescriptor rv[] = { inputFilePropName, outputFilePropName, pathUuidPropName };
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
        bd.setDisplayName("<html><font color='green'><center>Import refset spec");
        return bd;
    }

}
