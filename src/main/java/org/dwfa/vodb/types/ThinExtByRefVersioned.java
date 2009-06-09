package org.dwfa.vodb.types;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

import org.dwfa.ace.api.I_AmPart;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_Position;
import org.dwfa.ace.api.I_ManageConflict;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPart;
import org.dwfa.ace.api.ebr.I_ThinExtByRefTuple;
import org.dwfa.ace.api.ebr.I_ThinExtByRefVersioned;
import org.dwfa.ace.config.AceConfig;
import org.dwfa.ace.log.AceLog;
import org.dwfa.ace.table.TupleAdder;
import org.dwfa.ace.utypes.UniversalAceExtByRefPart;
import org.dwfa.ace.utypes.UniversalAceExtByRefPartBoolean;
import org.dwfa.ace.utypes.UniversalAceExtByRefPartConcept;
import org.dwfa.ace.utypes.UniversalAceExtByRefPartConceptConcept;
import org.dwfa.ace.utypes.UniversalAceExtByRefPartConceptConceptConcept;
import org.dwfa.ace.utypes.UniversalAceExtByRefPartConceptConceptString;
import org.dwfa.ace.utypes.UniversalAceExtByRefPartConceptString;
import org.dwfa.ace.utypes.UniversalAceExtByRefPartInteger;
import org.dwfa.ace.utypes.UniversalAceExtByRefPartLanguage;
import org.dwfa.ace.utypes.UniversalAceExtByRefPartMeasurement;
import org.dwfa.ace.utypes.UniversalAceExtByRefPartScopedLanguage;
import org.dwfa.ace.utypes.UniversalAceExtByRefPartString;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.vodb.VodbEnv;
import org.dwfa.vodb.bind.ThinVersionHelper;
import org.dwfa.vodb.conflict.IdentifyAllConflictStrategy;

/**
 * @todo add version to vodb -> added as getProperty...
 * @todo add imported change set info to vodb, need to set theProperty...
 * 
 * @todo have change sets automatically increment as size increases over a
 *       certain size. Added increment to change set file name format.
 * @todo add extension ability
 * 
 * @author kec
 * 
 */
public class ThinExtByRefVersioned implements I_ThinExtByRefVersioned {

	private int refsetId;

	private int memberId;

	private int componentId;

	private int typeId; // Use an enumeration when reading/writing, and convert
	// it to the corresponding concept nid...

	private List<I_ThinExtByRefPart> versions;

	public ThinExtByRefVersioned(int refsetId, int memberId, int componentId,
			int typeId) {
		this(refsetId, memberId, componentId, typeId, 1);
	}

	public ThinExtByRefVersioned(int refsetId, int memberId, int componentId,
			int typeId, int partCount) {
		super();
		this.refsetId = refsetId;
		this.memberId = memberId;
		this.componentId = componentId;
		this.typeId = typeId;
		this.versions = new ArrayList<I_ThinExtByRefPart>(partCount);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.dwfa.vodb.types.I_ThinExtByRefVersioned#getMemberId()
	 */
	public int getMemberId() {
		return memberId;
	}

	public int getTermComponentId() {
		return memberId;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.dwfa.vodb.types.I_ThinExtByRefVersioned#getComponentId()
	 */
	public int getComponentId() {
		return componentId;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.dwfa.vodb.types.I_ThinExtByRefVersioned#getTypeId()
	 */
	public int getTypeId() {
		return typeId;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.dwfa.vodb.types.I_ThinExtByRefVersioned#getVersions()
	 */
	public List<? extends I_ThinExtByRefPart> getVersions() {
		return versions;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.dwfa.vodb.types.I_ThinExtByRefVersioned#getRefsetId()
	 */
	public int getRefsetId() {
		return refsetId;
	}

	@Override
	public boolean equals(Object obj) {
		ThinExtByRefVersioned another = (ThinExtByRefVersioned) obj;
		return ((refsetId == another.refsetId)
				&& (memberId == another.memberId)
				&& (componentId == another.componentId)
				&& (typeId == another.typeId) && (versions
				.equals(another.versions)));
	}

	@Override
	public int hashCode() {
		return HashFunction.hashCode(new int[] { refsetId, memberId,
				componentId, typeId });
	}

	@Override
	public String toString() {
		return "ThinExtByRefVersioned refsetId: " + refsetId + " memberId: "
				+ memberId + " componentId: " + componentId + " typeId: "
				+ typeId + " versions: " + versions;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.dwfa.vodb.types.I_ThinExtByRefVersioned#addVersion(org.dwfa.vodb.types.ThinExtByRefPart)
	 */
	public void addVersion(I_ThinExtByRefPart part) {
		if (AceLog.getEditLog().isLoggable(Level.FINE)) {
			AceLog.getEditLog().fine(
					"Adding part: " + part + " to member: " + memberId
							+ " for component: " + componentId);
		}
		versions.add(part);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.dwfa.vodb.types.I_ThinExtByRefVersioned#setRefsetId(int)
	 */
	public void setRefsetId(int refsetId) {
		this.refsetId = refsetId;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.dwfa.vodb.types.I_ThinExtByRefVersioned#setTypeId(int)
	 */
	public void setTypeId(int typeId) {
		this.typeId = typeId;
	}

	public static ThinExtByRefPart makePart(UniversalAceExtByRefPart part)
			throws TerminologyException, IOException {
		VodbEnv vodb = AceConfig.getVodb();
		if (UniversalAceExtByRefPartBoolean.class.isAssignableFrom(part
				.getClass())) {
			ThinExtByRefPartBoolean thinPart = new ThinExtByRefPartBoolean();
			setStandardFields(part, vodb, thinPart);

			UniversalAceExtByRefPartBoolean booleanPart = (UniversalAceExtByRefPartBoolean) part;
			thinPart.setValue(booleanPart.getBooleanValue());
			return thinPart;
		} else if (UniversalAceExtByRefPartConcept.class.isAssignableFrom(part
				.getClass())) {
			ThinExtByRefPartConcept thinPart = new ThinExtByRefPartConcept();
			setStandardFields(part, vodb, thinPart);

			UniversalAceExtByRefPartConcept conceptPart = (UniversalAceExtByRefPartConcept) part;
			thinPart.setConceptId(vodb
					.uuidToNative(conceptPart.getConceptUid()));
			return thinPart;

		} else if (UniversalAceExtByRefPartInteger.class.isAssignableFrom(part
				.getClass())) {
			ThinExtByRefPartInteger thinPart = new ThinExtByRefPartInteger();
			setStandardFields(part, vodb, thinPart);

			UniversalAceExtByRefPartInteger integerPart = (UniversalAceExtByRefPartInteger) part;
			thinPart.setValue(integerPart.getIntValue());
			return thinPart;

		} else if (UniversalAceExtByRefPartLanguage.class.isAssignableFrom(part
				.getClass())) {
			ThinExtByRefPartLanguage thinPart = new ThinExtByRefPartLanguage();
			setStandardFields(part, vodb, thinPart);

			UniversalAceExtByRefPartLanguage languagePart = (UniversalAceExtByRefPartLanguage) part;
			thinPart.setAcceptabilityId(vodb.uuidToNative(languagePart
					.getAcceptabilityUids()));
			thinPart.setCorrectnessId(vodb.uuidToNative(languagePart
					.getCorrectnessUids()));
			thinPart.setDegreeOfSynonymyId(vodb.uuidToNative(languagePart
					.getDegreeOfSynonymyUids()));
			return thinPart;

		} else if (UniversalAceExtByRefPartMeasurement.class
				.isAssignableFrom(part.getClass())) {
			ThinExtByRefPartMeasurement thinPart = new ThinExtByRefPartMeasurement();
			setStandardFields(part, vodb, thinPart);

			UniversalAceExtByRefPartMeasurement measurementPart = (UniversalAceExtByRefPartMeasurement) part;
			thinPart.setMeasurementValue(measurementPart.getMeasurementValue());
			thinPart.setUnitsOfMeasureId(vodb.uuidToNative(measurementPart
					.getUnitsOfMeasureUids()));
			return thinPart;

		} else if (UniversalAceExtByRefPartScopedLanguage.class
				.isAssignableFrom(part.getClass())) {
			ThinExtByRefPartLanguageScoped thinPart = new ThinExtByRefPartLanguageScoped();
			setStandardFields(part, vodb, thinPart);

			UniversalAceExtByRefPartScopedLanguage scopedLanguagePart = (UniversalAceExtByRefPartScopedLanguage) part;
			thinPart.setAcceptabilityId(vodb.uuidToNative(scopedLanguagePart
					.getAcceptabilityUids()));
			thinPart.setCorrectnessId(vodb.uuidToNative(scopedLanguagePart
					.getCorrectnessUids()));
			thinPart.setDegreeOfSynonymyId(vodb.uuidToNative(scopedLanguagePart
					.getDegreeOfSynonymyUids()));

			thinPart.setPriority(scopedLanguagePart.getPriority());
			thinPart.setScopeId(vodb.uuidToNative(scopedLanguagePart
					.getScopeUids()));
			thinPart.setTagId(vodb
					.uuidToNative(scopedLanguagePart.getTagUids()));
			return thinPart;

		} else if (UniversalAceExtByRefPartString.class.isAssignableFrom(part
				.getClass())) {
			ThinExtByRefPartString thinPart = new ThinExtByRefPartString();
			setStandardFields(part, vodb, thinPart);

			UniversalAceExtByRefPartString stringPart = (UniversalAceExtByRefPartString) part;
			thinPart.setStringValue(stringPart.getStringValue());
			return thinPart;

		} else if (UniversalAceExtByRefPartConceptString.class.isAssignableFrom(part
				.getClass())) {
			ThinExtByRefPartConceptString thinPart = new ThinExtByRefPartConceptString();
			setStandardFields(part, vodb, thinPart);

			UniversalAceExtByRefPartConceptString conceptStringPart = (UniversalAceExtByRefPartConceptString) part;
			thinPart.setC1id(vodb.uuidToNative(conceptStringPart.getC1UuidCollection()));
			thinPart.setStr(conceptStringPart.getStr());
			return thinPart;

		} else if (UniversalAceExtByRefPartConceptConcept.class.isAssignableFrom(part
				.getClass())) {
			ThinExtByRefPartConceptConcept thinPart = new ThinExtByRefPartConceptConcept();
			setStandardFields(part, vodb, thinPart);

			UniversalAceExtByRefPartConceptConcept ccPart = (UniversalAceExtByRefPartConceptConcept) part;
			thinPart.setC1id(vodb.uuidToNative(ccPart.getC1UuidCollection()));
			thinPart.setC2id(vodb.uuidToNative(ccPart.getC2UuidCollection()));
			return thinPart;

		} else if (UniversalAceExtByRefPartConceptConceptConcept.class.isAssignableFrom(part
				.getClass())) {
			ThinExtByRefPartConceptConceptConcept thinPart = new ThinExtByRefPartConceptConceptConcept();
			setStandardFields(part, vodb, thinPart);

			UniversalAceExtByRefPartConceptConceptConcept cccPart = (UniversalAceExtByRefPartConceptConceptConcept) part;
			thinPart.setC1id(vodb.uuidToNative(cccPart.getC1UuidCollection()));
			thinPart.setC2id(vodb.uuidToNative(cccPart.getC2UuidCollection()));
			thinPart.setC3id(vodb.uuidToNative(cccPart.getC3UuidCollection()));
			return thinPart;

		} else if (UniversalAceExtByRefPartConceptConceptString.class.isAssignableFrom(part
				.getClass())) {
			ThinExtByRefPartConceptConceptString thinPart = new ThinExtByRefPartConceptConceptString();
			setStandardFields(part, vodb, thinPart);

			UniversalAceExtByRefPartConceptConceptString ccsPart = (UniversalAceExtByRefPartConceptConceptString) part;
			thinPart.setC1id(vodb.uuidToNative(ccsPart.getC1UuidCollection()));
			thinPart.setC2id(vodb.uuidToNative(ccsPart.getC2UuidCollection()));
			thinPart.setStr(ccsPart.getStr());
			return thinPart;

		} else {
			throw new UnsupportedOperationException(
					"Can't handle UniversalAceExtByRefPart of type: "
							+ part.getClass());
		}
	}

	private static void setStandardFields(UniversalAceExtByRefPart part,
			VodbEnv vodb, I_ThinExtByRefPart thinPart)
			throws TerminologyException, IOException {
		thinPart.setPathId(vodb.uuidToNative(part.getPathUid()));
		thinPart.setStatusId(vodb.uuidToNative(part.getStatusUid()));
		thinPart.setVersion(ThinVersionHelper.convert(part.getTime()));
	}

	private class ExtTupleAdder extends
			TupleAdder<I_ThinExtByRefTuple, ThinExtByRefVersioned> {

		@Override
		public I_ThinExtByRefTuple makeTuple(I_AmPart part,
				ThinExtByRefVersioned core) {
			return new ThinExtByRefTuple(core, (I_ThinExtByRefPart) part);
		}

	}

	ExtTupleAdder adder = new ExtTupleAdder();

	public void addTuples(I_IntSet allowedStatus, Set<I_Position> positions,
			List<I_ThinExtByRefTuple> matchingTuples, boolean addUncommitted) {
		adder.addTuples(allowedStatus, null, positions, matchingTuples,
				addUncommitted, versions, this);
	}
	
	public void addTuples(I_IntSet allowedStatus, Set<I_Position> positions,
			List<I_ThinExtByRefTuple> returnTuples, boolean addUncommitted,
			boolean returnConflictResolvedLatestState) throws TerminologyException, IOException {
		
	    List<I_ThinExtByRefTuple> tuples = new ArrayList<I_ThinExtByRefTuple>();
		
	    addTuples(allowedStatus, positions, returnTuples, addUncommitted);
		
		if (returnConflictResolvedLatestState) {
		    I_ConfigAceFrame config = AceConfig.getVodb().getActiveAceFrameConfig();
			I_ManageConflict conflictResolutionStrategy;
			if (config == null) {
				conflictResolutionStrategy = new IdentifyAllConflictStrategy();
			} else {
				conflictResolutionStrategy = config.getConflictResolutionStrategy();
			}
			
			tuples = conflictResolutionStrategy.resolveTuples(tuples);
		}

		returnTuples.addAll(tuples);
	}

	public void addTuples(List<I_ThinExtByRefTuple> returnTuples,
			boolean addUncommitted, boolean returnConflictResolvedLatestState) throws TerminologyException, IOException {
		
		I_ConfigAceFrame config = AceConfig.getVodb().getActiveAceFrameConfig();
		addTuples(config.getAllowedStatus(), config.getViewPositionSet(), returnTuples, 
				addUncommitted, returnConflictResolvedLatestState);
	}

	public List<I_ThinExtByRefTuple> getTuples(I_IntSet allowedStatus,
			Set<I_Position> positions, boolean addUncommitted) {
		List<I_ThinExtByRefTuple> returnTuples = new ArrayList<I_ThinExtByRefTuple>();
		addTuples(allowedStatus, positions, returnTuples, addUncommitted);
		return returnTuples;
	}

	public List<I_ThinExtByRefTuple> getTuples(I_IntSet allowedStatus,
			Set<I_Position> positions, boolean addUncommitted,
			boolean returnConflictResolvedLatestState) throws TerminologyException, IOException {
		List<I_ThinExtByRefTuple> returnTuples = new ArrayList<I_ThinExtByRefTuple>();
		addTuples(allowedStatus, positions, returnTuples, addUncommitted, returnConflictResolvedLatestState);
		return returnTuples;
	}

	public List<I_ThinExtByRefTuple> getTuples(boolean addUncommitted,
			boolean returnConflictResolvedLatestState) throws TerminologyException, IOException {
		List<I_ThinExtByRefTuple> returnTuples = new ArrayList<I_ThinExtByRefTuple>();
		addTuples(returnTuples, addUncommitted, returnConflictResolvedLatestState);
		return returnTuples;
	}
}
