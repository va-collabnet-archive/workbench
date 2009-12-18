package org.ihtsdo.db.bdb.concept.component;

import org.apache.commons.collections.primitives.ArrayIntList;
import org.dwfa.ace.api.I_AmPart;
import org.dwfa.ace.api.I_AmTuple;
import org.dwfa.util.HashFunction;

public class Tuple<V extends Version<V>, C extends ConceptComponent<V>> 
	implements I_AmTuple {

	private C component;
	private V version;
	
	protected Tuple(C component, V version) {
		super();
		this.component = component;
		this.version = version;
	}

	public final C getVersioned() {
		return component;
	}

	@Override
	public final C getFixedPart() {
		return component;
	}

	@Override
	public final int getFixedPartId() {
		return component.getNid();
	}

	@Override
	public final V getPart() {
		return version;
	}

	@Override
	public final V duplicate() {
		return version.duplicate();
	}

	@Override
	public final ArrayIntList getPartComponentNids() {
		return version.getPartComponentNids();
	}

	@Override
	public final int getPathId() {
		return version.getPathId();
	}

	@Override
	public final int getStatusId() {
		return version.getStatusId();
	}

	@Override
	public final long getTime() {
		return version.getTime();
	}

	@Override
	public final int getVersion() {
		return version.getVersion();
	}

	@Override
	public final I_AmPart makeAnalog(int statusNid, int pathNid, long time) {
		return version.makeAnalog(statusNid, pathNid, time);
	}

	@Override
	public final void setPathId(int pathId) {
		throw new UnsupportedOperationException();
	}

	@Override
	public final void setStatusId(int statusId) {
		throw new UnsupportedOperationException();
	}

	@Override
	public final void setVersion(int version) {
		throw new UnsupportedOperationException();
	}

	@Override
	public final boolean equals(Object obj) {
		if (Tuple.class.isAssignableFrom(obj.getClass())) {
			Tuple<?,?> another = (Tuple<?,?>) obj;
			return component.getNid() == another.component.getNid() && 
			 this.getPart().equals(another.getPart());
		}
		return false;
	}

	@Override
	public final int hashCode() {
		return HashFunction.hashCode( new int[] { component.nid });
	}

}
