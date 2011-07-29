package org.ihtsdo.testmodel;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.concept.ConceptChronicleBI;

public class DrConcept extends DrComponent {
	private String primordialUuid;

	private boolean defined;
	private List<DrDescription> descriptions;
	private List<DrRelationship> incomingRelationships;
	private List<DrRelationship> outgoingRelationships;
	private List<DrDefiningRolesSet> definingRoleSets;
	private List<DrLanguageDesignationSet> languageDesignationSets;
	private List<DrRefsetExtension> extensions;
	private List<DrIdentifier> identifiers;

	// Inferred properties
	private String specialConceptCategory = "";
	private int numberOfStatedParents = 0;
	private int numberOfIncomingAssociations = 0;
	private boolean parentOfStatedChildren = false;
	private boolean sourceOfDefiningRole = false;
	private boolean targetOfDefiningRole = false;
	private boolean targetOfHistoricalAssociation = false;
	private String semanticTag = "";
	private boolean variantEvaluationCandidate = false;
	private List<String> listOfDomainsUuids;
	private String effectiveDomain = "";

	@Override
	public String toString() {
		StringBuffer conceptSb = new StringBuffer("");
		ConceptChronicleBI concept = null;
		try {
			concept = Ts.get().getConcept(UUID.fromString(primordialUuid));
			conceptSb.append(concept.toString() + "(" + primordialUuid + "),");
			conceptSb.append(" defined: " + defined + ",");
			conceptSb.append(" specialConceptCategory: " + specialConceptCategory + ",");
			conceptSb.append(" numberOfStatedParents: " + numberOfStatedParents + ",");
			conceptSb.append(" numberOfIncomingAssociations: " + numberOfIncomingAssociations + ",");
			conceptSb.append(" parentOfStatedChildren: " + parentOfStatedChildren + ",");
			conceptSb.append(" sourceOfDefiningRole: " + sourceOfDefiningRole + ",");
			conceptSb.append(" targetOfDefiningRole: " + targetOfDefiningRole + ",");
			conceptSb.append(" targetOfHistoricalAssociation: " + targetOfHistoricalAssociation + ",");
			conceptSb.append(" semanticTag: " + semanticTag + ",");
			conceptSb.append(" variantEvaluationCandidate: " + variantEvaluationCandidate + ",");
			conceptSb.append(" specialConceptCategory: " + specialConceptCategory + ",");
			conceptSb.append(" effectiveDomain: " + effectiveDomain + ",");
			conceptSb.append("tdomainUuids: [");
			if (listOfDomainsUuids != null) {
				for (String domainUuid : listOfDomainsUuids) {
					ConceptChronicleBI domainConcept = Ts.get().getConcept(UUID.fromString(domainUuid));
					conceptSb.append(domainConcept + " (" + domainUuid + "),");
				}
			}
			conceptSb.append("], ");

			conceptSb.append("Descriptions: [");
			if (descriptions != null) {
				int i = 0;
				for (DrDescription description : descriptions) {
					conceptSb.append(description.toString() + (i == descriptions.size() - 1 ? "" : ","));
					i++;
				}
			}
			conceptSb.append("], ");

			conceptSb.append("Incoming Relationships: [");
			if (incomingRelationships != null) {
				int i = 0;
				for (DrRelationship incomingRel : incomingRelationships) {
					conceptSb.append(incomingRel.toString() + (i == incomingRelationships.size() - 1 ? "" : ","));
					i++;
				}
			}

			conceptSb.append("Outgoing Relationships: [");
			if (outgoingRelationships != null) {
				int i = 0;
				for (DrRelationship outgoingRel : outgoingRelationships) {
					conceptSb.append(outgoingRel.toString() + (i == outgoingRelationships.size() - 1 ? "" : ","));
					i++;
				}
			}
			conceptSb.append("], ");

			conceptSb.append("Defining Roles: [");
			if (definingRoleSets != null) {
				int i = 0;
				for (DrDefiningRolesSet defRole : definingRoleSets) {
					conceptSb.append(defRole.toString() + (i == definingRoleSets.size() - 1 ? "" : ","));
					i++;
				}
			}

			conceptSb.append("Language Designation Sets: [");
			if (languageDesignationSets != null) {
				int i = 0;
				for (DrLanguageDesignationSet langDes : languageDesignationSets) {
					conceptSb.append(langDes.toString() + (i == languageDesignationSets.size() - 1 ? "" : ","));
					i++;
				}
			}
			conceptSb.append("], ");

			conceptSb.append("Extensions: [");
			if (extensions != null) {
				int i = 0;
				for (DrRefsetExtension ext : extensions) {
					conceptSb.append(ext.toString() + (i == extensions.size() - 1 ? "" : ","));
					i++;
				}
			}
			conceptSb.append("], ");

			conceptSb.append("Identifiers: [");
			if (identifiers != null) {
				for (DrIdentifier identifier : identifiers) {
					int i = 0;
					conceptSb.append(identifier.toString() + (i == identifiers.size() - 1 ? "" : ","));
					i++;
				}
			}
			conceptSb.append("]");
		} catch (IOException e) {
			e.printStackTrace();
		}
		return conceptSb.toString();
	}

	public DrConcept() {
		descriptions = new ArrayList<DrDescription>();
		incomingRelationships = new ArrayList<DrRelationship>();
		outgoingRelationships = new ArrayList<DrRelationship>();
		definingRoleSets = new ArrayList<DrDefiningRolesSet>();
		languageDesignationSets = new ArrayList<DrLanguageDesignationSet>();
		extensions = new ArrayList<DrRefsetExtension>();
		identifiers = new ArrayList<DrIdentifier>();
		numberOfStatedParents = 0;
		variantEvaluationCandidate = true;
		parentOfStatedChildren = false;
		sourceOfDefiningRole = false;
		targetOfHistoricalAssociation = false;
		targetOfDefiningRole = false;

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

	public void setTargetOfHistoricalAssociation(boolean targetOfHistoricalAssociation) {
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

	public void setDefiningRoleSets(List<DrDefiningRolesSet> definingRoleSets) {
		this.definingRoleSets = definingRoleSets;
	}

	public List<DrLanguageDesignationSet> getLanguageDesignationSets() {
		return languageDesignationSets;
	}

	public void setLanguageDesignationSets(List<DrLanguageDesignationSet> languageDesignationSets) {
		this.languageDesignationSets = languageDesignationSets;
	}

	public int getNumberOfIncomingAssociations() {
		return numberOfIncomingAssociations;
	}

	public void setNumberOfIncomingAssociations(int numberOfIncomingAssociations) {
		this.numberOfIncomingAssociations = numberOfIncomingAssociations;
	}

}
