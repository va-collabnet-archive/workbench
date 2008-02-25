package org.dwfa.mojo.refset;

public class ConflictPair<T> {
	private T o1;
	private T o2;
	public T getO1() {
		return o1;
	}
	public void setO1(T o1) {
		this.o1 = o1;
	}
	public T getO2() {
		return o2;
	}
	public void setO2(T o2) {
		this.o2 = o2;
	}
	public ConflictPair(T o1, T o2) {
		super();
		this.o1 = o1;
		this.o2 = o2;
	}
	
	
}
