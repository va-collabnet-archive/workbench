package org.dwfa.ace.api;

import java.io.IOException;

public interface I_ImplementTermFactory extends I_TermFactory {
	public void setup(Object envHome, boolean readOnly, Long cacheSize) throws IOException;
	public void checkpoint() throws IOException;
	public void close() throws IOException;
    public I_ConfigAceDb newAceDbConfig();
}
