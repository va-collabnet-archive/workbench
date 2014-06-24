/**
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
package org.ihtsdo.issue;

import java.util.UUID;

/**
 * The Class IssueComment.
 */
public class IssueComment {

	/** The UU id. */
	private UUID UUId;
	
	/** The user. */
	private String user;
	
	/** The comment date. */
	private String commentDate;
	
	/** The comment. */
	private String comment;

	/**
	 * Gets the user.
	 * 
	 * @return the user
	 */
	public String getUser() {
		return user;
	}

	/**
	 * Sets the user.
	 * 
	 * @param user the new user
	 */
	public void setUser(String user) {
		this.user = user;
	}

	/**
	 * Gets the comment date.
	 * 
	 * @return the comment date
	 */
	public String getCommentDate() {
		return commentDate;
	}

	/**
	 * Sets the comment date.
	 * 
	 * @param commentDate the new comment date
	 */
	public void setCommentDate(String commentDate) {
		this.commentDate = commentDate;
	}

	/**
	 * Gets the comment.
	 * 
	 * @return the comment
	 */
	public String getComment() {
		return comment;
	}

	/**
	 * Sets the comment.
	 * 
	 * @param comment the new comment
	 */
	public void setComment(String comment) {
		this.comment = comment;
	}

	/**
	 * Gets the uU id.
	 * 
	 * @return the uU id
	 */
	public UUID getUUId() {
		return UUId;
	}

	/**
	 * Sets the uU id.
	 * 
	 * @param id the new uU id
	 */
	public void setUUId(UUID id) {
		UUId = id;
	}
}
