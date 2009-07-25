package org.dwfa.ace.refset;

import java.io.IOException;
import java.util.Collections;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartConceptConcept;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartConceptConceptConcept;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartConceptConceptString;
import org.dwfa.ace.api.ebr.I_ThinExtByRefTuple;
import org.dwfa.ace.api.ebr.I_ThinExtByRefVersioned;
import org.dwfa.vodb.bind.ThinExtBinder;
import org.dwfa.vodb.types.ConceptBean;

public class RefsetSpecTreeNode extends DefaultMutableTreeNode implements Comparable<RefsetSpecTreeNode> {

	I_ConfigAceFrame aceConfig;
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public int compareTo(RefsetSpecTreeNode o) {
		try {
		if (this.userObject instanceof  I_ThinExtByRefVersioned && o.userObject instanceof I_ThinExtByRefVersioned) {
				I_ThinExtByRefTuple thisExt = ((I_ThinExtByRefVersioned) this.userObject).getTuples(aceConfig.getAllowedStatus(), 
						aceConfig.getViewPositionSet(), true).iterator().next();
				I_ThinExtByRefTuple otherExt = ((I_ThinExtByRefVersioned) o.userObject).getTuples(aceConfig.getAllowedStatus(), 
						aceConfig.getViewPositionSet(), true).iterator().next();
					
					switch (ThinExtBinder.getExtensionType(thisExt.getCore())) {
					case CONCEPT_CONCEPT:
						
						switch (ThinExtBinder.getExtensionType(otherExt.getCore())) {
						case CONCEPT_CONCEPT:
							int comparison = compareTruth(thisExt, otherExt);
							if (comparison != 0) {
								return comparison;
							}
							return compareClause(thisExt, otherExt);
						case CONCEPT_CONCEPT_CONCEPT:
							return 1;
						case CONCEPT_CONCEPT_STRING:
							return 1;
						default:
							break;
						}
						
						break;

					case CONCEPT_CONCEPT_CONCEPT:
						switch (ThinExtBinder.getExtensionType(otherExt.getCore())) {
						case CONCEPT_CONCEPT:
							return -1;
						case CONCEPT_CONCEPT_CONCEPT:
							int comparison = compareTruth(thisExt, otherExt);
							if (comparison != 0) {
								return comparison;
							}
							comparison = compareClause(thisExt, otherExt);
							if (comparison != 0) {
								return comparison;
							}
							return compareConstraint(thisExt, otherExt);
						case CONCEPT_CONCEPT_STRING:
							return -1;
						default:
							break;
						}
						
						break;
					case CONCEPT_CONCEPT_STRING:
						switch (ThinExtBinder.getExtensionType(otherExt.getCore())) {
						case CONCEPT_CONCEPT:
							return -1;
						case CONCEPT_CONCEPT_CONCEPT:
							return 1;
						case CONCEPT_CONCEPT_STRING:
							int comparison = compareTruth(thisExt, otherExt);
							if (comparison != 0) {
								return comparison;
							}
							comparison = compareClause(thisExt, otherExt);
							if (comparison != 0) {
								return comparison;
							}
							return compareString(thisExt, otherExt);
						default:
							break;
						}
						
						break;
					default:
						break;
					}
				
			
		}
		return this.userObject.toString().compareTo(o.userObject.toString());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	private int compareString(I_ThinExtByRefTuple thisExt,
			I_ThinExtByRefTuple otherExt) throws IOException {
		String thisExtStr = ((I_ThinExtByRefPartConceptConceptString)thisExt.getPart()).getStr();
		String otherExtStr = ((I_ThinExtByRefPartConceptConceptString)otherExt.getPart()).getStr();
		return thisExtStr.toLowerCase().compareTo(otherExtStr.toLowerCase());
	}


	private int compareConstraint(I_ThinExtByRefTuple thisExt,
			I_ThinExtByRefTuple otherExt) throws IOException {
		ConceptBean thisClause = ConceptBean.get(((I_ThinExtByRefPartConceptConceptConcept)thisExt.getPart()).getC3id());
		ConceptBean otherClause = ConceptBean.get(((I_ThinExtByRefPartConceptConceptConcept)otherExt.getPart()).getC3id());
		I_DescriptionTuple thisClauseDesc = thisClause.getDescTuple(aceConfig.getTreeDescPreferenceList(), aceConfig);
		I_DescriptionTuple otherClauseDesc = otherClause.getDescTuple(aceConfig.getTreeDescPreferenceList(), aceConfig);
		return thisClauseDesc.getText().toLowerCase().compareTo(otherClauseDesc.getText().toLowerCase());
	}


	private int compareClause(I_ThinExtByRefTuple thisExt,
			I_ThinExtByRefTuple otherExt) throws IOException {
		ConceptBean thisClause = ConceptBean.get(((I_ThinExtByRefPartConceptConcept)thisExt.getPart()).getC2id());
		ConceptBean otherClause = ConceptBean.get(((I_ThinExtByRefPartConceptConcept)otherExt.getPart()).getC2id());
		I_DescriptionTuple thisClauseDesc = thisClause.getDescTuple(aceConfig.getTreeDescPreferenceList(), aceConfig);
		I_DescriptionTuple otherClauseDesc = otherClause.getDescTuple(aceConfig.getTreeDescPreferenceList(), aceConfig);
		return thisClauseDesc.getText().toLowerCase().compareTo(otherClauseDesc.getText().toLowerCase());
	}


	private int compareTruth(I_ThinExtByRefTuple thisExt,
			I_ThinExtByRefTuple otherExt) throws IOException {
		ConceptBean thisTruth = ConceptBean.get(((I_ThinExtByRefPartConceptConcept)thisExt.getPart()).getC1id());
		ConceptBean otherTruth = ConceptBean.get(((I_ThinExtByRefPartConceptConcept)otherExt.getPart()).getC1id());
		if (thisTruth != otherTruth) {
			I_DescriptionTuple thisTruthDesc = thisTruth.getDescTuple(aceConfig.getTreeDescPreferenceList(), aceConfig);
			I_DescriptionTuple otherTruthDesc = otherTruth.getDescTuple(aceConfig.getTreeDescPreferenceList(), aceConfig);
			if (thisTruthDesc.getText().toLowerCase().contains("true") && otherTruthDesc.getText().toLowerCase().contains("true") == false) {
				return -1;
			} else if (thisTruthDesc.getText().toLowerCase().contains("false") && otherTruthDesc.getText().toLowerCase().contains("false") == false) {
				return 1;
			} 
			return thisTruthDesc.getText().toLowerCase().compareTo(otherTruthDesc.getText().toLowerCase());	
		}
		return 0;
	}

		
	public RefsetSpecTreeNode(I_ConfigAceFrame aceConfig) {
		super();
		this.aceConfig = aceConfig;
	}

	public RefsetSpecTreeNode(Object userObject, boolean allowsChildren, I_ConfigAceFrame aceConfig) {
		super(userObject, allowsChildren);
		this.aceConfig = aceConfig;
	}

	public RefsetSpecTreeNode(Object userObject, I_ConfigAceFrame aceConfig) {
		super(userObject);
		this.aceConfig = aceConfig;
	}

	@Override
	public void add(MutableTreeNode newChild) {
		super.add(newChild);
		Collections.sort(children);
	}

}
