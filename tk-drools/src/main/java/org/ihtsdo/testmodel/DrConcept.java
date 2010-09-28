package org.ihtsdo.testmodel;

import java.util.ArrayList;
import java.util.List;

public class DrConcept extends DrComponent{
	private String primordialUuid;
	
	private boolean defined;
	private List<DrDescription> descriptions;
	private List<DrRelationship> incomingRelationships;
	private List<DrRelationship> outgoingRelationships;
	private List<DrRefsetExtension> extensions;
	private List<DrIdentifier> identifiers;
	
	private String specialConceptCategory;
	
	private int numberOfStatedParents;
	private boolean parentOfStatedChildren;
	private boolean sourceOfDefiningRole;
	private boolean targetOfDefiningRole;
	private boolean targetOfHistoricalAssociation;
	private String semanticTag;
	
	
	public DrConcept() {
		descriptions = new ArrayList<DrDescription>();
		incomingRelationships = new ArrayList<DrRelationship>();
		outgoingRelationships = new ArrayList<DrRelationship>();
		extensions = new ArrayList<DrRefsetExtension>();
		identifiers = new ArrayList<DrIdentifier>();
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
}
