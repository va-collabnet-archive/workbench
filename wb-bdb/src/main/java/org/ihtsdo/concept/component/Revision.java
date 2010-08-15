package org.ihtsdo.concept.component;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.collections.primitives.ArrayIntList;
import org.dwfa.ace.api.I_AmPart;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.api.TimePathId;
import org.dwfa.util.HashFunction;
import org.ihtsdo.db.bdb.Bdb;
import org.ihtsdo.time.TimeUtil;

import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

public abstract class Revision<V extends Revision<V, C>, 
							  C extends ConceptComponent<V, C>> 
	implements I_AmPart, I_HandleFutureStatusAtPositionSetup {
	
	public static SimpleDateFormat fileDateFormat = new SimpleDateFormat("yyyy-MM-dd-HH.mm.ss");

	
	public int sapNid = Integer.MAX_VALUE;
	public C primordialComponent;

	public final List<UUID> getUUIDs() {
		return primordialComponent.getUUIDs();
	}

	public final int getNid() {
		return primordialComponent.getNid();
	}

	public Revision(int statusAtPositionNid, C primordialComponent) {
		super();
		assert primordialComponent != null;
		this.sapNid = statusAtPositionNid;
		this.primordialComponent = primordialComponent;
		primordialComponent.clearVersions();
		assert primordialComponent != null;
		assert statusAtPositionNid != Integer.MAX_VALUE;
	}

	public Revision(int statusNid, int authorNid, int pathNid, long time, C primordialComponent) {
		this.sapNid = Bdb.getSapDb().getSapNid(statusNid, authorNid, pathNid, time);
		this.primordialComponent = primordialComponent;
        primordialComponent.clearVersions();
		assert primordialComponent != null;
		assert sapNid != Integer.MAX_VALUE;
	}
	
	public Revision(TupleInput input, C conceptComponent) {
		this(input.readInt(), conceptComponent);
        conceptComponent.clearVersions();
	}

    public Revision() {
        super();
    }
   
    protected void modified() {
        if (primordialComponent != null) {
            primordialComponent.modified();
        }
    }
	public final void writePartToBdb(TupleOutput output) {
		output.writeInt(sapNid);
		writeFieldsToBdb(output);
	}
	
	/* (non-Javadoc)
	 * @see org.ihtsdo.db.bdb.concept.component.I_HandleDeferredStatusAtPositionSetup#isSetup()
	 */
	public boolean isSetup() {
		return sapNid != Integer.MAX_VALUE;
	}

	/* (non-Javadoc)
	 * @see org.ihtsdo.db.bdb.concept.component.I_HandleDeferredStatusAtPositionSetup#setStatusAtPositionNid(int)
	 */
	public void setStatusAtPositionNid(int sapNid) {
		this.sapNid = sapNid;
        modified();
	}
	
	protected abstract void writeFieldsToBdb(TupleOutput output);
	
	public final C getVersioned() {
		return primordialComponent;
	}

	@Deprecated
	public final Set<TimePathId> getTimePathSet() {
		return primordialComponent.getTimePathSet();
	}
	
	public List<V> getVersions() {
		return primordialComponent.revisions;
	}

	@Override
	public final int hashCode() {
		return HashFunction.hashCode( new int[] { primordialComponent.nid });
	}


	public final int getStatusAtPositionNid() {
		return sapNid;
	}

	public final ArrayIntList getPartComponentNids() {
		ArrayIntList resultList = getVariableVersionNids();
		resultList.add(getPathNid());
		resultList.add(getStatusNid());
		return resultList;
	}
	
	public abstract ArrayIntList getVariableVersionNids();
	
	@Override
	@Deprecated
	public int getPathId() {
		return Bdb.getSapDb().getPathNid(sapNid);
	}
	public int getPathNid() {
		return Bdb.getSapDb().getPathNid(sapNid);
	}

	@Override
	@Deprecated
	public int getStatusId() {
		return Bdb.getSapDb().getStatusNid(sapNid);
	}
	@Override
	public int getStatusNid() {
		return Bdb.getSapDb().getStatusNid(sapNid);
	}

	@Override
	@Deprecated
	public int getVersion() {
		return Bdb.getSapDb().getVersion(sapNid);
	}

	@Override
	public long getTime() {
		return Bdb.getSapDb().getTime(sapNid);
	}
	
	/**
	 * 1. Analog, an object, concept or situation which in some way resembles a different situation
	 * 2. Analogy, in language, a comparison between concepts
	 * @param statusNid
	 * @param pathNid
	 * @param time
	 * @return
	 */
	public abstract V makeAnalog(int statusNid, int pathNid, long time);
	
	public abstract V makeAnalog(int statusNid, int authorNid, int pathNid, long time);

	public void setStatusAtPosition(int statusNid, int authorNid, int pathNid, long time) {
		this.sapNid = Bdb.getSapDb().getSapNid(statusNid, authorNid, pathNid, time);
		modified();
	}


	@Override
	@Deprecated
	public final void setPathId(int pathId) {
		setPathNid(pathId);
	}

	@Override
	public final void setPathNid(int pathId) {
		if (getTime() != Long.MAX_VALUE) {
			throw new UnsupportedOperationException(
					"Cannot change status if time != Long.MAX_VALUE; Use makeAnalog instead.");
		}
			this.sapNid = Bdb.getSapNid(getStatusNid(), 
					Terms.get().getAuthorNid(),
					pathId, Long.MAX_VALUE);
	}

	@Override
	@Deprecated
	public final void setStatusId(int statusNid) {
		setStatusNid(statusNid);
        modified();
	}

	@Override
	public final void setStatusNid(int statusNid) {
		if (getTime() != Long.MAX_VALUE) {
			throw new UnsupportedOperationException(
					"Cannot change status if time != Long.MAX_VALUE; Use makeAnalog instead.");
		}
		try {
			this.sapNid = Bdb.getSapNid(statusNid, 
					Terms.get().getAuthorNid(),
					getPathNid(), Long.MAX_VALUE);
		} catch (Exception e) {
			throw new RuntimeException();
		} 
        modified();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
    public String toString() {
        StringBuffer buf = new StringBuffer();
         buf.append(" sap:");
        buf.append(sapNid);
        buf.append(" status:");
        ConceptComponent.addNidToBuffer(buf, getStatusNid());
        buf.append(" author:");
        ConceptComponent.addNidToBuffer(buf, getAuthorNid());
        buf.append(" path:");
        ConceptComponent.addNidToBuffer(buf, getPathNid());
        buf.append(" tm: ");
        buf.append(TimeUtil.formatDate(getTime()));
        buf.append(" ");
        buf.append(getTime());
         buf.append(" };");
        return buf.toString();
    }

		
	@SuppressWarnings("unchecked")
	@Override
	public boolean equals(Object obj) {
        if (obj == null)
            return false;
		if (Revision.class.isAssignableFrom(obj.getClass())) {
			Revision<V, C> another = (Revision<V, C>) obj;
			if (this.sapNid == another.sapNid) {
				return true;
			}
		}
		return false;
	}

    /**
     * Test method to check to see if two objects are equal in all respects. 
     * @param another
     * @return either a zero length String, or a String containing a description of the
     * validation failures. 
     * @throws IOException 
     */
    public String validate(Revision<?, ?> another) throws IOException {
        assert another != null;
        StringBuffer buf = new StringBuffer();
        
        if (this.sapNid != another.sapNid) {
            buf.append("\t\tRevision.sapNid not equal: \n" + 
                "\t\t\tthis.sapNid = " + this.sapNid + "\n" + 
                "\t\t\tanother.sapNid = " + another.sapNid + "\n");
        }
        if (!this.primordialComponent.equals(another.primordialComponent)) {
            buf.append("\t\tRevision.primordialComponent not equal: \n" + 
                "\t\t\tthis.primordialComponent = " + this.primordialComponent + "\n" + 
                "\t\t\tanother.primordialComponent = " + another.primordialComponent + "\n");
        }
        return buf.toString();
    }
    
	@Override
	public final void setTime(long time) {
		if (getTime() != Long.MAX_VALUE) {
			throw new UnsupportedOperationException(
					"Cannot change status if time != Long.MAX_VALUE; Use makeAnalog instead.");
		}
		if (time != getTime()) {
			try {
				this.sapNid = Bdb.getSapNid(getStatusNid(), 
						Terms.get().getAuthorNid(),
						getPathNid(), time);
			} catch (Exception e) {
				throw new RuntimeException();
			} 
	        modified();
		}
	}
    @Override
	public int getAuthorNid() {
        return Bdb.getSapDb().getAuthorNid(sapNid);
	}

	@Override
	public void setAuthorNid(int authorNid) {
        if (getTime() != Long.MAX_VALUE) {
            throw new UnsupportedOperationException(
                "Cannot change status if time != Long.MAX_VALUE; Use makeAnalog instead.");
        }
        if (authorNid != getPathNid()) {
            this.sapNid = Bdb.getSapNid(getStatusNid(), authorNid, getPathNid(), Long.MAX_VALUE);
            modified();
        }
	}

}
