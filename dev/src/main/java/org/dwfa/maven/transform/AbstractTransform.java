package org.dwfa.maven.transform;

import org.dwfa.maven.I_ReadAndTransform;
import org.dwfa.maven.Transform;

public abstract class AbstractTransform implements I_ReadAndTransform {
	
	private String name;
	
	private String lastTransform;
	
	private int columnId = -1;
	
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
	public int getColumnId() {
		return columnId;
	}
	public void setColumnId(int columnId) {
		this.columnId = columnId;
	}
	
	public final void setup(Transform transformer) {
		if (columnId == -1) {
			columnId = transformer.getNextColumnId();
		}
		setupImpl(transformer);
	}
	public abstract void setupImpl(Transform transformer);


}
