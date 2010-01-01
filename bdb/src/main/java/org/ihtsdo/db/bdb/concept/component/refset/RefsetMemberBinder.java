package org.ihtsdo.db.bdb.concept.component.refset;

import org.ihtsdo.db.bdb.concept.component.ConceptComponentBinder;

public class RefsetMemberBinder {

	private static final ThreadLocal<ConceptComponentBinder<AbstractRefsetMember, RefsetMemberMutablePart>> binders = 
		new ThreadLocal<ConceptComponentBinder<AbstractRefsetMember, RefsetMemberMutablePart>>() {

		@Override
		protected ConceptComponentBinder<AbstractRefsetMember, RefsetMemberMutablePart> initialValue() {
			RefsetMemberFactory factory = new RefsetMemberFactory();
			return new ConceptComponentBinder<AbstractRefsetMember, RefsetMemberMutablePart>(
					factory);
		}
	};

	public static ConceptComponentBinder<AbstractRefsetMember, RefsetMemberMutablePart> getBinder() {
		return binders.get();
	}

}
