package org.dwfa.ace.task.refset.members.export;

import org.dwfa.cement.ArchitectonicAuxiliary;

import java.util.UUID;
import java.util.Collection;

public interface StatusUUIDs {

    Collection<UUID> CURRENT_STATUS_UUIDS = ArchitectonicAuxiliary.Concept.CURRENT.getUids();
    Collection<UUID> PREFERED_TERM_UUIDS = ArchitectonicAuxiliary.Concept.PREFERRED_DESCRIPTION_TYPE.getUids();
    Collection<UUID> FULLY_SPECIFIED_UUIDS = ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.getUids();

}
