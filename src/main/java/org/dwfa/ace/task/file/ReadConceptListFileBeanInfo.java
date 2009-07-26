package org.dwfa.ace.task.file;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.bpa.tasks.editor.PropertyNameLabelEditor;

public class ReadConceptListFileBeanInfo extends SimpleBeanInfo {

    /**
     * 
     */
    public ReadConceptListFileBeanInfo() {
        super();
     }
    
    public PropertyDescriptor[] getPropertyDescriptors() {
        try {  
            PropertyDescriptor inputFilePropName =
                new PropertyDescriptor("inputFilePropName", ReadConceptListFile.class);
            inputFilePropName.setBound(true);
            inputFilePropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            inputFilePropName.setDisplayName("<html><font color='green'>Input file property:");
            inputFilePropName.setShortDescription("Name of the property containing the filename to import. ");
            
            PropertyDescriptor conceptListPropName =
                new PropertyDescriptor("conceptListPropName", ReadConceptListFile.class);
            conceptListPropName.setBound(true);
            conceptListPropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            conceptListPropName.setDisplayName("<html><font color='green'>Concept list property:");
            conceptListPropName.setShortDescription("Name of the property set by this task to the concept list in the specified file. ");
            
            PropertyDescriptor rv[] = { inputFilePropName, conceptListPropName };
            return rv;
        } catch (IntrospectionException e) {
             throw new Error(e.toString());
        }
    }        
    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(ReadConceptListFile.class);
        bd.setDisplayName("<html><font color='green'><center>Read concept list file");
        return bd;
    }

}