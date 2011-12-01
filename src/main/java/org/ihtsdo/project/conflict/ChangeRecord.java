package org.ihtsdo.project.conflict;

public class ChangeRecord {
	private Long time;
	private Integer author;
	
	public ChangeRecord(Long time, Integer author) {
		super();
		this.time = time;
		this.author = author;
	}

	public Long getTime() {
		return time;
	}

	public void setTime(Long time) {
		this.time = time;
	}

	public Integer getAuthor() {
		return author;
	}

	public void setAuthor(Integer author) {
		this.author = author;
	}

}
