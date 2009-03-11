package org.dwfa.clock;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface I_KeepTime extends Remote {
	public long getTime() throws RemoteException;
}
