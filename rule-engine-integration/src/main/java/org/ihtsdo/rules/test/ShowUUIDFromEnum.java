package org.ihtsdo.rules.test;

import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.util.id.Type3UuidFactory;

public class ShowUUIDFromEnum {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		System.out.println("PreCommit: " + 
				Type3UuidFactory.fromEnum(RefsetAuxiliary.Concept.REALTIME_PRECOMMIT_QA_CONTEXT));
		System.out.println("Realtime: " + 
				Type3UuidFactory.fromEnum(RefsetAuxiliary.Concept.REALTIME_QA_CONTEXT));
		System.out.println("Batch: " + 
				Type3UuidFactory.fromEnum(RefsetAuxiliary.Concept.BATCH_QA_CONTEXT));

	}

}
