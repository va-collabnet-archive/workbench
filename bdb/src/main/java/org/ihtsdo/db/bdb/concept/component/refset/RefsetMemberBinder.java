package org.ihtsdo.db.bdb.concept.component.refset;

import org.ihtsdo.db.bdb.concept.component.ConceptComponentBinder;

public class RefsetMemberBinder {

	private static final ThreadLocal<ConceptComponentBinder<RefsetVersion, RefsetMember>> binders = 
		new ThreadLocal<ConceptComponentBinder<RefsetVersion, RefsetMember>>() {

		@Override
		protected ConceptComponentBinder<RefsetVersion, RefsetMember> initialValue() {
			RefsetMemberFactory factory = new RefsetMemberFactory();
			return new ConceptComponentBinder<RefsetVersion, RefsetMember>(
					factory);
		}
	};

	public static ConceptComponentBinder<RefsetVersion, RefsetMember> getBinder() {
		return binders.get();
	}

}
