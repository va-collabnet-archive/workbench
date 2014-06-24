package org.ihtsdo.rf2.fileqa.model;

public class Header {

	private boolean present;
	private int count;
	private String data;
	private int size;

	public void init() {
		this.present = false;
		this.count = 0;
		this.data = null;
		this.size = 0;
	}

	public boolean isPresent() {
		return present;
	}

	public int getCount() {
		return count;
	}

	public String getData() {
		return data;
	}

	public int getSize() {
		return size;
	}

	public void setPresent(boolean present) {
		this.present = present;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public void setData(String data) {
		this.data = data;
	}

	public void setSize(int size) {
		this.size = size;
	}
}
