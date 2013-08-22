package org.ihtsdo.workunit.sif;

import java.util.Set;

public class SifContext {
	
	private Set<SifConceptDescriptor> parents;
	private Set<SifConceptDescriptor> children;
	private Set<SifConceptDescriptor> siblings;
	private Set<SifConceptDescriptor> relationshipTargets;
	private Set<SifConceptDescriptor> metadata;
	private String extraHMTLInfo;

	public SifContext() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @return the parents
	 */
	public Set<SifConceptDescriptor> getParents() {
		return parents;
	}

	/**
	 * @param parents the parents to set
	 */
	public void setParents(Set<SifConceptDescriptor> parents) {
		this.parents = parents;
	}

	/**
	 * @return the children
	 */
	public Set<SifConceptDescriptor> getChildren() {
		return children;
	}

	/**
	 * @param children the children to set
	 */
	public void setChildren(Set<SifConceptDescriptor> children) {
		this.children = children;
	}

	/**
	 * @return the siblings
	 */
	public Set<SifConceptDescriptor> getSiblings() {
		return siblings;
	}

	/**
	 * @param siblings the siblings to set
	 */
	public void setSiblings(Set<SifConceptDescriptor> siblings) {
		this.siblings = siblings;
	}

	/**
	 * @return the relationshipTargets
	 */
	public Set<SifConceptDescriptor> getRelationshipTargets() {
		return relationshipTargets;
	}

	/**
	 * @param relationshipTargets the relationshipTargets to set
	 */
	public void setRelationshipTargets(Set<SifConceptDescriptor> relationshipTargets) {
		this.relationshipTargets = relationshipTargets;
	}

	/**
	 * @return the metadata
	 */
	public Set<SifConceptDescriptor> getMetadata() {
		return metadata;
	}

	/**
	 * @param metadata the metadata to set
	 */
	public void setMetadata(Set<SifConceptDescriptor> metadata) {
		this.metadata = metadata;
	}

	/**
	 * @return the extraHMTLInfo
	 */
	public String getExtraHMTLInfo() {
		return extraHMTLInfo;
	}

	/**
	 * @param extraHMTLInfo the extraHMTLInfo to set
	 */
	public void setExtraHMTLInfo(String extraHMTLInfo) {
		this.extraHMTLInfo = extraHMTLInfo;
	}


}
