package org.ihtsdo.etypes;

import java.io.DataInput;
import java.io.IOException;

import org.dwfa.ace.api.ebr.I_ExtendByRefPartCidCidCid;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.tk.concept.component.refset.cidcidcid.TkRefsetCidCidCidRevision;

public class ERefsetCidCidCidRevision extends TkRefsetCidCidCidRevision {

    public static final long serialVersionUID = 1;

    public ERefsetCidCidCidRevision(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
        super(in, dataVersion);
    }

    public ERefsetCidCidCidRevision(I_ExtendByRefPartCidCidCid part) throws TerminologyException,
            IOException {
        c1Uuid = nidToUuid(part.getC1id());
        c2Uuid = nidToUuid(part.getC2id());
        c3Uuid = nidToUuid(part.getC3id());
        pathUuid = nidToUuid(part.getPathId());
        statusUuid = nidToUuid(part.getStatusId());
        time = part.getTime();
    }

    public ERefsetCidCidCidRevision() {
        super();
    }

}
