package org.ihtsdo.tk.spec;

import java.io.IOException;
import java.util.UUID;

import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.concept.ConceptChronicleBI;
import org.ihtsdo.tk.api.description.DescriptionVersionBI;

public class SpecFactory {

	public static ConceptSpec get(ConceptChronicleBI concept) {
		return new ConceptSpec(concept.toString(), concept.getPrimUuid());
	}
	
	public static DescriptionSpec get(DescriptionVersionBI desc) throws IOException {
		
		return new DescriptionSpec(desc.getUUIDs().toArray(new UUID[]{}), 
				get(Ts.get().getConcept(desc.getConceptNid())), 
				get(Ts.get().getConcept(desc.getTypeNid())), 
				desc.getText());
	}

}
