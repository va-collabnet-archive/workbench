package org.ihtsdo.tk.api.constraint;

import org.ihtsdo.tk.spec.ConceptSpec;

public class RelConstraintOutgoing extends RelConstraint {

	public RelConstraintOutgoing(ConceptSpec originSpec,
			ConceptSpec relTypeSpec, ConceptSpec destinationSpec) {
		super(originSpec, relTypeSpec, destinationSpec);
	}

}
