package org.ihtsdo.tk.api.constraint;

import java.io.IOException;

import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.Coordinate;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;
import org.ihtsdo.tk.spec.ConceptSpec;

public class DescriptionConstraint implements ConstraintBI {

    private ConceptSpec conceptSpec;
	private ConceptSpec descTypeSpec;
    private String text;
    
	public DescriptionConstraint(ConceptSpec conceptSpec,
			ConceptSpec descTypeSpec, String text) {
		super();
		this.conceptSpec = conceptSpec;
		this.descTypeSpec = descTypeSpec;
		this.text = text;
	}

	public ConceptSpec getConceptSpec() {
		return conceptSpec;
	}

	public ConceptSpec getDescTypeSpec() {
		return descTypeSpec;
	}

	public String getText() {
		return text;
	}

	public ConceptVersionBI getConcept(Coordinate c) throws IOException {
		return conceptSpec.get(c);
	}

	public ConceptVersionBI getDescType(Coordinate c) throws IOException {
		return descTypeSpec.get(c);
	}


	public int getConceptNid() throws IOException {
		return Ts.get().uuidsToNid(conceptSpec.getUuids());
	}

	public int getDescTypeNid() throws IOException {
		return Ts.get().uuidsToNid(descTypeSpec.getUuids());
	}

    

}
