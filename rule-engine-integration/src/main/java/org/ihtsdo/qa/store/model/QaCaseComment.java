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
package org.ihtsdo.qa.store.model;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

/**
 * The Class QaCaseComment.
 */
public class QaCaseComment {
	
	/** The comment uuid. */
	private UUID commentUuid;
	
	/** The effective time. */
	private Date effectiveTime;
	
	/** The status. */
	private Integer status;
	
	/** The comment. */
	private String comment;
	
	/** The author. */
	private String author;
	
	/** The case uuid. */
	private UUID caseUuid;

	/**
	 * Gets the case uuid.
	 *
	 * @return the case uuid
	 */
	public UUID getCaseUuid() {
		return caseUuid;
	}

	/**
	 * Sets the case uuid.
	 *
	 * @param caseUuid the new case uuid
	 */
	public void setCaseUuid(UUID caseUuid) {
		this.caseUuid = caseUuid;
	}

	/**
	 * Gets the comment uuid.
	 *
	 * @return the comment uuid
	 */
	public UUID getCommentUuid() {
		return commentUuid;
	}

	/**
	 * Sets the comment uuid.
	 *
	 * @param commentUuid the new comment uuid
	 */
	public void setCommentUuid(UUID commentUuid) {
		this.commentUuid = commentUuid;
	}

	/**
	 * Gets the effective time.
	 *
	 * @return the effective time
	 */
	public Date getEffectiveTime() {
		return effectiveTime;
	}

	/**
	 * Sets the effective time.
	 *
	 * @param effectiveTime the new effective time
	 */
	public void setEffectiveTime(Date effectiveTime) {
		this.effectiveTime = effectiveTime;
	}

	/**
	 * Gets the status.
	 *
	 * @return the status
	 */
	public Integer getStatus() {
		return status;
	}

	/**
	 * Sets the status.
	 *
	 * @param status the new status
	 */
	public void setStatus(Integer status) {
		this.status = status;
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
	 * Gets the author.
	 *
	 * @return the author
	 */
	public String getAuthor() {
		return author;
	}

	/**
	 * Sets the author.
	 *
	 * @param author the new author
	 */
	public void setAuthor(String author) {
		this.author = author;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm");
		String et = effectiveTime == null ? "  " : sdf.format(effectiveTime);
		return "<html>["+author+"]" + " <B>" + comment + "</B> <i>  - " + et + "</i></html>";
	}
}
