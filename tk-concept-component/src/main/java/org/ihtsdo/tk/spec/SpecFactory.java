package org.ihtsdo.tk.spec;

import java.io.IOException;
import java.util.UUID;

import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.ContraditionException;
import org.ihtsdo.tk.api.concept.ConceptChronicleBI;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;
import org.ihtsdo.tk.api.description.DescriptionVersionBI;

public class SpecFactory {

	public static ConceptSpec get(ConceptChronicleBI concept) throws IOException {
      if (ConceptVersionBI.class.isAssignableFrom(concept.getClass())) {
         ConceptVersionBI cv = (ConceptVersionBI) concept;
         try {
            return new ConceptSpec(cv.getDescsActive().iterator().next().getText(), 
                    concept.getPrimUuid());
         } catch (ContraditionException ex) {
             return new ConceptSpec(
                     concept.getDescs().iterator().next().getVersions().iterator().next().getText(), 
                     concept.getPrimUuid());
         }
      }
		return new ConceptSpec(
              concept.getDescs().iterator().next().getVersions().iterator().next().getText(), 
              concept.getPrimUuid());
	}
	
	public static DescriptionSpec get(DescriptionVersionBI desc) throws IOException {
		DescriptionSpec ds = new DescriptionSpec(desc.getUUIDs().toArray(new UUID[]{}), 
				get(Ts.get().getConcept(desc.getConceptNid())), 
				get(Ts.get().getConcept(desc.getTypeNid())), 
				desc.getText());
      ds.setLangText(desc.getText());
      return ds;
	}

}
