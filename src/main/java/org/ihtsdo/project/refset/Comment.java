package org.ihtsdo.project.refset;

public class Comment implements Comparable<Comment> {
	private int typeCid;
	private int subTypeCid;
	private String comment;
	private Long time;

	public Comment(int typeCid, int subTypeCid, String comment, Long time) {
		super();
		this.typeCid = typeCid;
		this.subTypeCid = subTypeCid;
		this.comment = comment;
		this.time = time;
	}

	public int getTypeCid() {
		return typeCid;
	}

	public void setTypeCid(int typeCid) {
		this.typeCid = typeCid;
	}

	public int getSubTypeCid() {
		return subTypeCid;
	}

	public void setSubTypeCid(int subTypeCid) {
		this.subTypeCid = subTypeCid;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public Long getTime() {
		return time;
	}

	public void setTime(Long time) {
		this.time = time;
	}

	public int compareTo(Comment comment2) {
		return this.getTime().compareTo(comment2.getTime());
	}

}
