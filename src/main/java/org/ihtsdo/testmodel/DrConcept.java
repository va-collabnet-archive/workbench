package org.ihtsdo.testmodel;

import java.util.ArrayList;
import java.util.List;

public class DrConcept extends DrComponent{
	private String primordialUuid;
	
	private boolean defined;
	private List<DrDescription> descriptions;
	private List<DrRelationship> incomingRelationships;
	private List<DrRelationship> outgoingRelationships;
	private List<DrDefiningRolesSet> definingRoleSets;
	private List<DrLanguageDesignationSet> languageDesignationSets;
	private List<DrRefsetExtension> extensions;
	private List<DrIdentifier> identifiers;
	
	//Inferred properties
	private String specialConceptCategory;
	private int numberOfStatedParents;
	private boolean parentOfStatedChildren;
	private boolean sourceOfDefiningRole;
	private boolean targetOfDefiningRole;
	private boolean targetOfHistoricalAssociation;
	private String semanticTag;
	private boolean variantEvaluationCandidate;
	private List<String> listOfDomainsUuids;
	private String effectiveDomain;
	
	
	public DrConcept() {
		descriptions = new ArrayList<DrDescription>();
		incomingRelationships = new ArrayList<DrRelationship>();
		outgoingRelationships = new ArrayList<DrRelationship>();
		definingRoleSets = new ArrayList<DrDefiningRolesSet>();
		languageDesignationSets = new ArrayList<DrLanguageDesignationSet>();
		extensions = new ArrayList<DrRefsetExtension>();
		identifiers = new ArrayList<DrIdentifier>();
		numberOfStatedParents=0;
		variantEvaluationCandidate=true;
		parentOfStatedChildren=false;
		sourceOfDefiningRole=false;
		targetOfHistoricalAssociation=false;
		targetOfDefiningRole=false;
		
	}

	public String getPrimordialUuid() {
		return primordialUuid;
	}

	public void setPrimordialUuid(String primordialUuid) {
		this.primordialUuid = primordialUuid;
	}

	public boolean isDefined() {
		return defined;
	}

	public void setDefined(boolean defined) {
		this.defined = defined;
	}

	public List<DrDescription> getDescriptions() {
		return descriptions;
	}

	public void setDescriptions(List<DrDescription> descriptions) {
		this.descriptions = descriptions;
	}

	public List<DrRelationship> getIncomingRelationships() {
		return incomingRelationships;
	}

	public void setIncomingRelationships(List<DrRelationship> incomingRelationships) {
		this.incomingRelationships = incomingRelationships;
	}

	public List<DrRelationship> getOutgoingRelationships() {
		return outgoingRelationships;
	}

	public void setOutgoingRelationships(List<DrRelationship> outgoingRelationships) {
		this.outgoingRelationships = outgoingRelationships;
	}

	public List<DrRefsetExtension> getExtensions() {
		return extensions;
	}

	public void setExtensions(List<DrRefsetExtension> extensions) {
		this.extensions = extensions;
	}

	public List<DrIdentifier> getIdentifiers() {
		return identifiers;
	}

	public void setIdentifiers(List<DrIdentifier> identifiers) {
		this.identifiers = identifiers;
	}

	public String getSpecialConceptCategory() {
		return specialConceptCategory;
	}

	public void setSpecialConceptCategory(String specialConceptCategory) {
		this.specialConceptCategory = specialConceptCategory;
	}

	public int getNumberOfStatedParents() {
		return numberOfStatedParents;
	}

	public void setNumberOfStatedParents(int numberOfStatedParents) {
		this.numberOfStatedParents = numberOfStatedParents;
	}

	public boolean isParentOfStatedChildren() {
		return parentOfStatedChildren;
	}

	public void setParentOfStatedChildren(boolean parentOfStatedChildren) {
		this.parentOfStatedChildren = parentOfStatedChildren;
	}

	public boolean isSourceOfDefiningRole() {
		return sourceOfDefiningRole;
	}

	public void setSourceOfDefiningRole(boolean sourceOfDefiningRole) {
		this.sourceOfDefiningRole = sourceOfDefiningRole;
	}

	public boolean isTargetOfDefiningRole() {
		return targetOfDefiningRole;
	}

	public void setTargetOfDefiningRole(boolean targetOfDefiningRole) {
		this.targetOfDefiningRole = targetOfDefiningRole;
	}

	public boolean isTargetOfHistoricalAssociation() {
		return targetOfHistoricalAssociation;
	}

	public void setTargetOfHistoricalAssociation(
			boolean targetOfHistoricalAssociation) {
		this.targetOfHistoricalAssociation = targetOfHistoricalAssociation;
	}

	public String getSemanticTag() {
		return semanticTag;
	}

	public void setSemanticTag(String semanticTag) {
		this.semanticTag = semanticTag;
	}

	public boolean isVariantEvaluationCandidate() {
		return variantEvaluationCandidate;
	}

	public void setVariantEvaluationCandidate(boolean variantEvaluationCandidate) {
		this.variantEvaluationCandidate = variantEvaluationCandidate;
	}

	public List<String> getListOfDomainsUuids() {
		return listOfDomainsUuids;
	}

	public void setListOfDomainsUuids(List<String> listOfDomainsUuids) {
		this.listOfDomainsUuids = listOfDomainsUuids;
	}

	public String getEffectiveDomain() {
		return effectiveDomain;
	}

	public void setEffectiveDomain(String effectiveDomain) {
		this.effectiveDomain = effectiveDomain;
	}

	public List<DrDefiningRolesSet> getDefiningRoleSets() {
		return definingRoleSets;
	}

	public void setDefiningRolesSets(List<DrDefiningRolesSet> definingRoleSets) {
		this.definingRoleSets = definingRoleSets;
	}

	public List<DrLanguageDesignationSet> getLanguageDesignationSets() {
		return languageDesignationSets;
	}

	public void setLanguageDesignationSets(
			List<DrLanguageDesignationSet> languageDesignationSets) {
		this.languageDesignationSets = languageDesignationSets;
	}

}
