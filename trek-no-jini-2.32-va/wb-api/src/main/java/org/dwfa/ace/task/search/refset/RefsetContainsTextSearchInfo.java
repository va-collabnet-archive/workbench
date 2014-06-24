package org.dwfa.ace.task.search.refset;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;

import org.dwfa.ace.task.search.AbstractSeachTestSearchInfo;
import org.dwfa.bpa.tasks.editor.JTextFieldEditorOneLine;

public class RefsetContainsTextSearchInfo extends AbstractSeachTestSearchInfo {

    @Override
    protected PropertyDescriptor[] getAdditionalPropertyDescriptors() {
        try {

            PropertyDescriptor relRestrictionTerm = new PropertyDescriptor("text",
                getBeanDescriptor().getBeanClass());
            relRestrictionTerm.setBound(true);
            relRestrictionTerm.setPropertyEditorClass(JTextFieldEditorOneLine.class);
            relRestrictionTerm.setDisplayName("<html><font color='green'>text:");
            relRestrictionTerm.setShortDescription("The text to test for inclusion in refset member.");

            PropertyDescriptor[] rv = { relRestrictionTerm };
            return rv;
        } catch (IntrospectionException e) {
            throw new Error(e.toString());
        }
    }

    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(RefsetContainsText.class);
        bd.setDisplayName("text in member");
        return bd;
    }
}
