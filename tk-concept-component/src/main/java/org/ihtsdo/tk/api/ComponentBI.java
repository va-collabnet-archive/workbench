package org.ihtsdo.tk.api;

import java.io.IOException;
import java.util.List;
import java.util.UUID;


public interface ComponentBI {

	public List<UUID> getUUIDs() throws IOException;

	public int getNid();
	
	public int getConceptNid();

}
