package org.dwfa.ace.api;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import org.dwfa.ace.utypes.UniversalAceImage;
import org.dwfa.tapi.TerminologyException;

public interface I_ImageVersioned extends I_AmTermComponent {

	public byte[] getImage();

	public int getImageId();

	public List<I_ImagePart> getVersions();

	public boolean addVersion(I_ImagePart part);

	public String getFormat();

	public int getConceptId();

	public I_ImageTuple getLastTuple();

	public List<I_ImageTuple> getTuples();

	public void convertIds(I_MapNativeToNative jarToDbNativeMap);

	public boolean merge(I_ImageVersioned jarImage);

	public Set<TimePathId> getTimePathSet();

	public void addTuples(I_IntSet allowedStatus, I_IntSet allowedTypes,
			Set<I_Position> positions, List<I_ImageTuple> returnImages);

	public UniversalAceImage getUniversal() throws IOException,
			TerminologyException;
}