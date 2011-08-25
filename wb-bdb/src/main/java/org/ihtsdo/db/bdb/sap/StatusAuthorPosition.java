/**
 * 
 */
package org.ihtsdo.db.bdb.sap;

import org.dwfa.util.HashFunction;

public class StatusAuthorPosition implements Comparable<StatusAuthorPosition> {
	
	private int statusNid;
	
	private int authorNid;

	private int pathNid;
	
	private long time;
		
	StatusAuthorPosition(int statusNid, int authorNid, int pathNid, long time) {
		super();
		this.statusNid = statusNid;
		this.authorNid = authorNid;
		this.pathNid = pathNid;
		this.time = time;
		assert time != 0;
		assert statusNid != 0;
		assert pathNid != 0;
	}
		
	@Override
	public int compareTo(StatusAuthorPosition o) {
		if (this.time > o.time) {
			return 1;
		}
		
		if (this.time < o.time) {
			return -1;
		}
		
		if (this.statusNid != o.statusNid) {
			return this.statusNid - o.statusNid;
		}
		
		if (this.authorNid != o.authorNid) {
			return this.authorNid - o.authorNid;
		}
		
		return this.pathNid - o.pathNid;
	}

	public long getTime() {
		return time;
	}

	public int getStatusNid() {
		return statusNid;
	}

	public int getPathNid() {
		return pathNid;
	}
	
	
	public int getAuthorNid() {
		return authorNid;
	}

	public void setAuthorNid(int authorNid) {
		this.authorNid = authorNid;
	}

	@Override
	public boolean equals(Object obj) {
		if (StatusAuthorPosition.class.isAssignableFrom(obj.getClass())) {
			return compareTo((StatusAuthorPosition) obj) == 0;
		}
		return false;
	}

	@Override
	public int hashCode() {
		return HashFunction.hashCode(new int[] {authorNid, statusNid, pathNid, (int) time});
	}
	
	

}