package org.ihtsdo.tk.spec;

import java.io.IOException;
import java.util.UUID;

import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.ContradictionException;
import org.ihtsdo.tk.api.concept.ConceptChronicleBI;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.api.description.DescriptionChronicleBI;
import org.ihtsdo.tk.api.description.DescriptionVersionBI;

public class SpecFactory {

    public static ConceptSpec get(ConceptChronicleBI conceptChronicle, ViewCoordinate viewCoordinate) throws IOException {
        ConceptVersionBI cv = Ts.get().getConceptVersion(viewCoordinate, conceptChronicle.getNid());
        try {
            return new ConceptSpec(cv.getDescriptionsActive().iterator().next().getText(),
                    conceptChronicle.getPrimUuid());
        } catch (ContradictionException ex) {
            for (DescriptionChronicleBI desc : conceptChronicle.getDescriptions()) {
                for (DescriptionVersionBI dv : desc.getVersions(viewCoordinate)) {
                    return new ConceptSpec(dv.getText(),
                            conceptChronicle.getPrimUuid());
                }
            }
            throw new IOException("No current description for: " + cv);
        }
    }

    public static ConceptSpec get(ConceptVersionBI conceptVersion) throws IOException {
        try {
            return new ConceptSpec(conceptVersion.getDescriptionsActive().iterator().next().getText(),
                    conceptVersion.getPrimUuid());
        } catch (ContradictionException ex) {
            return new ConceptSpec(
                    conceptVersion.getDescriptions().iterator().next().getVersions().iterator().next().getText(),
                    conceptVersion.getPrimUuid());
        }
    }

    public static DescriptionSpec get(DescriptionVersionBI descriptionVersion, ViewCoordinate viewCoordinate) throws IOException {
        if (descriptionVersion != null && descriptionVersion.getUUIDs() != null) {
            DescriptionSpec ds = new DescriptionSpec(descriptionVersion.getUUIDs().toArray(new UUID[]{}),
                    get(Ts.get().getConcept(descriptionVersion.getConceptNid()), viewCoordinate),
                    get(Ts.get().getConcept(descriptionVersion.getTypeNid()), viewCoordinate),
                    descriptionVersion.getText());
            ds.setLangText(descriptionVersion.getText());
            return ds;
        } else {
            return null;
        }
    }
}
