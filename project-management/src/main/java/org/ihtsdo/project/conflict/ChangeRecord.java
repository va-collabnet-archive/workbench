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

/**
 * The Class ChangeRecord.
 */
public class ChangeRecord {
	
	/** The time. */
	private Long time;
	
	/** The author. */
	private Integer author;
	
	/**
	 * Instantiates a new change record.
	 *
	 * @param time the time
	 * @param author the author
	 */
	public ChangeRecord(Long time, Integer author) {
		super();
		this.time = time;
		this.author = author;
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
	 * Gets the author.
	 *
	 * @return the author
	 */
	public Integer getAuthor() {
		return author;
	}

	/**
	 * Sets the author.
	 *
	 * @param author the new author
	 */
	public void setAuthor(Integer author) {
		this.author = author;
	}

}
