package org.dwfa.tapi;

import java.io.IOException;
import java.util.Collection;
import java.util.UUID;

public interface I_AmIdentified {
	public Collection<UUID> getUids() throws IOException, TerminologyException;
}
