package org.ihtsdo.etypes;

import java.io.DataInput;
import java.io.IOException;

import org.dwfa.ace.api.I_DescriptionPart;
import org.dwfa.ace.api.Terms;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.tk.dto.concept.component.description.TkDescriptionRevision;

public class EDescriptionRevision extends TkDescriptionRevision {

    public static final long serialVersionUID = 1;

    public EDescriptionRevision(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
        super();
        readExternal(in, dataVersion);
    }

    public EDescriptionRevision(I_DescriptionPart part) throws TerminologyException, IOException {
        initialCaseSignificant = part.isInitialCaseSignificant();
        lang = part.getLang();
        text = part.getText();
        typeUuid = Terms.get().nidToUuid(part.getTypeId());
        pathUuid = Terms.get().nidToUuid(part.getPathId());
        statusUuid = Terms.get().nidToUuid(part.getStatusId());
        time = part.getTime();
    }

    public EDescriptionRevision() {
        super();
    }
}
