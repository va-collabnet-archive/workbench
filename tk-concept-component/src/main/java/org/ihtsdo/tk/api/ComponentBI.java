package org.ihtsdo.tk.api;

import java.util.List;
import java.util.UUID;


public interface ComponentBI {

	public UUID getPrimUuid();
	
	public List<UUID> getUUIDs();

	public int getNid();
	
	public int getConceptNid();
	
	public String toUserString();

}
