package org.ihtsdo.workunit.sif;

public class SifChange {
	
	private String comment;
	private SifAuthor author;
	private SifConcept before;
	private SifConcept after;
	
	public SifChange() {
		// TODO Auto-generated constructor stub
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

	/**
	 * @return the before
	 */
	public SifConcept getBefore() {
		return before;
	}

	/**
	 * @param before the before to set
	 */
	public void setBefore(SifConcept before) {
		this.before = before;
	}

	/**
	 * @return the after
	 */
	public SifConcept getAfter() {
		return after;
	}

	/**
	 * @param after the after to set
	 */
	public void setAfter(SifConcept after) {
		this.after = after;
	}

}
