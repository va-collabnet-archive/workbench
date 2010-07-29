package org.ihtsdo.etypes;

import java.io.DataInput;
import java.io.IOException;

import org.dwfa.ace.api.ebr.I_ExtendByRefPartCidCid;
import org.dwfa.ace.api.Terms;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.tk.concept.component.refset.cidcid.TkRefsetCidCidRevision;

public class ERefsetCidCidRevision extends TkRefsetCidCidRevision {

    public static final long serialVersionUID = 1;

    public ERefsetCidCidRevision(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
        super();
        readExternal(in, dataVersion);
    }

    public ERefsetCidCidRevision(I_ExtendByRefPartCidCid part) throws TerminologyException, IOException {
        c1Uuid = Terms.get().nidToUuid(part.getC1id());
        c2Uuid = Terms.get().nidToUuid(part.getC2id());
        pathUuid = Terms.get().nidToUuid(part.getPathId());
        statusUuid = Terms.get().nidToUuid(part.getStatusId());
        time = part.getTime();
    }

    public ERefsetCidCidRevision()  {
        super();
    }

}
