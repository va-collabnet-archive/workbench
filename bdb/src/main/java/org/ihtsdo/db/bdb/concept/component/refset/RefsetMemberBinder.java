package org.ihtsdo.db.bdb.concept.component.refset;

import org.ihtsdo.db.bdb.concept.component.ConceptComponentBinder;

public class RefsetMemberBinder<V extends RefsetVersion<V, C>, C extends RefsetMember<V, C>> {

	private ThreadLocal<ConceptComponentBinder<V, C>> 
	binders = 
		new ThreadLocal<ConceptComponentBinder<V, C>>() {

		@SuppressWarnings("unchecked")
		@Override
		protected ConceptComponentBinder<V, C> initialValue() {
			RefsetMemberFactory factory = new RefsetMemberFactory();
			return new ConceptComponentBinder<V, C>(
					factory);
		}
	};

	public ConceptComponentBinder<V, C> getBinder() {
		return binders.get();
	}

}
