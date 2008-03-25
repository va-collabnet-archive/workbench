package org.dwfa.vodb.impl;

import org.dwfa.ace.api.I_RelPart;
import org.dwfa.vodb.I_StoreInBdb;

import com.sleepycat.je.DatabaseException;

public interface I_StoreRelParts extends I_StoreInBdb {

	public abstract int getRelPartId(I_RelPart part) throws DatabaseException;

	public abstract I_RelPart getRelPart(int partId) throws DatabaseException;

}