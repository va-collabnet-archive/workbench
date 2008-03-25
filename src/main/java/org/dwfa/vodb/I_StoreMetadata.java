package org.dwfa.vodb;

import java.io.IOException;
import java.util.Map;

public interface I_StoreMetadata extends I_StoreInBdb {

	public String getProperty(String key) throws IOException;

	public Map<String, String> getProperties() throws IOException;

	public void setProperty(String key, String value)
			throws IOException;

}
