package org.dwfa.ace.task.refset.members.export;

import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPart;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartConcept;
import org.dwfa.ace.task.refset.members.RefsetUtil;

import java.util.List;

public final class RefsetTextWriterImpl implements RefsetTextWriter {

    private final RefsetUtil refsetUtil;
    private final I_TermFactory termFactory;

    public RefsetTextWriterImpl(final RefsetUtil refsetUtil, final I_TermFactory termFactory) {
        this.refsetUtil = refsetUtil;
        this.termFactory = termFactory;
    }

    public void writeRefset(final I_GetConceptData concept, final List<I_DescriptionTuple> descriptionTuples,
                final DescriptionWriter descriptionWriter, final I_ThinExtByRefPart part,
                final NoDescriptionWriter noDescriptionWriter) throws Exception {
                
        I_GetConceptData value = termFactory.getConcept(((I_ThinExtByRefPartConcept) part).getConceptId());

        if (descriptionTuples.isEmpty()) {
            noDescriptionWriter.write(concept);
            return;
        }

        if (value.getConceptId() != refsetUtil.getLocalizedParentMarkerNid()) {
            descriptionWriter.write(concept, descriptionTuples);
        }
    }
}
