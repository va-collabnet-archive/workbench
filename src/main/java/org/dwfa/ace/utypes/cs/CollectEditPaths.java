package org.dwfa.ace.utypes.cs;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.dwfa.ace.utypes.UniversalAceConceptAttributesPart;
import org.dwfa.ace.utypes.UniversalAceDescriptionPart;
import org.dwfa.ace.utypes.UniversalAceExtByRefPart;
import org.dwfa.ace.utypes.UniversalAceIdentificationPart;
import org.dwfa.ace.utypes.UniversalAceImagePart;
import org.dwfa.ace.utypes.UniversalAcePath;
import org.dwfa.ace.utypes.UniversalAceRelationshipPart;

public class CollectEditPaths extends AbstractUncommittedProcessor {

	Set<UUID> pathSet = new HashSet<UUID>();
	
	public Set<UUID> getPathSet() {
		return pathSet;
	}

	@Override
	protected void processNewUniversalAcePath(UniversalAcePath path) {
		//Nothing to do...

	}

	@Override
	protected void processUncommittedUniversalAceConceptAttributesPart(
			UniversalAceConceptAttributesPart part) {
		pathSet.addAll(part.getPathId());

	}

	@Override
	protected void processUncommittedUniversalAceDescriptionPart(
			UniversalAceDescriptionPart part) {
		pathSet.addAll(part.getPathId());
	}

	@Override
	protected void processUncommittedUniversalAceExtByRefPart(
			UniversalAceExtByRefPart part) {
		pathSet.addAll(part.getPathUid());
	}

	@Override
	protected void processUncommittedUniversalAceIdentificationPart(
			UniversalAceIdentificationPart part) {
		pathSet.addAll(part.getPathId());

	}

	@Override
	protected void processUncommittedUniversalAceImagePart(
			UniversalAceImagePart part) {
		pathSet.addAll(part.getPathId());

	}

	@Override
	protected void processUncommittedUniversalAceRelationshipPart(
			UniversalAceRelationshipPart part) {
		pathSet.addAll(part.getPathId());
	}

}
