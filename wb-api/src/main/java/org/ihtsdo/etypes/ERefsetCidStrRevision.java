package org.ihtsdo.etypes;

import java.io.DataInput;
import java.io.IOException;

import org.dwfa.ace.api.ebr.I_ExtendByRefPartCidString;
import org.dwfa.ace.api.Terms;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.tk.dto.concept.component.refset.cidstr.TkRefsetCidStrRevision;

public class ERefsetCidStrRevision extends TkRefsetCidStrRevision {

    public static final long serialVersionUID = 1;

    public ERefsetCidStrRevision(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
        super(in, dataVersion);
    }

    public ERefsetCidStrRevision(I_ExtendByRefPartCidString part) throws TerminologyException, IOException {
        c1Uuid = Terms.get().nidToUuid(part.getC1id());
        strValue = part.getStringValue();
        pathUuid = Terms.get().nidToUuid(part.getPathId());
        statusUuid = Terms.get().nidToUuid(part.getStatusId());
        time = part.getTime();
    }

    public ERefsetCidStrRevision() {
        super();
    }

}
