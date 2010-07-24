package org.ihtsdo.etypes;

import java.io.DataInput;
import java.io.IOException;

import org.dwfa.ace.api.I_DescriptionPart;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.tk.concept.component.description.TkDescriptionRevision;

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
        typeUuid = nidToUuid(part.getTypeId());
        pathUuid = nidToUuid(part.getPathId());
        statusUuid = nidToUuid(part.getStatusId());
        time = part.getTime();
    }

    public EDescriptionRevision() {
        super();
    }
}
