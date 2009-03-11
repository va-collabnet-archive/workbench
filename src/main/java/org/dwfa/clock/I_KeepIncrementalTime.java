package org.dwfa.clock;

import java.rmi.RemoteException;

public interface I_KeepIncrementalTime extends I_KeepTime {

	public void increment() throws RemoteException;

	public void reset() throws RemoteException;

}