package org.ihtsdo.etypes;

import java.io.DataInput;
import java.io.IOException;
import org.dwfa.ace.api.I_ConceptAttributeVersioned;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.tk.dto.concept.component.attribute.TkConceptAttributes;

public class EConceptAttributes extends TkConceptAttributes {
	public static final long serialVersionUID = 1;

	public EConceptAttributes() {
		super();
	}

	public EConceptAttributes(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
		super(in, dataVersion);
	}

	public EConceptAttributes(I_ConceptAttributeVersioned<?> conceptAttributes) throws TerminologyException, IOException {
		super(conceptAttributes);
	}

}