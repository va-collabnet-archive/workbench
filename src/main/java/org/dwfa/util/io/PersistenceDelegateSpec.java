package org.dwfa.util.io;

import java.beans.PersistenceDelegate;

public class PersistenceDelegateSpec {
	private Class<?> type;
    private PersistenceDelegate persistenceDelegate;
	public PersistenceDelegateSpec(Class<?> type, PersistenceDelegate persistenceDelegate) {
		super();
		this.type = type;
		this.persistenceDelegate = persistenceDelegate;
	}
	public PersistenceDelegate getPersistenceDelegate() {
		return persistenceDelegate;
	}
	public Class<?> getType() {
		return type;
	}
}
