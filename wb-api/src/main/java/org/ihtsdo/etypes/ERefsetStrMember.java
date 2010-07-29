package org.ihtsdo.etypes;

import java.io.DataInput;
import java.io.IOException;
import java.util.ArrayList;

import org.dwfa.ace.api.I_Identify;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.api.ebr.I_ExtendByRef;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartStr;
import org.dwfa.ace.api.ebr.I_ExtendByRefVersion;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.tk.concept.component.refset.str.TkRefsetStrMember;
import org.ihtsdo.tk.concept.component.refset.str.TkRefsetStrRevision;

public class ERefsetStrMember extends TkRefsetStrMember {

    public static final long serialVersionUID = 1;

    public ERefsetStrMember(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
        super(in, dataVersion);
    }

    public ERefsetStrMember(I_ExtendByRef m) throws TerminologyException, IOException {
        if (I_Identify.class.isAssignableFrom(m.getClass())) {
            EConcept.convertId((I_Identify) m, this);
        } else {
            EConcept.convertId(Terms.get().getId(m.getMemberId()), this);
        }
        int partCount = m.getMutableParts().size();
        refsetUuid = Terms.get().nidToUuid(m.getRefsetId());
        componentUuid = Terms.get().nidToUuid(m.getComponentId());

        I_ExtendByRefPartStr part = (I_ExtendByRefPartStr) m.getMutableParts().get(0);
        strValue = part.getStringValue();
        pathUuid = Terms.get().nidToUuid(part.getPathId());
        statusUuid = Terms.get().nidToUuid(part.getStatusId());
        time = part.getTime();
        if (partCount > 1) {
            revisions = new ArrayList<TkRefsetStrRevision>(partCount - 1);
            for (int i = 1; i < partCount; i++) {
                revisions.add(new ERefsetStrRevision((I_ExtendByRefPartStr) m.getMutableParts().get(i)));
            }
        }
    }

    public ERefsetStrMember() {
        super();
    }

    public ERefsetStrMember(I_ExtendByRefVersion m) throws TerminologyException, IOException {
        if (I_Identify.class.isAssignableFrom(m.getClass())) {
            EConcept.convertId((I_Identify) m, this);
        } else {
            EConcept.convertId(Terms.get().getId(m.getMemberId()), this);
        }
        refsetUuid = Terms.get().nidToUuid(m.getRefsetId());
        componentUuid = Terms.get().nidToUuid(m.getComponentId());
        I_ExtendByRefPartStr part = (I_ExtendByRefPartStr) m.getMutablePart();
        strValue = part.getStringValue();
        pathUuid = Terms.get().nidToUuid(part.getPathId());
        statusUuid = Terms.get().nidToUuid(part.getStatusId());
        time = part.getTime();
	}
}
