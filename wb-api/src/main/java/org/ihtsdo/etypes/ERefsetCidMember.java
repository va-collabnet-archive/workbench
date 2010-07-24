package org.ihtsdo.etypes;

import java.io.DataInput;
import java.io.IOException;
import java.util.ArrayList;

import org.dwfa.ace.api.I_Identify;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.api.ebr.I_ExtendByRef;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartCid;
import org.dwfa.ace.api.ebr.I_ExtendByRefVersion;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.tk.concept.component.refset.cid.TkRefsetCidMember;
import org.ihtsdo.tk.concept.component.refset.cid.TkRefsetCidRevision;

public class ERefsetCidMember extends TkRefsetCidMember {

    public static final long serialVersionUID = 1;

    public ERefsetCidMember(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
        super(in, dataVersion);
    }

    public ERefsetCidMember(I_ExtendByRef m) throws TerminologyException, IOException {
        if (I_Identify.class.isAssignableFrom(m.getClass())) {
            EConcept.convertId((I_Identify) m, this);
        } else {
            EConcept.convertId(Terms.get().getId(m.getMemberId()), this);
        }
        int partCount = m.getMutableParts().size();
        refsetUuid = nidToUuid(m.getRefsetId());
        componentUuid = nidToUuid(m.getComponentId());

        I_ExtendByRefPartCid part = (I_ExtendByRefPartCid) m.getMutableParts().get(0);
        c1Uuid = nidToUuid(part.getC1id());
        pathUuid = nidToUuid(part.getPathId());
        statusUuid = nidToUuid(part.getStatusId());
        time = part.getTime();
        if (partCount > 1) {
            revisions = new ArrayList<TkRefsetCidRevision>(partCount - 1);
            for (int i = 1; i < partCount; i++) {
                revisions.add(new ERefsetCidRevision((I_ExtendByRefPartCid) m.getMutableParts().get(i)));
            }
        }
    }

    public ERefsetCidMember() {
        super();
    }

    public ERefsetCidMember(I_ExtendByRefVersion m, I_Identify id) throws TerminologyException, IOException {
        EConcept.convertId(id, this);
        refsetUuid = nidToUuid(m.getRefsetId());
        componentUuid = nidToUuid(m.getComponentId());
        I_ExtendByRefPartCid part = (I_ExtendByRefPartCid) m.getMutablePart();
        c1Uuid = nidToUuid(part.getC1id());
        pathUuid = nidToUuid(part.getPathId());
        statusUuid = nidToUuid(part.getStatusId());
        time = part.getTime();
	}

}
