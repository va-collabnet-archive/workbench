/*
 * Copyright (c) 2010 International Health Terminology Standards Development
 * Organisation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ihtsdo.project.conflict;

import java.util.ArrayList;
import java.util.List;

/**
 * The Class ConflictDetector.
 */
public class ConflictDetector {
	
	/**
	 * Detect conflicts.
	 *
	 * @param observedHistory the observed history
	 * @param commitRecords the commit records
	 * @return the list
	 */
	public static List<CommitRecord> detectConflicts(ObservedHistory observedHistory,
			List<CommitRecord> commitRecords) {
		List<CommitRecord> commitRecordsInConflict = new ArrayList<CommitRecord>();
		
		for (CommitRecord loopCommitRecord : commitRecords) {
			
		}
		
		return commitRecordsInConflict;
	}

}
