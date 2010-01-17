package org.ihtsdo.db.uuidmap;

public interface IntUuidProxyIntProcedure {

	/**
	 * 
	 * @param uuid
	 * @param second
	 * @return 	iteration will stop if <tt>false</tt>, otherwise continues.

	 */
	public boolean apply(int uNid, int second);
	
	public void close();

}
