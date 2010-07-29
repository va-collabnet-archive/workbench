package org.ihtsdo.etypes;

import java.io.DataInput;
import java.io.IOException;
import java.util.ArrayList;

import org.dwfa.ace.api.I_Identify;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.api.ebr.I_ExtendByRef;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartInt;
import org.dwfa.ace.api.ebr.I_ExtendByRefVersion;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.tk.concept.component.refset.integer.TkRefsetIntMember;
import org.ihtsdo.tk.concept.component.refset.integer.TkRefsetIntRevision;

public class ERefsetIntMember extends TkRefsetIntMember {

    public static final long serialVersionUID = 1;

    public ERefsetIntMember(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
        super(in, dataVersion);
    }

    public ERefsetIntMember(I_ExtendByRef m) throws TerminologyException, IOException {
        if (I_Identify.class.isAssignableFrom(m.getClass())) {
            EConcept.convertId((I_Identify) m, this);
        } else {
            EConcept.convertId(Terms.get().getId(m.getMemberId()), this);
        }
        int partCount = m.getMutableParts().size();
        refsetUuid = Terms.get().nidToUuid(m.getRefsetId());
        componentUuid = Terms.get().nidToUuid(m.getComponentId());

        I_ExtendByRefPartInt part = (I_ExtendByRefPartInt) m.getMutableParts().get(0);
        intValue = part.getIntValue();
        pathUuid = Terms.get().nidToUuid(part.getPathId());
        statusUuid = Terms.get().nidToUuid(part.getStatusId());
        time = part.getTime();
        if (partCount > 1) {
            extraVersions = new ArrayList<TkRefsetIntRevision>(partCount - 1);
            for (int i = 1; i < partCount; i++) {
                extraVersions.add(new ERefsetIntRevision((I_ExtendByRefPartInt) m.getMutableParts().get(i)));
            }
        }
    }

    public ERefsetIntMember() {
        super();
    }

    public ERefsetIntMember(I_ExtendByRefVersion m) throws TerminologyException, IOException {
        if (I_Identify.class.isAssignableFrom(m.getClass())) {
            EConcept.convertId((I_Identify) m, this);
        } else {
            EConcept.convertId(Terms.get().getId(m.getMemberId()), this);
        }
        refsetUuid = Terms.get().nidToUuid(m.getRefsetId());
        componentUuid = Terms.get().nidToUuid(m.getComponentId());
        I_ExtendByRefPartInt part = (I_ExtendByRefPartInt) m.getMutablePart();
        intValue = part.getIntValue();
        pathUuid = Terms.get().nidToUuid(part.getPathId());
        statusUuid = Terms.get().nidToUuid(part.getStatusId());
        time = part.getTime();
	}
}
