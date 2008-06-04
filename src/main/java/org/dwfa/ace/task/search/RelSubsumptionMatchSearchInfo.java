package org.dwfa.ace.task.search;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;

import org.dwfa.bpa.tasks.editor.CheckboxEditor;
import org.dwfa.bpa.tasks.editor.ConceptLabelEditor;

public class RelSubsumptionMatchSearchInfo extends AbstractSeachTestSearchInfo {

	@Override
	protected PropertyDescriptor[] getAdditionalPropertyDescriptors() {
		try {
            PropertyDescriptor applySubsumption =
                new PropertyDescriptor("applySubsumption", getBeanDescriptor().getBeanClass());
            applySubsumption.setBound(true);
            applySubsumption.setPropertyEditorClass(CheckboxEditor.class);
            applySubsumption.setDisplayName("<html><font color='green'>use subsumption:");
            applySubsumption.setShortDescription("If checked, uses subsumption to test rel type and rel restriction.");

            PropertyDescriptor relTypeTerm = 
				new PropertyDescriptor("relTypeTerm", getBeanDescriptor().getBeanClass());
			relTypeTerm.setBound(true);
			relTypeTerm.setPropertyEditorClass(ConceptLabelEditor.class);
			relTypeTerm.setDisplayName("<html><font color='green'>type kind:");
			relTypeTerm.setShortDescription("The concept to test for type is kind of.");

			PropertyDescriptor relRestrictionTerm = 
				new PropertyDescriptor("relRestrictionTerm", getBeanDescriptor().getBeanClass());
			relRestrictionTerm.setBound(true);
			relRestrictionTerm.setPropertyEditorClass(ConceptLabelEditor.class);
			relRestrictionTerm.setDisplayName("<html><font color='green'>restriction kind:");
			relRestrictionTerm.setShortDescription("The concept to test for restriction is kind of.");

			PropertyDescriptor[] rv = { applySubsumption, relTypeTerm, relRestrictionTerm };
			return rv;
		} catch (IntrospectionException e) {
			throw new Error(e.toString());
		}
	}

	/**
	 * @see java.beans.BeanInfo#getBeanDescriptor()
	 */
	public BeanDescriptor getBeanDescriptor() {
		BeanDescriptor bd = new BeanDescriptor(RelSubsumptionMatch.class);
		bd.setDisplayName("rel kind");
		return bd;
	}
}
