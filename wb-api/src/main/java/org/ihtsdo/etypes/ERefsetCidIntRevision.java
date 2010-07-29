package org.ihtsdo.etypes;

import java.io.DataInput;
import java.io.IOException;

import org.dwfa.ace.api.ebr.I_ExtendByRefPartCidInt;
import org.dwfa.ace.api.Terms;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.tk.concept.component.refset.cidint.TkRefsetCidIntRevision;

public class ERefsetCidIntRevision extends TkRefsetCidIntRevision {

    public static final long serialVersionUID = 1;

    public ERefsetCidIntRevision(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
        super(in, dataVersion);
    }

    public ERefsetCidIntRevision(I_ExtendByRefPartCidInt part) throws TerminologyException, IOException {
        c1Uuid = Terms.get().nidToUuid(part.getC1id());
        intValue = part.getIntValue();
        pathUuid = Terms.get().nidToUuid(part.getPathId());
        statusUuid = Terms.get().nidToUuid(part.getStatusId());
        time = part.getTime();
    }

    public ERefsetCidIntRevision() {
        super();
    }

}
