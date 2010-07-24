package org.ihtsdo.etypes;

import java.io.DataInput;
import java.io.IOException;

import org.dwfa.ace.api.ebr.I_ExtendByRefPartCidCid;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.tk.concept.component.refset.cidcid.TkRefsetCidCidRevision;

public class ERefsetCidCidRevision extends TkRefsetCidCidRevision {

    public static final long serialVersionUID = 1;

    public ERefsetCidCidRevision(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
        super();
        readExternal(in, dataVersion);
    }

    public ERefsetCidCidRevision(I_ExtendByRefPartCidCid part) throws TerminologyException, IOException {
        c1Uuid = nidToUuid(part.getC1id());
        c2Uuid = nidToUuid(part.getC2id());
        pathUuid = nidToUuid(part.getPathId());
        statusUuid = nidToUuid(part.getStatusId());
        time = part.getTime();
    }

    public ERefsetCidCidRevision()  {
        super();
    }

}
