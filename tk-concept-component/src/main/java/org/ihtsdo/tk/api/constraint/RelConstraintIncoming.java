package org.ihtsdo.tk.api.constraint;

import org.ihtsdo.tk.spec.ConceptSpec;

public class RelConstraintIncoming extends RelConstraint {

	public RelConstraintIncoming(ConceptSpec originSpec,
			ConceptSpec relTypeSpec, ConceptSpec destinationSpec) {
		super(originSpec, relTypeSpec, destinationSpec);
	}
}
