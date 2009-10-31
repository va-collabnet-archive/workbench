package org.dwfa.ace.api;

import java.io.IOException;
import java.util.Set;

import org.dwfa.tapi.TerminologyException;

public interface I_AmTermComponent {
	
	/**
	 * @deprecated use getNid();
	 * @return The terminology component's identifier
	 */
	public int getTermComponentId();
	
	/**
	 * @return The terminology component's identifier
	 */
	public int getNid();
	
	/**
	 * 
	 * @param viewPosition The position items should be promoted from. 
	 * @param pomotionPaths The path to promote items to. 
	 * @param allowedStatus Only promote items that have one of these status values. 
	 * @return true if there are any promotions that require commitment. 
	 * @throws IOException 
	 * @throws TerminologyException 
	 */
	public boolean promote(I_Position viewPosition, Set<I_Path> pomotionPaths, I_IntSet allowedStatus) 
		throws IOException, TerminologyException;

}
