package org.ihtsdo.etypes;

import java.io.DataInput;
import java.io.IOException;
import java.util.UUID;

import org.dwfa.ace.api.I_IdPart;
import org.dwfa.ace.api.Terms;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.tk.dto.concept.component.TkComponent;
import org.ihtsdo.tk.dto.concept.component.identifier.TkIdentifierUuid;

public class EIdentifierUuid extends TkIdentifierUuid {

    public static final long serialVersionUID = 1;

    public EIdentifierUuid(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
        super(in, dataVersion);
    }

    public EIdentifierUuid(I_IdPart idp) throws TerminologyException, IOException {
        denotation = (UUID) idp.getDenotation();
        authorityUuid = Terms.get().nidToUuid(idp.getAuthorityNid());
        pathUuid = Terms.get().nidToUuid(idp.getPathId());
        statusUuid = Terms.get().nidToUuid(idp.getStatusId());
        time = idp.getTime();
    }

    public EIdentifierUuid() {
        super();
    }

    public EIdentifierUuid(TkComponent<?> eComponent) {
        denotation = eComponent.primordialUuid;
        authorityUuid = eComponent.getAuthorUuid();
        pathUuid = eComponent.getPathUuid();
        statusUuid = eComponent.getStatusUuid();
        time = eComponent.getTime();
    }
}
