package org.ihtsdo.db.bdb;

/**
 * Use Lucene to store all external identifiers, and return a nid. 
 * 
 * @author kec
 *
 */
public abstract class Lucene {
	
	public abstract int getNid(String externalId);

}
