package org.dwfa.cement;

import java.util.Collection;
import java.util.UUID;


public interface I_ConceptEnumeration
{
	//boolean isPrimitive(I_StoreUniversalFixedTerminology server);
	Collection<UUID> getUids();
	boolean isUniversal();
	String name();
	String[] getParents_S();
	String[] getDescriptions_S();
}
