package org.ihtsdo.etypes;

import java.io.DataInput;
import java.io.IOException;

import org.dwfa.ace.api.I_IdPart;
import org.dwfa.ace.api.Terms;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.tk.concept.component.identifier.TkIdentifierString;

public class EIdentifierString extends TkIdentifierString {

    public static final long serialVersionUID = 1;

    public EIdentifierString(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
        super(in, dataVersion);
    }

    public EIdentifierString(I_IdPart idp) throws TerminologyException, IOException {
        denotation = (String) idp.getDenotation();
        authorityUuid = Terms.get().nidToUuid(idp.getAuthorityNid());
        pathUuid = Terms.get().nidToUuid(idp.getPathId());
        statusUuid = Terms.get().nidToUuid(idp.getStatusId());
        time = idp.getTime();
    }

    public EIdentifierString() {
        super();
    }
}
