package org.dwfa.ace.task.refset.spec.importexport;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.bpa.tasks.editor.PropertyNameLabelEditor;

public class ExportRefsetSpecTaskBeanInfo extends SimpleBeanInfo {

    /**
    *
    */
    public ExportRefsetSpecTaskBeanInfo() {
        super();
    }

    public PropertyDescriptor[] getPropertyDescriptors() {
        PropertyDescriptor outputFilePropName;
        PropertyDescriptor reportFilePropName;
        try {
            outputFilePropName = new PropertyDescriptor("outputFilePropName", ExportRefsetSpecTask.class);
            outputFilePropName.setBound(true);
            outputFilePropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            outputFilePropName.setDisplayName("<html><font color='green'>Output file property:");
            outputFilePropName.setShortDescription("Name of the property containing the filename to export to. ");

            reportFilePropName = new PropertyDescriptor("reportFilePropName", ExportRefsetSpecTask.class);
            reportFilePropName.setBound(true);
            reportFilePropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            reportFilePropName.setDisplayName("<html><font color='green'>Output file property:");
            reportFilePropName.setShortDescription("Name of the property containing the report filename. ");
            PropertyDescriptor rv[] = { outputFilePropName, reportFilePropName };
            return rv;
        } catch (IntrospectionException e) {
            throw new Error(e.toString());
        }
    }

    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(ExportRefsetSpecTask.class);
        bd.setDisplayName("<html><font color='green'><center>Export refset spec");
        return bd;
    }

}
