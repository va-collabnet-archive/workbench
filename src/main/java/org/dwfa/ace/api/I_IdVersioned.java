package org.dwfa.ace.api;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.dwfa.ace.utypes.UniversalAceIdentification;
import org.dwfa.tapi.TerminologyException;


public interface I_IdVersioned extends I_AmTermComponent {

	public int getNativeId();

	public void setNativeId(int nativeId);

	public List<I_IdPart> getVersions();

	public List<UUID> getUIDs();

	public boolean addVersion(I_IdPart srcId);

	public boolean hasVersion(I_IdPart newPart);

	public Set<TimePathId> getTimePathSet();

	public List<I_IdTuple> getTuples();
	
	public UniversalAceIdentification getUniversal() throws IOException, TerminologyException;

}