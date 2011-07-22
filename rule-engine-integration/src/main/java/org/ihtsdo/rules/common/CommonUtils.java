package org.ihtsdo.rules.common;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.PRECEDENCE;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.api.ebr.I_ExtendByRef;
import org.dwfa.ace.api.ebr.I_ExtendByRefPart;
import org.dwfa.ace.api.ebr.I_ExtendByRefVersion;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRf2;

public class CommonUtils {
	public static I_ExtendByRefPart getLastExtensionPart(I_ExtendByRef extension) throws TerminologyException, IOException {
		long lastVersion = Long.MIN_VALUE;
		I_ConfigAceFrame config = Terms.get().getActiveAceFrameConfig();
		I_IntSet allowedStatus = Terms.get().newIntSet();
		allowedStatus.addAll(config.getAllowedStatus().getSetValues());
		allowedStatus.add(ArchitectonicAuxiliary.Concept.INACTIVE.localize().getNid());
		allowedStatus.add(ArchitectonicAuxiliary.Concept.RETIRED.localize().getNid());
		allowedStatus.add(SnomedMetadataRf2.INACTIVE_VALUE_RF2.getLenient().getNid());
		I_ExtendByRefPart lastPart = null;
		for (I_ExtendByRefVersion loopTuple : extension.getTuples(allowedStatus, config.getViewPositionSetReadOnly(), PRECEDENCE.TIME.getTkPrecedence(), config.getConflictResolutionStrategy())) {
			List<? extends I_ExtendByRefPart> versions = loopTuple.getVersions();
			for (I_ExtendByRefPart version : versions) {
				if(version.getTime() > lastVersion){
					lastVersion = version.getTime();
					lastPart = loopTuple.getMutablePart();
				}
			}
		}

		if (lastPart == null) {
			throw new TerminologyException("No parts on this viewpositionset.");
		}

		return lastPart;
	}
	
	public static boolean isActive(int statusId) {
		List<Integer> activeStatuses = new ArrayList<Integer>();
		try {
			activeStatuses.add(ArchitectonicAuxiliary.Concept.ACTIVE.localize().getNid());
			activeStatuses.add(SnomedMetadataRf2.ACTIVE_VALUE_RF2.getLenient().getNid());
			activeStatuses.add(ArchitectonicAuxiliary.Concept.CURRENT.localize().getNid());
			activeStatuses.add(ArchitectonicAuxiliary.Concept.LIMITED.localize().getNid());
		} catch (IOException e) {
			e.printStackTrace();
		} catch (TerminologyException e) {
			e.printStackTrace();
		}
		return (activeStatuses.contains(statusId));
	}
	
}
