package org.dwfa.maven.transform;

import org.dwfa.maven.I_ReadAndTransform;

public abstract class AbstractTransform implements I_ReadAndTransform {
	private String name;
	private String lastTransform;
	public String toString() {
		return getClass().getSimpleName() + ": " + name;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getLastTransform() {
		return lastTransform;
	}
	protected String setLastTransform(String lastTransfrom) {
		this.lastTransform = lastTransfrom;
		return lastTransfrom;
	}
}
