package org.ihtsdo.qa.store.model;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

public class QaCaseComment {
	private UUID commentUuid;
	private Date effectiveTime;
	private Integer status;
	private String comment;
	private String author;
	private UUID caseUuid;

	public UUID getCaseUuid() {
		return caseUuid;
	}

	public void setCaseUuid(UUID caseUuid) {
		this.caseUuid = caseUuid;
	}

	public UUID getCommentUuid() {
		return commentUuid;
	}

	public void setCommentUuid(UUID commentUuid) {
		this.commentUuid = commentUuid;
	}

	public Date getEffectiveTime() {
		return effectiveTime;
	}

	public void setEffectiveTime(Date effectiveTime) {
		this.effectiveTime = effectiveTime;
	}

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}
	
	@Override
	public String toString() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm");
		String et = effectiveTime == null ? "  " : sdf.format(effectiveTime);
		return "<html>["+author+"]" + " <B>" + comment + "</B> <i>  - " + et + "</i></html>";
	}
}