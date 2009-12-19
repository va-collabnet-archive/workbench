package org.ihtsdo.db.bdb.concept.component.identifier;

import org.ihtsdo.db.bdb.concept.component.ConceptComponentBinder;

public class IdentifierBinder {

	private static final ThreadLocal<ConceptComponentBinder<Identifier, IdentifierVariablePart>> binders = new ThreadLocal<ConceptComponentBinder<Identifier, IdentifierVariablePart>>() {

		@Override
		protected ConceptComponentBinder<Identifier, IdentifierVariablePart> initialValue() {
			IdentifierFactory factory = new IdentifierFactory();
			return new ConceptComponentBinder<Identifier, IdentifierVariablePart>(
					factory);
		}
	};

	public static ConceptComponentBinder<Identifier, IdentifierVariablePart> getBinder() {
		return binders.get();
	}

}
