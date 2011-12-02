package org.ihtsdo.project.conflict;

import java.util.ArrayList;
import java.util.List;

public class ConflictDetector {
	
	public static List<CommitRecord> detectConflicts(ObservedHistory observedHistory,
			List<CommitRecord> commitRecords) {
		List<CommitRecord> commitRecordsInConflict = new ArrayList<CommitRecord>();
		
		for (CommitRecord loopCommitRecord : commitRecords) {
			
		}
		
		return commitRecordsInConflict;
	}

}
