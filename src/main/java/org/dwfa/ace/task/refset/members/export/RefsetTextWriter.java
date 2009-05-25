package org.dwfa.ace.task.refset.members.export;

import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPart;

import java.util.List;

public interface RefsetTextWriter {

    void writeRefset(I_GetConceptData concept, List<I_DescriptionTuple> descriptionTuples,
                     DescriptionWriter descriptionWriter, I_ThinExtByRefPart part,
                     NoDescriptionWriter noDescriptionWriter) throws Exception;
}
