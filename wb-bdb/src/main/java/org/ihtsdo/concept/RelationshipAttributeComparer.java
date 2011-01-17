package org.ihtsdo.concept;

import org.ihtsdo.concept.ConflictIdentifier.CONTRADICTION_INVESTIGATION_TYPE;
import org.ihtsdo.tk.api.ComponentVersionBI;
import org.ihtsdo.tk.api.relationship.RelationshipVersionBI;

public class RelationshipAttributeComparer extends AttributeComparer {

	private int lcaOriginNid = 0;
	private int lcaDestinationNid = 0;
	private int lcaRefinabilityNid = 0;
	private int lcaCharacteristicNid = 0; 
	private int lcaGroup = 0;
	private int lcaStatusNid = 0;
	
	public RelationshipAttributeComparer() {
		super();
		componentType = CONTRADICTION_INVESTIGATION_TYPE.RELATIONSHIP;
	}


	@Override
	boolean hasSameAttributes(ComponentVersionBI v) {
		RelationshipVersionBI relAttributeVersion = (RelationshipVersionBI)v;

		if ((relAttributeVersion.getOriginNid() != lcaOriginNid) ||
			(relAttributeVersion.getDestinationNid() != lcaDestinationNid)||
			(relAttributeVersion.getRefinabilityNid() != lcaRefinabilityNid)||
			(relAttributeVersion.getCharacteristicNid() != lcaCharacteristicNid)||
			(relAttributeVersion.getGroup() != lcaGroup)||
			(relAttributeVersion.getStatusNid() != lcaStatusNid))
			return false;
		
		return true;
	}
 
	@Override
	public void initializeAttributes(ComponentVersionBI v) {
		RelationshipVersionBI relAttributeVersion = (RelationshipVersionBI)v;
		comparerInitialized = true;

		lcaOriginNid = relAttributeVersion.getOriginNid();
		lcaDestinationNid = relAttributeVersion.getDestinationNid();
		lcaRefinabilityNid = relAttributeVersion.getRefinabilityNid();
		lcaCharacteristicNid = relAttributeVersion.getCharacteristicNid();
		lcaGroup = relAttributeVersion.getGroup();
		lcaStatusNid = relAttributeVersion.getStatusNid();
	}

}
