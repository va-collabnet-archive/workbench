package org.dwfa.ace.task.refset.members;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.bpa.tasks.editor.PropertyNameLabelEditor;

public class ImportRefsetFromLanguageSubsetFileBeanInfo extends SimpleBeanInfo {

    public PropertyDescriptor[] getPropertyDescriptors() {
        try {

            PropertyDescriptor refsetConceptPropName =
                new PropertyDescriptor("refsetConceptPropName", getBeanDescriptor().getBeanClass());
            refsetConceptPropName.setBound(true);
            refsetConceptPropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            refsetConceptPropName.setDisplayName("<html><font color='green'>Refset concept property:");
            refsetConceptPropName.setShortDescription("The property containing the refset concept. ");

            PropertyDescriptor languageSpcificationFileName =
                new PropertyDescriptor("languageSpcificationFileName", getBeanDescriptor().getBeanClass());
            languageSpcificationFileName.setBound(true);
            languageSpcificationFileName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            languageSpcificationFileName.setDisplayName("<html><font color='green'>File name property:");
            languageSpcificationFileName.setShortDescription("The property containing the name of the Language Subset file to be imported. ");

            PropertyDescriptor rv[] = { refsetConceptPropName, languageSpcificationFileName };
            return rv;
        } catch (IntrospectionException e) {
            throw new Error(e.toString());
        }
    }

    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(ImportRefsetFromLanguageSubsetFile.class);
        bd.setDisplayName("<html><font color='green'><center>Create Refset from <br> Language Subset File");
        return bd;
    }

}
