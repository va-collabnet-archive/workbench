package org.dwfa.ace.api;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import org.dwfa.ace.utypes.UniversalAceConceptAttributes;
import org.dwfa.tapi.I_ConceptualizeLocally;
import org.dwfa.tapi.TerminologyException;

public interface I_ConceptAttributeVersioned extends I_AmTermComponent {

	public boolean addVersion(I_ConceptAttributePart part);

	public List<I_ConceptAttributePart> getVersions();

	public int versionCount();

	public int getConId();

	public List<I_ConceptAttributeTuple> getTuples();

	public void convertIds(I_MapNativeToNative jarToDbNativeMap);

	public boolean merge(I_ConceptAttributeVersioned jarCon);

	public Set<TimePathId> getTimePathSet();

    public void addTuples(I_IntSet allowedStatus,
         Set<I_Position> positionSet, List<I_ConceptAttributeTuple> returnTuples);

    public void addTuples(I_IntSet allowedStatus,
         Set<I_Position> positionSet, List<I_ConceptAttributeTuple> returnTuples, boolean addUncommitted);
   

    public void addTuples(I_IntSet allowedStatus,
         Set<I_Position> positionSet, List<I_ConceptAttributeTuple> returnTuples, boolean addUncommitted, 
         boolean returnConflictResolvedLatestState) throws TerminologyException, IOException;

	public I_ConceptualizeLocally getLocalFixedConcept();
	
	public UniversalAceConceptAttributes getUniversal() throws IOException, TerminologyException;

	public List<I_ConceptAttributeTuple> getTuples(I_IntSet allowedStatus,
			Set<I_Position> viewPositionSet);

}