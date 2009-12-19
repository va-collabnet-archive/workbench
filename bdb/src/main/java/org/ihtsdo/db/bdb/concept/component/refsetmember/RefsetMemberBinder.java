package org.ihtsdo.db.bdb.concept.component.refsetmember;

import org.ihtsdo.db.bdb.concept.component.ConceptComponentBinder;

public class RefsetMemberBinder {

	private static final ThreadLocal<ConceptComponentBinder<RefsetMember, RefsetMemberMutablePart>> binders = 
		new ThreadLocal<ConceptComponentBinder<RefsetMember, RefsetMemberMutablePart>>() {

		@Override
		protected ConceptComponentBinder<RefsetMember, RefsetMemberMutablePart> initialValue() {
			RefsetMemberFactory factory = new RefsetMemberFactory();
			return new ConceptComponentBinder<RefsetMember, RefsetMemberMutablePart>(
					factory);
		}
	};

	public static ConceptComponentBinder<RefsetMember, RefsetMemberMutablePart> getBinder() {
		return binders.get();
	}

}
