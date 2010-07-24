package org.ihtsdo.etypes;

import java.io.DataInput;
import java.io.IOException;
import java.util.ArrayList;

import org.dwfa.ace.api.I_DescriptionPart;
import org.dwfa.ace.api.I_DescriptionVersioned;
import org.dwfa.ace.api.Terms;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.tk.concept.component.description.TkDescription;
import org.ihtsdo.tk.concept.component.description.TkDescriptionRevision;

public class EDescription extends TkDescription {
    public static final long serialVersionUID = 1;


    public EDescription(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
        super(in, dataVersion);
    }

    public EDescription(I_DescriptionVersioned desc) throws TerminologyException, IOException {
        EConcept.convertId(Terms.get().getId(desc.getNid()), this);
        int partCount = desc.getMutableParts().size();
        I_DescriptionPart part = desc.getMutableParts().get(0);
        conceptUuid = nidToUuid(desc.getConceptId());
        initialCaseSignificant = part.isInitialCaseSignificant();
        lang = part.getLang();
        text = part.getText();
        typeUuid = nidToUuid(part.getTypeId());
        pathUuid = nidToUuid(part.getPathId());
        statusUuid = nidToUuid(part.getStatusId());
        time = part.getTime();
        if (partCount > 1) {
            revisions = new ArrayList<TkDescriptionRevision>(partCount - 1);
            for (int i = 1; i < partCount; i++) {
                revisions.add(new EDescriptionRevision(desc.getMutableParts().get(i)));
            }
        }
    }

    public EDescription() {
        super();
    }
}
