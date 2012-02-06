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

import java.util.Set;

/**
 * The Class CommitRecord.
 */
public class CommitRecord {
	
	/** The time. */
	private Long time;
	
	/** The visible records. */
	private Set<ChangeRecord> visibleRecords;
	
	/**
	 * Instantiates a new commit record.
	 *
	 * @param time the time
	 * @param visibleRecords the visible records
	 */
	public CommitRecord(Long time, Set<ChangeRecord> visibleRecords) {
		super();
		this.time = time;
		this.visibleRecords = visibleRecords;
	}

	/**
	 * Gets the time.
	 *
	 * @return the time
	 */
	public Long getTime() {
		return time;
	}

	/**
	 * Sets the time.
	 *
	 * @param time the new time
	 */
	public void setTime(Long time) {
		this.time = time;
	}

	/**
	 * Gets the visible records.
	 *
	 * @return the visible records
	 */
	public Set<ChangeRecord> getVisibleRecords() {
		return visibleRecords;
	}

	/**
	 * Sets the visible records.
	 *
	 * @param visibleRecords the new visible records
	 */
	public void setVisibleRecords(Set<ChangeRecord> visibleRecords) {
		this.visibleRecords = visibleRecords;
	}
	

}
