package org.dwfa.ace.refset;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.dwfa.ace.api.I_ConceptAttributePart;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IdPart;
import org.dwfa.ace.api.I_IdVersioned;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPart;
import org.dwfa.ace.api.ebr.I_ThinExtByRefVersioned;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.tapi.TerminologyException;

public final class RefsetUtilImpl implements RefsetUtil {

    public I_ConceptAttributePart getLastestAttributePart(
            final I_GetConceptData refsetConcept) throws IOException {
        List<I_ConceptAttributePart> refsetAttibuteParts = refsetConcept
                .getConceptAttributes().getVersions();
        I_ConceptAttributePart latestAttributePart = null;
        for (I_ConceptAttributePart attributePart : refsetAttibuteParts) {
            if (latestAttributePart == null
                    || attributePart.getVersion() >= latestAttributePart
                            .getVersion()) {
                latestAttributePart = attributePart;
            }
        }
        return latestAttributePart;
    }

    public I_IntSet createIntSet(final I_TermFactory termFactory,
            final Collection<UUID> uuid) throws Exception {
        I_IntSet status = termFactory.newIntSet();
        status.add(termFactory.getConcept(uuid).getConceptId());
        status.add(ArchitectonicAuxiliary.getSnomedDescriptionStatusId(uuid));
        return status;
    }

    public I_ThinExtByRefPart getLatestVersionIfCurrent(
            final I_ThinExtByRefVersioned ext, final I_TermFactory termFactory)
            throws TerminologyException, IOException {
        I_ThinExtByRefPart latest = null;
        List<? extends I_ThinExtByRefPart> versions = ext.getVersions();
        for (I_ThinExtByRefPart version : versions) {

            if (latest == null) {
                latest = version;
            } else {
                if (latest.getVersion() < version.getVersion()) {
                    latest = version;
                }
            }
        }

        return latest;
    }

    public String getSnomedId(final int nid, final I_TermFactory termFactory)
            throws Exception {

        if (nid == 0) {
            return "no identifier";
        }

        I_IdVersioned idVersioned = termFactory.getId(nid);
        for (I_IdPart idPart : idVersioned.getVersions()) {
            if (idPart.getSource() == termFactory
                    .uuidToNative(ArchitectonicAuxiliary.Concept.SNOMED_INT_ID
                            .getUids())) {
                return idPart.getSourceId().toString();
            }
        }

        return "no SCTID found";
    }

    public <T> T assertExactlyOne(final Collection<T> collection) {
        assert collection.size() == 1 : "Collection " + collection
                + " was expected to only have one element";
        return collection.iterator().next();
    }

    public int getMarkedParentIsARelationshipTarget(
            final I_TermFactory termFactory, I_GetConceptData memberRefset)
            throws Exception {

        I_IntSet allowedStatus = termFactory.newIntSet();
        allowedStatus.add(termFactory.getConcept(
                ArchitectonicAuxiliary.Concept.CURRENT.getUids())
                .getConceptId());

        I_IntSet allowedType = termFactory.newIntSet();
        allowedType.add(termFactory.getConcept(
                RefsetAuxiliary.Concept.MARKED_PARENT_IS_A_TYPE.getUids())
                .getConceptId());

        Set<I_GetConceptData> requiredIsAType = memberRefset
                .getSourceRelTargets(allowedStatus, allowedType, null, false);

        if (requiredIsAType == null || requiredIsAType.size() == 0) {
            // no specified marked-parent-is-a relationship defined, so first
            // default to using snomed is-a
            List<UUID> snomedIsA = Arrays.asList(ConceptConstants.SNOMED_IS_A
                    .getUuids());
            if (termFactory.hasId(snomedIsA)) {
                return termFactory.getConcept(snomedIsA).getConceptId();
            } else { // second default if snomed is-a doesn't exist
                return termFactory.getConcept(
                        ArchitectonicAuxiliary.Concept.IS_A_REL.getUids())
                        .getConceptId();
            }
        } else { // relationship exists so use the is-a specified by the
            // marked-parent-is-a relationship
            return requiredIsAType.iterator().next().getConceptId();
        }
    }
}
