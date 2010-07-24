package org.ihtsdo.etypes;

import java.io.DataInput;
import java.io.IOException;

import org.dwfa.ace.api.ebr.I_ExtendByRefPartCidCidString;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.tk.concept.component.refset.cidcidstr.TkRefsetCidCidStrRevision;

public class ERefsetCidCidStrRevision extends TkRefsetCidCidStrRevision {

    public static final long serialVersionUID = 1;

    public ERefsetCidCidStrRevision(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
        super(in, dataVersion);
    }

    public ERefsetCidCidStrRevision(I_ExtendByRefPartCidCidString part) throws TerminologyException,
            IOException {
        c1Uuid = nidToUuid(part.getC1id());
        c2Uuid = nidToUuid(part.getC2id());
        stringValue = part.getStringValue();
        pathUuid = nidToUuid(part.getPathId());
        statusUuid = nidToUuid(part.getStatusId());
        time = part.getTime();
    }

    public ERefsetCidCidStrRevision() {
        super();
    }
}
