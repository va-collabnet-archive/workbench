package org.dwfa.ace.task.search;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;

import org.dwfa.ace.prop.editor.ConceptLabelPropEditor;

public class IsKindOfSearchInfo extends AbstractSeachTestSearchInfo {

	@Override
	protected PropertyDescriptor[] getAdditionalPropertyDescriptors() {
		try {
            PropertyDescriptor parentTerm =
                new PropertyDescriptor("parentTerm", getBeanDescriptor().getBeanClass());
            parentTerm.setBound(true);
            parentTerm.setPropertyEditorClass(ConceptLabelPropEditor.class);
            parentTerm.setDisplayName("<html><font color='green'>parent:");
            parentTerm.setShortDescription("The concept to test for parentage of.");

            PropertyDescriptor rv[] =
                { parentTerm };
            return rv;
        } catch (IntrospectionException e) {
             throw new Error(e.toString());
        }
     }        
    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(IsKindOf.class);
        bd.setDisplayName("is kind of");
        return bd;
    }
}
