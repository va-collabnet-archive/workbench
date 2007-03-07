package org.dwfa.clock;

public class ConstantTime implements I_KeepTime {

	long time;
	
	public ConstantTime(long time) {
		super();
		this.time = time;
	}

	public long getTime() {
		return time;
	}

}
