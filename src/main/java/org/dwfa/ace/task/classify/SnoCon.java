package org.dwfa.ace.task.classify;

public class SnoCon implements Comparable<Object> {
	public int id;
	public boolean isDefined;

	public SnoCon(int id, boolean isDefined) {
		this.id = id;
		this.isDefined = isDefined;
	}

	public int compareTo(Object o) {
		SnoCon other = (SnoCon) o;
		if (this.id > other.id) {
			return 1; // this is greater than received
		} else if (this.id < other.id) {
			return -1; // this is less than received
		} else {
			return 0; // this == received
		}
	}

}
