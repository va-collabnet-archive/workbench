package org.ihtsdo.qadb.data;

import java.io.Serializable;
import java.util.Calendar;

public class QAComment implements Serializable {

	private static final long serialVersionUID = -320461674331212407L;
	private String commentUuid;
	private Calendar effectiveTime;
	private Integer status;
	private String comment;
	private String caseUuid;
	private String author;
	
	public String getCaseUuid() {
		return caseUuid;
	}

	public void setCaseUuid(String caseUuid) {
		this.caseUuid = caseUuid;
	}

	public Calendar getEffectiveTime() {
		return effectiveTime;
	}

	public void setEffectiveTime(Calendar effectiveTime) {
		this.effectiveTime = effectiveTime;
	}

	public String getCommentUuid() {
		return commentUuid;
	}

	public void setCommentUuid(String commentUuid) {
		this.commentUuid = commentUuid;
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

}
