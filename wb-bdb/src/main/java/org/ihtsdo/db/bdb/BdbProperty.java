/**
 * 
 */
package org.ihtsdo.db.bdb;

public enum BdbProperty {
	LAST_CHANGE_SET_WRITTEN, LAST_CHANGE_SET_READ;
	
	public String toString() {
		return this.getClass().getCanonicalName() + "." + name();
	};
}