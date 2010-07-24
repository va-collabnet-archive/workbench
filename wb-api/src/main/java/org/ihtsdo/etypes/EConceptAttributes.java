package org.ihtsdo.etypes;

import java.io.DataInput;
import java.io.IOException;
import java.util.ArrayList;

import org.dwfa.ace.api.I_ConceptAttributePart;
import org.dwfa.ace.api.I_ConceptAttributeVersioned;
import org.dwfa.ace.api.Terms;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.tk.concept.component.attribute.TkConceptAttributes;
import org.ihtsdo.tk.concept.component.attribute.TkConceptAttributesRevision;

public class EConceptAttributes extends TkConceptAttributes {
    public static final long serialVersionUID = 1;

    public EConceptAttributes() {
        super();
    }

    public EConceptAttributes(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
        super(in, dataVersion);
    }

    public EConceptAttributes(I_ConceptAttributeVersioned conceptAttributes) throws TerminologyException, IOException {
        super();
        EConcept.convertId(Terms.get().getId(conceptAttributes.getNid()), this);
        int partCount = conceptAttributes.getMutableParts().size();
        I_ConceptAttributePart part = conceptAttributes.getMutableParts().get(0);
        defined = part.isDefined();
        pathUuid = nidToUuid(part.getPathId());
        statusUuid = nidToUuid(part.getStatusId());
        time = part.getTime();
        if (partCount > 1) {
            revisions = new ArrayList<TkConceptAttributesRevision>(partCount - 1);
            for (int i = 1; i < partCount; i++) {
                revisions.add(new EConceptAttributesRevision(conceptAttributes.getMutableParts().get(i)));
            }
        }
    }
}