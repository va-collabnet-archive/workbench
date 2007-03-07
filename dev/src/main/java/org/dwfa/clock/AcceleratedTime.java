package org.dwfa.clock;

public class AcceleratedTime implements I_KeepTime {

	private long startTime = System.currentTimeMillis();
	private int speed;
	private long timeZero;
	
	public AcceleratedTime(int speed, long timeZero) {
		this.speed = speed;
		this.timeZero = timeZero;
	}
	public AcceleratedTime(int speed) {
		this(speed, System.currentTimeMillis());
	}
	
	public long getTime() {
		long elapsedTime = System.currentTimeMillis() - startTime;
		return timeZero + (elapsedTime * speed);
	}

}
