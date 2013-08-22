package org.ihtsdo.workunit.sif;

public class SifComment {
	
	private long time;
	private String comment;
	private SifAuthor author;

	public SifComment(long time, String comment, SifAuthor author) {
		super();
		this.time = time;
		this.comment = comment;
		this.author = author;
	}

	public SifComment() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @return the time
	 */
	public long getTime() {
		return time;
	}

	/**
	 * @param time the time to set
	 */
	public void setTime(long time) {
		this.time = time;
	}

	/**
	 * @return the comment
	 */
	public String getComment() {
		return comment;
	}

	/**
	 * @param comment the comment to set
	 */
	public void setComment(String comment) {
		this.comment = comment;
	}

	/**
	 * @return the author
	 */
	public SifAuthor getAuthor() {
		return author;
	}

	/**
	 * @param author the author to set
	 */
	public void setAuthor(SifAuthor author) {
		this.author = author;
	}

}
