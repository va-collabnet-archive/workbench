package org.ihtsdo.etypes;

import java.io.DataInput;
import java.io.IOException;
import java.util.ArrayList;

import org.dwfa.ace.api.I_Identify;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.api.ebr.I_ExtendByRef;
import org.dwfa.ace.api.ebr.I_ExtendByRefPart;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.tk.dto.concept.component.refex.type_member.TkRefexMember;
import org.ihtsdo.tk.dto.concept.component.refex.type_member.TkRefexRevision;

public class ERefsetMemberMember extends TkRefexMember {

    public static final long serialVersionUID = 1;

    public ERefsetMemberMember() {
        super();
    }

    public ERefsetMemberMember(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
        super(in, dataVersion);
    }

    public ERefsetMemberMember(I_ExtendByRef m) throws TerminologyException, IOException {
        if (I_Identify.class.isAssignableFrom(m.getClass())) {
            EConcept.convertId((I_Identify) m, this);
        } else {
            EConcept.convertId(Terms.get().getId(m.getMemberId()), this);
        }
        int partCount = m.getMutableParts().size();
        refsetUuid = Terms.get().nidToUuid(m.getRefsetId());
        componentUuid = Terms.get().nidToUuid(m.getComponentNid());

        I_ExtendByRefPart part = (I_ExtendByRefPart) m.getMutableParts().get(0);
        pathUuid = Terms.get().nidToUuid(part.getPathNid());
        statusUuid = Terms.get().nidToUuid(part.getStatusNid());
        authorUuid    = Terms.get().nidToUuid(part.getAuthorNid());
        moduleUuid    = Terms.get().nidToUuid(part.getModuleNid());
        time = part.getTime();
        if (partCount > 1) {
            revisions = new ArrayList<TkRefexRevision>(partCount - 1);
            for (int i = 1; i < partCount; i++) {
                revisions.add(new ERefsetRevision((I_ExtendByRefPart) m.getMutableParts().get(i)));
            }
        }
    }
}
