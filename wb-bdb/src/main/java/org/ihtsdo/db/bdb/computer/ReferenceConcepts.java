package org.ihtsdo.db.bdb.computer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.cement.SNOMED;
import org.ihtsdo.db.bdb.Bdb;

public enum ReferenceConcepts {
	REFSET_PATHS(RefsetAuxiliary.Concept.REFSET_PATHS.getUids()),
	PATH(ArchitectonicAuxiliary.Concept.PATH.getUids()),
	REFSET_PATH_ORIGINS(RefsetAuxiliary.Concept.REFSET_PATH_ORIGINS.getUids()),
	TERM_AUXILIARY_PATH(ArchitectonicAuxiliary.Concept.ARCHITECTONIC_BRANCH.getUids()),
	CURRENT(ArchitectonicAuxiliary.Concept.CURRENT.getUids()),
	RETIRED(ArchitectonicAuxiliary.Concept.RETIRED.getUids()),
	CONCEPT_EXTENSION(RefsetAuxiliary.Concept.CONCEPT_EXTENSION.getUids()),
	REFSET_MEMBER_PURPOSE(RefsetAuxiliary.Concept.REFSET_MEMBER_PURPOSE.getUids()),
	REFSET_IDENTITY(RefsetAuxiliary.Concept.REFSET_IDENTITY.getUids()),
	AUX_IS_A(ArchitectonicAuxiliary.Concept.IS_A_REL.getUids()),
	SNOMED_IS_A(SNOMED.Concept.IS_A.getUids()),
	MARKED_PARENT_IS_A_TYPE(RefsetAuxiliary.Concept.MARKED_PARENT_IS_A_TYPE.getUids()),
    NORMAL_MEMBER(RefsetAuxiliary.Concept.NORMAL_MEMBER.getUids()),
	;
	
	private int nid;
	private List<UUID> uuids;
	private ReferenceConcepts(Collection<UUID> uuids) {
		this.uuids = new ArrayList<UUID>(uuids);
		this.nid = Bdb.uuidsToNid(uuids);
	}
	public int getNid() {
		return nid;
	}
	public List<UUID> getUuids() {
		return uuids;
	}
}
