package org.ihtsdo.etypes;

import java.io.DataInput;
import java.io.IOException;

import org.dwfa.ace.api.Terms;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartCid;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.tk.dto.concept.component.refset.cid.TkRefsetCidRevision;

public class ERefsetCidRevision extends TkRefsetCidRevision {

    public static final long serialVersionUID = 1;

    public ERefsetCidRevision(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
        super(in, dataVersion);
    }

    public ERefsetCidRevision(I_ExtendByRefPartCid part) throws TerminologyException, IOException {
        c1Uuid = Terms.get().nidToUuid(part.getC1id());
        pathUuid = Terms.get().nidToUuid(part.getPathId());
        statusUuid = Terms.get().nidToUuid(part.getStatusId());
        time = part.getTime();
    }

    public ERefsetCidRevision() {
        super();
    }
}
