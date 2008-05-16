package org.dwfa.ace.utypes;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class UniversalAcePosition implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private Collection<UUID> pathId;

	private long time;

	public UniversalAcePosition() {
		super();
	}

	// START: ADDED TO IMPLEMENT JAVABEANS SPEC
	/**
	 * DO NOT USE THIS METHOD.
	 * 
	 * This method has been included to meet the JavaBeans specification,
	 * however it should not be used as it allows access to attributes that
	 * should not be modifiable and weakens the interface. The method has been
	 * added as a convenience to allow JavaBeans tools access via introspection
	 * but is not intended for general use by developers.
	 * 
	 * @deprecated
	 */
    public void setPathId(Collection<UUID> pathId) {
		this.pathId = pathId;
	}
	// END: ADDED TO IMPLEMENT JAVABEANS SPEC

    public UniversalAcePosition(Collection<UUID> pathId, long time) {
      super();
      this.pathId = pathId;
      this.time = time;
    }

	public Collection<UUID> getPathId() {
		return pathId;
	}

	public void setPathId(List<UUID> pathId) {
		this.pathId = pathId;
	}

	public long getTime() {
		return time;
	}

	public void setTime(long version) {
		this.time = version;
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName() + " id: "  + pathId + " time: " + time + " (" + new Date(time) + ")";
	}

}
