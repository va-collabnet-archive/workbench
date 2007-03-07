package org.dwfa.clock;

public class SystemTime implements I_KeepTime {

	public long getTime() {
		return System.currentTimeMillis();
	}

}
