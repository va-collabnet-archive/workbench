package org.ihtsdo.db.bdb.concept.component.identifier;

import org.ihtsdo.db.bdb.concept.component.ConceptComponentBinder;

public class IdentifierBinder {

	private static final ThreadLocal<ConceptComponentBinder<Identifier, IdentifierMutablePart>> binders = new ThreadLocal<ConceptComponentBinder<Identifier, IdentifierMutablePart>>() {

		@Override
		protected ConceptComponentBinder<Identifier, IdentifierMutablePart> initialValue() {
			IdentifierFactory factory = new IdentifierFactory();
			return new ConceptComponentBinder<Identifier, IdentifierMutablePart>(
					factory);
		}
	};

	public static ConceptComponentBinder<Identifier, IdentifierMutablePart> getBinder() {
		return binders.get();
	}

}
