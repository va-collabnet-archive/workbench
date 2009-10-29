package org.dwfa.ace.task.search;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;

import org.dwfa.ace.prop.editor.ConceptLabelPropEditor;

public class RefsetMatchSearchInfo extends AbstractSeachTestSearchInfo {

	@Override
	protected PropertyDescriptor[] getAdditionalPropertyDescriptors() {
		try {
            PropertyDescriptor refsetTerm =
                new PropertyDescriptor("refset", getBeanDescriptor().getBeanClass());
            refsetTerm.setBound(true);
            refsetTerm.setPropertyEditorClass(ConceptLabelPropEditor.class);
            refsetTerm.setDisplayName("<html><font color='green'>refset:");
            refsetTerm.setShortDescription("The refset to test the component is in.");

            PropertyDescriptor rv[] =
                { refsetTerm };
            return rv;
        } catch (IntrospectionException e) {
             throw new Error(e.toString());
        }
     }        
    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(RefsetMatch.class);
        bd.setDisplayName("refset member");
        return bd;
    }
}
