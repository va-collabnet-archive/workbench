package org.ihtsdo.etypes;

import java.io.DataInput;
import java.io.IOException;

import org.dwfa.ace.api.I_RelPart;
import org.dwfa.ace.api.Terms;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.tk.dto.concept.component.relationship.TkRelationshipRevision;

public class ERelationshipRevision extends TkRelationshipRevision {

    public static final long serialVersionUID = 1;

    public ERelationshipRevision(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
        super(in, dataVersion);
    }

    public ERelationshipRevision(I_RelPart part) throws TerminologyException, IOException {
        characteristicUuid = Terms.get().nidToUuid(part.getCharacteristicId());
        refinabilityUuid = Terms.get().nidToUuid(part.getRefinabilityId());
        group = part.getGroup();
        typeUuid = Terms.get().nidToUuid(part.getTypeNid());
        pathUuid = Terms.get().nidToUuid(part.getPathNid());
        statusUuid = Terms.get().nidToUuid(part.getStatusNid());
        authorUuid    = Terms.get().nidToUuid(part.getAuthorNid());
        moduleUuid    = Terms.get().nidToUuid(part.getModuleNid());
        time = part.getTime();
    }

    public ERelationshipRevision() {
        super();
    }

}
