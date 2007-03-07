package org.dwfa.clock;

import java.rmi.RemoteException;

public class IncrementalTime implements I_KeepIncrementalTime {

	private int increment;
	private int count = 0;
	private I_KeepTime base;
	
	public IncrementalTime(int increment, I_KeepTime base) {
		this.increment = increment;
		this.base = base;
	}
	public IncrementalTime(int increment) {
		this(increment, new ConstantTime(System.currentTimeMillis()));
	}
	
	public long getTime() throws RemoteException {
		long elapsedTime = increment * count;
		return base.getTime() + elapsedTime;
	}
	
	/* (non-Javadoc)
	 * @see org.dwfa.clock.I_KeepIncrementalTime#increment()
	 */
	public void increment() {
		count++;
	}

	/* (non-Javadoc)
	 * @see org.dwfa.clock.I_KeepIncrementalTime#reset()
	 */
	public void reset() {
		count = 0;
	}
}
