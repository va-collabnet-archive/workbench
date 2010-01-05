package org.ihtsdo.db.bdb.concept.component.description;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.collections.primitives.ArrayIntList;
import org.dwfa.ace.api.I_AmPart;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_DescriptionPart;
import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_DescriptionVersioned;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_ManageConflict;
import org.dwfa.ace.api.I_MapNativeToNative;
import org.dwfa.ace.api.I_Position;
import org.dwfa.ace.api.PathSetReadOnly;
import org.dwfa.ace.api.PositionSetReadOnly;
import org.dwfa.ace.config.AceConfig;
import org.dwfa.ace.utypes.UniversalAceDescription;
import org.dwfa.ace.utypes.UniversalAceDescriptionPart;
import org.dwfa.tapi.I_DescribeConceptLocally;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.tapi.impl.LocalFixedTerminology;
import org.dwfa.vodb.bind.ThinVersionHelper;
import org.dwfa.vodb.conflict.IdentifyAllConflictStrategy;
import org.ihtsdo.db.bdb.Bdb;
import org.ihtsdo.db.bdb.concept.Concept;
import org.ihtsdo.db.bdb.concept.component.ConceptComponent;
import org.ihtsdo.db.util.VersionComputer;
import org.ihtsdo.etypes.EDescription;
import org.ihtsdo.etypes.EDescriptionVersion;

import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

public class Description 
	extends ConceptComponent<DescriptionVersion, Description> 
	implements I_DescriptionVersioned, I_DescriptionPart, I_DescriptionTuple {

	private static class DescTupleComputer extends
			VersionComputer<Description, DescriptionVersion> {
	}

	private static DescTupleComputer computer = new DescTupleComputer();
	
	private String text;
	private boolean initialCaseSignificant;
	private int typeNid; 
	private String lang;

	
	public Description(int nid,
			int versionCount, Concept enclosingConcept, 
			UUID primordialUuid) {
		super(nid, versionCount, enclosingConcept, primordialUuid);
	}
	
	public Description(UniversalAceDescription uDesc, Concept enclosingConcept) {
		super(Bdb.uuidsToNid(uDesc.getDescId()), uDesc.getVersions().size(), 
				enclosingConcept, uDesc.getDescId().iterator().next());
		for (UniversalAceDescriptionPart umPart: uDesc.getMutableParts()) {
			additionalVersions.add(new DescriptionVersion(umPart, this));
		}
	}

	public Description(EDescription eDesc, Concept enclosingConcept) {
		super(eDesc, enclosingConcept);
		initialCaseSignificant = eDesc.isInitialCaseSignificant();
		lang = eDesc.getLang();
		text = eDesc.getText();
		typeNid = Bdb.uuidToNid(eDesc.getTypeUuid());
		primordialStatusAtPositionNid = Bdb.getStatusAtPositionNid(eDesc);
		if (eDesc.getExtraVersionsList() != null) {
			additionalVersions = new ArrayList<DescriptionVersion>(eDesc.getExtraVersionsList().size());
			for (EDescriptionVersion edv: eDesc.getExtraVersionsList()) {
				additionalVersions.add(new DescriptionVersion(edv, this));
			}
		}
	}

	@Override
	public void readFromBdb(TupleInput input, int listSize) {
		// nid, list size, and conceptNid are read already by the binder...
		for (int i = 0; i < listSize; i++) {
			additionalVersions.add(new DescriptionVersion(input, this));
		}
	}

	@Override
	public void writeToBdb(TupleOutput output, int maxReadOnlyStatusAtPositionNid) {
		List<DescriptionVersion> partsToWrite = new ArrayList<DescriptionVersion>();
		for (DescriptionVersion p: additionalVersions) {
			if (p.getStatusAtPositionNid() > maxReadOnlyStatusAtPositionNid) {
				partsToWrite.add(p);
			}
		}
		output.writeShort(partsToWrite.size());
		// conceptNid is the enclosing concept, does not need to be written. 
		for (DescriptionVersion p: partsToWrite) {
			p.writePartToBdb(output);
		}

	}

	@Override
	public void convertIds(I_MapNativeToNative jarToDbNativeMap) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int getConceptId() {
		return enclosingConcept.getNid();
	}

	@Override
	public int getDescId() {
		return nid;
	}

	@Override
	public DescriptionVersion getFirstTuple() {
		return additionalVersions.get(0);
	}

	@Override
	public DescriptionVersion getLastTuple() {
		return additionalVersions.get(additionalVersions.size() - 1);
	}


	@Override
	public List<I_DescriptionTuple> getTuples(
			boolean returnConflictResolvedLatestState)
			throws TerminologyException, IOException {
		List<I_DescriptionTuple> tuples = new ArrayList<I_DescriptionTuple>();
		for (DescriptionVersion p : getMutableParts(returnConflictResolvedLatestState)) {
			tuples.add(p);
		}
		return tuples;
	}

	@Override
	public UniversalAceDescription getUniversal() throws IOException,
			TerminologyException {
		UniversalAceDescription universal = new UniversalAceDescription(
				getUids(nid), enclosingConcept.getUids(), this.versionCount());
		for (DescriptionVersion part : additionalVersions) {
			UniversalAceDescriptionPart universalPart = new UniversalAceDescriptionPart();
			universalPart.setInitialCaseSignificant(part
					.isInitialCaseSignificant());
			universalPart.setLang(part.getLang());
			universalPart.setPathId(getUids(part.getPathId()));
			universalPart.setStatusId(getUids(part.getStatusId()));
			universalPart.setText(part.getText());
			universalPart.setTypeId(getUids(part.getTypeId()));
			universalPart.setTime(ThinVersionHelper.convert(part.getVersion()));
			universal.addVersion(universalPart);
		}
		return universal;
	}

	@Override
	public boolean matches(Pattern p) {
		String lastText = null;
		for (DescriptionVersion desc : additionalVersions) {
			if (desc.getText() != lastText) {
				lastText = desc.getText();
				Matcher m = p.matcher(lastText);
				if (m.find()) {
					return true;
				}
			}
		}
		return false;
	}

	public boolean merge(Description jarDesc) {
		throw new UnsupportedOperationException();
	}

	@Override
	public I_DescribeConceptLocally toLocalFixedDesc() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean promote(I_Position viewPosition, PathSetReadOnly pomotionPaths,
			I_IntSet allowedStatus) throws IOException, TerminologyException {
		throw new UnsupportedOperationException();
		/*
		   int viewPathId = viewPosition.getPath().getConceptId();
		   List<DescriptionVersion> matchingTuples = new ArrayList<DescriptionVersion>();
		   addTuples(allowedStatus, viewPosition, matchingTuples);
		   boolean promotedAnything = false;
		   for (I_Path promotionPath: pomotionPaths) {
			   for (DescriptionVersion dt: matchingTuples) {
				   if (dt.getPathId() == viewPathId) {
					   DescriptionMutablePart promotionPart = 
						   dt.getPart().makeAnalog(dt.getStatusId(), 
								   promotionPath.getConceptId(), 
								   Long.MAX_VALUE);
					   // TODO add a check to see if already promoted?
					   dt.getDescVersioned().addVersion(promotionPart);
					   promotedAnything = true;
				   }
			   }
		   }
		   return promotedAnything;
		 */
	}

	private static Collection<UUID> getUids(int id) throws IOException,
			TerminologyException {
		return LocalFixedTerminology.getStore().getUids(id);
	}

	@Override
	public boolean addVersion(I_DescriptionPart newPart) {
		return additionalVersions.add((DescriptionVersion) newPart);
	}

	@Override
	public boolean merge(I_DescriptionVersioned jarDesc) {
		throw new UnsupportedOperationException();
	}

	public int getConceptNid() {
		return enclosingConcept.getNid();
	}

	/*
	 * Consider depreciating the below methods...
	 */
	
	

	public final List<? extends I_DescriptionTuple> getTuples() {
		List<I_DescriptionTuple> tuples = new ArrayList<I_DescriptionTuple>();
		for (DescriptionVersion p : additionalVersions) {
			tuples.add(p);
		}
		return additionalVersions;
	}

	public void addTuples(I_IntSet allowedStatus, I_Position viewPosition,
			List<I_DescriptionTuple> matchingTuples) {
		throw new UnsupportedOperationException();
		/*
		computer.addTuples(allowedStatus, viewPosition,
				matchingTuples, mutableParts, this);
		*/
	}

	public void addTuples(I_IntSet allowedStatus, I_IntSet allowedTypes,
			PositionSetReadOnly positions, List<I_DescriptionTuple> matchingTuples,
			boolean addUncommitted) {
		throw new UnsupportedOperationException();
		/*
		computer.addTuples(allowedStatus, allowedTypes, positions,
				matchingTuples, addUncommitted, mutableParts, this);
		*/
	}

	public void addTuples(I_IntSet allowedStatus, I_IntSet allowedTypes,
			PositionSetReadOnly positions, List<I_DescriptionTuple> matchingTuples,
			boolean addUncommitted, boolean returnConflictResolvedLatestState)
			throws TerminologyException, IOException {
		List<DescriptionVersion> tuples = new ArrayList<DescriptionVersion>();

		computer.addTuples(allowedStatus, allowedTypes, positions,
				tuples, addUncommitted, additionalVersions, this);

		if (returnConflictResolvedLatestState) {
			I_ConfigAceFrame config = AceConfig.getVodb()
					.getActiveAceFrameConfig();
			I_ManageConflict conflictResolutionStrategy;
			if (config == null) {
				conflictResolutionStrategy = new IdentifyAllConflictStrategy();
			} else {
				conflictResolutionStrategy = config
						.getConflictResolutionStrategy();
			}

			tuples = conflictResolutionStrategy.resolveTuples(tuples);
		}
		matchingTuples.addAll(tuples);
	}

	@Override
	public void addTuples(I_IntSet allowedStatus, I_IntSet allowedTypes,
			Set<I_Position> positionSet,
			List<I_DescriptionTuple> matchingTuples, boolean addUncommitted) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void addTuples(I_IntSet allowedStatus, I_IntSet allowedTypes,
			Set<I_Position> positionSet,
			List<I_DescriptionTuple> matchingTuples, boolean addUncommitted,
			boolean returnConflictResolvedLatestState)
			throws TerminologyException, IOException {
		throw new UnsupportedOperationException();
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getLang() {
		return lang;
	}

	public void setLang(String lang) {
		this.lang = lang;
	}

	public boolean isInitialCaseSignificant() {
		return initialCaseSignificant;
	}

	public void setInitialCaseSignificant(boolean initialCaseSignificant) {
		this.initialCaseSignificant = initialCaseSignificant;
	}

	@Override
	public ArrayIntList getVariableVersionNids() {
		ArrayIntList nidList = new ArrayIntList(3);
		nidList.add(typeNid);
		return nidList;
	}

	@Override
	public int getTypeId() {
		return typeNid;
	}

	@Override
	public void setTypeId(int typeNid) {
		this.typeNid = typeNid;
	}

	@Override
	public I_AmPart makeAnalog(int statusNid, int pathNid, long time) {
		return new DescriptionVersion(this, statusNid, pathNid, time, this);
	}

	@Override
	public I_DescriptionVersioned getDescVersioned() {
		return this;
	}
	
	@Override
	public I_DescriptionPart duplicate() {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public I_DescriptionPart getMutablePart() {
		return this;
	}



}
