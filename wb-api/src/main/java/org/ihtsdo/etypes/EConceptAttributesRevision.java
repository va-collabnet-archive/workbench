package org.ihtsdo.etypes;

import java.io.DataInput;
import java.io.IOException;

import org.dwfa.ace.api.I_ConceptAttributePart;
import org.dwfa.ace.api.Terms;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.tk.dto.concept.component.attribute.TkConceptAttributesRevision;

public class EConceptAttributesRevision extends TkConceptAttributesRevision {
    public static final long serialVersionUID = 1;

    public EConceptAttributesRevision(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
        super(in, dataVersion);
    }

    public EConceptAttributesRevision(I_ConceptAttributePart part) throws TerminologyException, IOException {
        defined = part.isDefined();
        pathUuid = Terms.get().nidToUuid(part.getPathId());
        statusUuid = Terms.get().nidToUuid(part.getStatusId());
        time = part.getTime();
    }

    public EConceptAttributesRevision() {
        super();
    }
}
