package org.ihtsdo.tk.api;

import java.util.concurrent.CountDownLatch;

import org.ihtsdo.tk.api.Coordinate;

public interface KindOfCacheBI {

	public abstract void setup(Coordinate coordinate) throws Exception;

	public abstract boolean isKindOf(int childNid, int parentNid)
			throws Exception;
	
	public CountDownLatch getLatch();

}