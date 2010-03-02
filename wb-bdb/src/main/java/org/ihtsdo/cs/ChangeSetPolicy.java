package org.ihtsdo.cs;

public enum ChangeSetPolicy {
	/**
	 * Only include changes that represent the sapNids from the current commit. 
	 */
	INCREMENTAL, 
	/**
	 * Only include sapNids that are written to the mutable database. 
	 */
	MUTABLE_ONLY, 
	/**
	 * Include all changes. 
	 */
	COMPREHENSIVE;
}
