package org.ihtsdo.workunit;

import java.util.List;

import org.ihtsdo.tk.dto.concept.TkConcept;

public class WuFocusConcept {
	
	private TkConcept concept;
	private TkConcept topLevelAncestor;
	private List<TkConcept> directInferredParents;
	private List<TkConcept> directInferredChildren;
	private List<TkConcept> directInferredSiblings;
	private List<TkConcept> otherConcepts;
	private String additionalContext;
	
	public WuFocusConcept() {
		super();
	}

	/**
	 * @return the concept
	 */
	public TkConcept getConcept() {
		return concept;
	}

	/**
	 * @param concept the concept to set
	 */
	public void setConcept(TkConcept concept) {
		this.concept = concept;
	}

	/**
	 * @return the topLevelAncestor
	 */
	public TkConcept getTopLevelAncestor() {
		return topLevelAncestor;
	}

	/**
	 * @param topLevelAncestor the topLevelAncestor to set
	 */
	public void setTopLevelAncestor(TkConcept topLevelAncestor) {
		this.topLevelAncestor = topLevelAncestor;
	}

	/**
	 * @return the directInferredParents
	 */
	public List<TkConcept> getDirectInferredParents() {
		return directInferredParents;
	}

	/**
	 * @param directInferredParents the directInferredParents to set
	 */
	public void setDirectInferredParents(List<TkConcept> directInferredParents) {
		this.directInferredParents = directInferredParents;
	}

	/**
	 * @return the directInferredChildren
	 */
	public List<TkConcept> getDirectInferredChildren() {
		return directInferredChildren;
	}

	/**
	 * @param directInferredChildren the directInferredChildren to set
	 */
	public void setDirectInferredChildren(List<TkConcept> directInferredChildren) {
		this.directInferredChildren = directInferredChildren;
	}

	/**
	 * @return the directInferredSiblings
	 */
	public List<TkConcept> getDirectInferredSiblings() {
		return directInferredSiblings;
	}

	/**
	 * @param directInferredSiblings the directInferredSiblings to set
	 */
	public void setDirectInferredSiblings(List<TkConcept> directInferredSiblings) {
		this.directInferredSiblings = directInferredSiblings;
	}

	/**
	 * @return the otherConcepts
	 */
	public List<TkConcept> getOtherConcepts() {
		return otherConcepts;
	}

	/**
	 * @param otherConcepts the otherConcepts to set
	 */
	public void setOtherConcepts(List<TkConcept> otherConcepts) {
		this.otherConcepts = otherConcepts;
	}

	/**
	 * @return the additionalContext
	 */
	public String getAdditionalContext() {
		return additionalContext;
	}

	/**
	 * @param additionalContext the additionalContext to set
	 */
	public void setAdditionalContext(String additionalContext) {
		this.additionalContext = additionalContext;
	}
}
