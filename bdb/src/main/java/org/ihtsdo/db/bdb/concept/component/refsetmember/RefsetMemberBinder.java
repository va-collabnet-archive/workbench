package org.ihtsdo.db.bdb.concept.component.refsetmember;

import org.ihtsdo.db.bdb.concept.component.ConceptComponentBinder;

public class RefsetMemberBinder {

	private static final ThreadLocal<ConceptComponentBinder<RefsetMember, RefsetMemberPart>> binders = 
		new ThreadLocal<ConceptComponentBinder<RefsetMember, RefsetMemberPart>>() {

		@Override
		protected ConceptComponentBinder<RefsetMember, RefsetMemberPart> initialValue() {
			RefsetMemberFactory factory = new RefsetMemberFactory();
			return new ConceptComponentBinder<RefsetMember, RefsetMemberPart>(
					factory);
		}
	};

	public static ConceptComponentBinder<RefsetMember, RefsetMemberPart> getBinder() {
		return binders.get();
	}

}
