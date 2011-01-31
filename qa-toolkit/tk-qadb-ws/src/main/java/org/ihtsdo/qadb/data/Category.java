package org.ihtsdo.qadb.data;

import java.io.Serializable;


public class Category implements Serializable{
	
	private static final long serialVersionUID = -5297000140817939202L;
	private String categoryUuid;
	private int status;
	private String name;
	private String description;
	private String author;
	
	public Category(){
		super();
	}

	public String getCategoryUuid() {
		return categoryUuid;
	}

	public void setCategoryUuid(String categoryUuid) {
		this.categoryUuid = categoryUuid;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}
	
}
