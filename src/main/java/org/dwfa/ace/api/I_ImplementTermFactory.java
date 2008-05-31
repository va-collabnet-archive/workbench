package org.dwfa.ace.api;

import java.io.IOException;

public interface I_ImplementTermFactory extends I_TermFactory {
	public void setup(Object envHome, boolean readOnly, Long cacheSize) throws IOException;
	public void setup(Object envHome, boolean readOnly, Long cacheSize, DatabaseSetupConfig databaseSetupConfig) throws IOException;
	public void checkpoint() throws IOException;
	/**
	 * 
	 * @param minUtilization a number between 0 and 90 reflecting the desired minimum percent utilization of the log files. 
	 * @throws IOException
	 */
	public void compress(int minUtilization) throws IOException;
	public void close() throws IOException;
    public I_ConfigAceDb newAceDbConfig();
}
