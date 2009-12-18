package org.ihtsdo.db.bdb.concept.component.refsetmember;

import org.ihtsdo.db.bdb.concept.component.ConceptComponentBinder;

public class RefsetMemberBinder {

	private static final ThreadLocal<ConceptComponentBinder<RefsetMember, RefsetMemberVariablePart>> binders = 
		new ThreadLocal<ConceptComponentBinder<RefsetMember, RefsetMemberVariablePart>>() {

		@Override
		protected ConceptComponentBinder<RefsetMember, RefsetMemberVariablePart> initialValue() {
			RefsetMemberFactory factory = new RefsetMemberFactory();
			return new ConceptComponentBinder<RefsetMember, RefsetMemberVariablePart>(
					factory);
		}
	};

	public static ConceptComponentBinder<RefsetMember, RefsetMemberVariablePart> getBinder() {
		return binders.get();
	}

}
