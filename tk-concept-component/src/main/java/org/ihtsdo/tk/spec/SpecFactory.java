package org.ihtsdo.tk.spec;

import java.io.IOException;
import java.util.UUID;

import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.ContraditionException;
import org.ihtsdo.tk.api.concept.ConceptChronicleBI;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.api.description.DescriptionChronicleBI;
import org.ihtsdo.tk.api.description.DescriptionVersionBI;

public class SpecFactory {

   public static ConceptSpec get(ConceptChronicleBI concept, ViewCoordinate vc) throws IOException {
         ConceptVersionBI cv = Ts.get().getConceptVersion(vc, concept.getNid());
         try {
            return new ConceptSpec(cv.getDescsActive().iterator().next().getText(), 
                    concept.getPrimUuid());
         } catch (ContraditionException ex) {
             for (DescriptionChronicleBI desc: concept.getDescs()) {
                for (DescriptionVersionBI dv: desc.getVersions(vc)) {
                   return new ConceptSpec(dv.getText(), 
                    concept.getPrimUuid());
                }
             }
             throw new IOException("No current description for: " + cv);
         }
	}

	public static ConceptSpec get(ConceptVersionBI concept) throws IOException {
         try {
            return new ConceptSpec(concept.getDescsActive().iterator().next().getText(), 
                    concept.getPrimUuid());
         } catch (ContraditionException ex) {
             return new ConceptSpec(
                     concept.getDescs().iterator().next().getVersions().iterator().next().getText(), 
                     concept.getPrimUuid());
         }
	}
	
	public static DescriptionSpec get(DescriptionVersionBI desc, ViewCoordinate vc) throws IOException {
		DescriptionSpec ds = new DescriptionSpec(desc.getUUIDs().toArray(new UUID[]{}), 
				get(Ts.get().getConcept(desc.getConceptNid()), vc), 
				get(Ts.get().getConcept(desc.getTypeNid()), vc), 
				desc.getText());
      ds.setLangText(desc.getText());
      return ds;
	}

}
