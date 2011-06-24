package org.ihtsdo.project;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_DescriptionPart;
import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_DescriptionVersioned;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_HelpRefsets;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.PositionSetReadOnly;
import org.dwfa.ace.api.RefsetPropertyMap;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.api.ebr.I_ExtendByRef;
import org.dwfa.ace.api.ebr.I_ExtendByRefPart;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartCid;
import org.dwfa.ace.api.ebr.I_ExtendByRefVersion;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.etypes.EConcept;
import org.ihtsdo.tk.api.PathBI;
import org.ihtsdo.tk.api.Precedence;
import org.ihtsdo.tk.api.RelAssertionType;

public class ContextualizedDescription implements I_ContextualizeDescription {
	private int descId;
	private Collection<UUID> uuids;
	private int descriptionStatusId;
	private int extensionStatusId;
	private int conceptId;
	private int typeId;
	private String lang;
	private String text;
	private boolean isInitialCaseSignificant;
	private int acceptabilityId;
	private int languageRefsetId;
	private I_DescriptionVersioned<?> descriptionVersioned;
	private I_DescriptionPart descriptionPart;
	private I_ExtendByRef languageExtension;
	private I_ExtendByRefPartCid languageExtensionPart;
	private I_GetConceptData concept;

	public ContextualizedDescription(int descId, int conId, int languageRefsetId) throws Exception {
		super();
		I_TermFactory tf = Terms.get();
		//TODO add config as parameter
		I_ConfigAceFrame config = tf.getActiveAceFrameConfig();
		try {
			//TODO: decide how to validate refsets
			validateRefsetAsLanguageEnum(languageRefsetId);
			concept = tf.getConcept(conId);
			if (concept == null) throw new Exception("Concept not found!");
			try {
				descriptionVersioned = tf.getDescription(descId, conId);
			} catch (IOException e) {
				if (e.getMessage().startsWith("No such description")) {
					for (I_DescriptionVersioned uncommittedDescription : concept.getDescriptions()) {
						if (uncommittedDescription.getDescId() == descId) {
							descriptionVersioned = uncommittedDescription;
						}
					}
				}
			}
			if (descriptionVersioned == null) throw new Exception("Description not found!");


			long lastVersion = Long.MIN_VALUE;
			for (I_DescriptionTuple loopTuple : descriptionVersioned.getTuples(config.getConflictResolutionStrategy())) {
				if (loopTuple.getTime() >= lastVersion) {
					descriptionPart = loopTuple.getMutablePart();
					lastVersion = loopTuple.getTime();
				}
			}

			if (descriptionPart == null) throw new Exception("Description part not found!");

			List<? extends I_ExtendByRef> extensions = tf.getAllExtensionsForComponent(
					descriptionVersioned.getDescId(), true);
			for (I_ExtendByRef extension : extensions) {
				if (extension.getRefsetId() == languageRefsetId) {
					languageExtension = extension;
					break;
				}
			}
			if (languageExtension != null) {

				lastVersion = Long.MIN_VALUE;
				for (I_ExtendByRefVersion loopTuple : languageExtension.getTuples(config.getConflictResolutionStrategy())) {
					if (loopTuple.getTime() >= lastVersion) {
						lastVersion = loopTuple.getTime();
						languageExtensionPart = (I_ExtendByRefPartCid) loopTuple.getMutablePart();
					}
				}
				if (languageExtensionPart == null) throw new Exception("Language refset extension part not found!");

				this.extensionStatusId = languageExtensionPart.getStatusNid();
				this.languageRefsetId = languageExtension.getRefsetId();
				this.acceptabilityId = languageExtensionPart.getC1id();
			}

			this.descId = descriptionVersioned.getDescId();
			this.conceptId = descriptionVersioned.getConceptNid();
			this.uuids = descriptionVersioned.getUniversal().getDescId();
			this.lang = descriptionPart.getLang();
			this.text = descriptionPart.getText();
			this.typeId = descriptionPart.getTypeNid();
			this.isInitialCaseSignificant = descriptionPart.isInitialCaseSignificant();
			this.descriptionStatusId = descriptionPart.getStatusNid();


		} catch (IOException e) {
			e.printStackTrace();
		} catch (TerminologyException e) {
			e.printStackTrace();
		}
	}

	/* (non-Javadoc)
	 * @see org.ihtsdo.project.refset.I_ContextualizeDescription#persistChanges()
	 */
	public boolean persistChanges() throws Exception {
		// Deviation to nochecks
		//		if (1 == 1) {
		//			return persistChangesNoChecks();
		//		}
		// end of deviation, code below is not executed
		boolean success = true;
		I_TermFactory tf = Terms.get();
		I_ConfigAceFrame config = Terms.get().getActiveAceFrameConfig();

		boolean descriptionPartChanged = false;
		if (this.descriptionStatusId != descriptionPart.getStatusNid() || this.typeId != descriptionPart.getTypeNid() ||
				this.lang != descriptionPart.getLang() || this.text != descriptionPart.getText() ||
				this.isInitialCaseSignificant != descriptionPart.isInitialCaseSignificant()) descriptionPartChanged = true;

		if (descriptionPartChanged) {
			if (descriptionPart.getTime() == Long.MAX_VALUE ||
					descriptionPart.getTime() == Integer.MAX_VALUE) {
				descriptionPart.setLang(lang.trim());
				descriptionPart.setText(text.trim());
				descriptionPart.setTypeNid(typeId);
				descriptionPart.setInitialCaseSignificant(isInitialCaseSignificant);
				tf.addUncommitted(concept);
			} else {
				for (PathBI editPath : config.getEditingPathSet()) {
					I_DescriptionPart newDescriptionPart =(I_DescriptionPart) descriptionPart.makeAnalog(
							descriptionStatusId, editPath.getConceptNid(), 
							Long.MAX_VALUE);
					newDescriptionPart.setText(text.trim());
					newDescriptionPart.setLang(lang.trim());
					newDescriptionPart.setInitialCaseSignificant(isInitialCaseSignificant);
					newDescriptionPart.setTypeNid(typeId);
					descriptionVersioned.addVersion(newDescriptionPart);
				}
				tf.addUncommitted(concept);
			}
		}

		boolean extensionPartChanged = false;
		if (languageExtension != null) {
			if (this.extensionStatusId != languageExtensionPart.getStatusNid() || 
					this.acceptabilityId != languageExtensionPart.getC1id()) extensionPartChanged = true;

			if (extensionPartChanged) {
				if (languageExtensionPart.getTime() == Long.MAX_VALUE || 
						languageExtensionPart.getTime() == Integer.MAX_VALUE) {
					languageExtensionPart.setC1id(acceptabilityId);
					tf.addUncommitted(languageExtension);
				} else {
					for (PathBI editPath : config.getEditingPathSet()) {
						I_ExtendByRefPartCid newExtConceptPart = (I_ExtendByRefPartCid) 
						languageExtensionPart.makeAnalog(
								extensionStatusId, editPath.getConceptNid(), 
								Long.MAX_VALUE);
						newExtConceptPart.setC1id(acceptabilityId);
						languageExtension.addVersion(newExtConceptPart);
					}
					tf.addUncommitted(languageExtension);
				}
			}
		}
		if (descriptionPartChanged || extensionPartChanged) {
			success = tf.commit();
		}
		return success;
	}
	/* (non-Javadoc)
	 * @see org.ihtsdo.project.refset.I_ContextualizeDescription#persistChanges()
	 */
	public boolean persistChangesNoChecks() throws Exception {
		boolean success = true;
		I_TermFactory tf = Terms.get();
		I_ConfigAceFrame config = Terms.get().getActiveAceFrameConfig();

		boolean descriptionPartChanged = false;
		if (this.descriptionStatusId != descriptionPart.getStatusNid() || this.typeId != descriptionPart.getTypeNid() ||
				this.lang != descriptionPart.getLang() || this.text != descriptionPart.getText() ||
				this.isInitialCaseSignificant != descriptionPart.isInitialCaseSignificant()) descriptionPartChanged = true;

		if (descriptionPartChanged) {
			if (descriptionPart.getTime() == Long.MAX_VALUE ||
					descriptionPart.getTime() == Integer.MAX_VALUE) {
				descriptionPart.setLang(lang.trim());
				descriptionPart.setText(text.trim());
				descriptionPart.setTypeNid(typeId);
				descriptionPart.setInitialCaseSignificant(isInitialCaseSignificant);
				tf.addUncommittedNoChecks(concept);
			} else {
				for (PathBI editPath : config.getEditingPathSet()) {
					I_DescriptionPart newDescriptionPart =(I_DescriptionPart) descriptionPart.makeAnalog(
							descriptionStatusId, editPath.getConceptNid(), 
							Long.MAX_VALUE);
					newDescriptionPart.setText(text.trim());
					newDescriptionPart.setLang(lang.trim());
					newDescriptionPart.setInitialCaseSignificant(isInitialCaseSignificant);
					newDescriptionPart.setTypeNid(typeId);
					descriptionVersioned.addVersion(newDescriptionPart);
				}
				tf.addUncommittedNoChecks(concept);
			}
		}

		boolean extensionPartChanged = false;
		if (languageExtension != null) {
			if (this.extensionStatusId != languageExtensionPart.getStatusNid() || 
					this.acceptabilityId != languageExtensionPart.getC1id()) extensionPartChanged = true;

			if (extensionPartChanged) {
				if (languageExtensionPart.getTime() == Long.MAX_VALUE || 
						languageExtensionPart.getTime() == Integer.MAX_VALUE) {
					languageExtensionPart.setC1id(acceptabilityId);
					tf.addUncommittedNoChecks(languageExtension);
				} else {
					for (PathBI editPath : config.getEditingPathSet()) {
						I_ExtendByRefPartCid newExtConceptPart = (I_ExtendByRefPartCid) 
						languageExtensionPart.makeAnalog(
								extensionStatusId, editPath.getConceptNid(), 
								Long.MAX_VALUE);
						newExtConceptPart.setC1id(acceptabilityId);
						languageExtension.addVersion(newExtConceptPart);
					}
					tf.addUncommittedNoChecks(languageExtension);
				}
			}
		}
		if (descriptionPartChanged || extensionPartChanged) {
			success = tf.commit();
		}
		return success;
	}

	public void retireFromThisContext() throws Exception {
		this.extensionStatusId = ArchitectonicAuxiliary.Concept.RETIRED.localize().getNid();
		persistChanges();
	}

	public static List<ContextualizedDescription> getContextualizedDescriptions(int conceptId, int languageRefsetId, 
			boolean returnConflictResolvedLatestState) throws TerminologyException, IOException, Exception {
		I_ConfigAceFrame config = Terms.get().getActiveAceFrameConfig();
		return getContextualizedDescriptions(conceptId, languageRefsetId, config.getAllowedStatus(),
				config.getDescTypes(), config.getViewPositionSetReadOnly(), returnConflictResolvedLatestState);
	}

	public static List<ContextualizedDescription> getContextualizedDescriptions(int conceptId, int languageRefsetId, 
			I_IntSet allowedStatus, 
			I_IntSet allowedTypes, PositionSetReadOnly positions, boolean returnConflictResolvedLatestState) 
			throws TerminologyException, IOException, Exception {
		I_TermFactory tf = Terms.get();
		//TODO add config as parameter
		I_ConfigAceFrame config = tf.getActiveAceFrameConfig();
		I_GetConceptData concept = tf.getConcept(conceptId);
		List<ContextualizedDescription> contextualizedDescriptions = new ArrayList<ContextualizedDescription>();
		List<? extends I_DescriptionTuple> tuplesList = concept.getDescriptionTuples(allowedStatus, 
				allowedTypes, positions, Precedence.TIME, config.getConflictResolutionStrategy());
		tuplesList = cleanDescTuplesList(tuplesList);
		for (I_DescriptionTuple descriptionTuple : tuplesList) {
			contextualizedDescriptions.add(new ContextualizedDescription(descriptionTuple.getDescId(), 
					conceptId, languageRefsetId));
		}
		return contextualizedDescriptions;
	}

	private static List<? extends I_DescriptionTuple> cleanDescTuplesList(List<? extends I_DescriptionTuple> tuples) {
		HashMap<Integer, I_DescriptionTuple> cleanMap = new HashMap<Integer, I_DescriptionTuple>();
		for (I_DescriptionTuple loopTuple : tuples) {
			if (cleanMap.get(loopTuple.getDescId()) ==  null) {
				cleanMap.put(loopTuple.getDescId(), loopTuple);
			} else if (cleanMap.get(loopTuple.getDescId()).getTime() < loopTuple.getTime()) {
				cleanMap.put(loopTuple.getDescId(), loopTuple);
			}
		}
		List<I_DescriptionTuple> cleanList = new ArrayList<I_DescriptionTuple>();
		cleanList.addAll(cleanMap.values());
		return cleanList;
	}
	public static I_ContextualizeDescription createNewContextualizedDescription(int conceptId, int languageRefsetId, 
			String langCode) throws Exception {
		I_TermFactory tf = Terms.get();
		//TODO move config to parameter
		I_ConfigAceFrame config = Terms.get().getActiveAceFrameConfig();
		I_HelpRefsets refsetHelper = tf.getRefsetHelper(config);

		//TODO: decide how to validate refsets
		//validateRefsetAsSpec(refsetId);

		I_GetConceptData concept = tf.getConcept(conceptId);
		I_GetConceptData typeConcept = tf.getConcept(ArchitectonicAuxiliary.Concept.SYNONYM_DESCRIPTION_TYPE.getUids());
		I_DescriptionVersioned<?> newDescription = tf.newDescription(UUID.randomUUID(), concept, langCode, 
				"New Description", typeConcept, config);
		newDescription.getMutableParts().iterator().next().setInitialCaseSignificant(false);

		I_GetConceptData acceptabilityConcept = tf.getConcept(ArchitectonicAuxiliary.Concept.ACCEPTABLE.getUids());
		I_GetConceptData languagerefsetConcept = tf.getConcept(languageRefsetId);

		refsetHelper.newRefsetExtension(languageRefsetId, 
				newDescription.getDescId(), EConcept.REFSET_TYPES.CID, 
				new RefsetPropertyMap().with(RefsetPropertyMap.REFSET_PROPERTY.CID_ONE, 
						acceptabilityConcept.getConceptNid()), config);

		tf.addUncommittedNoChecks(concept);

		for (I_ExtendByRef extension : tf.getAllExtensionsForComponent(newDescription.getDescId())) {
			if (extension.getRefsetId() == languageRefsetId &&
					extension.getMutableParts().iterator().next().getTime() == Long.MAX_VALUE) {
				tf.addUncommittedNoChecks(extension);
			}
		}

		I_ContextualizeDescription newContextualizedDescription = new ContextualizedDescription(
				newDescription.getDescId(), concept.getConceptNid(), languageRefsetId);
		return newContextualizedDescription;
	}

	/* (non-Javadoc)
	 * @see org.ihtsdo.project.refset.I_ContextualizeDescription#contextualizeThisDescription(int, int)
	 */
	public I_ContextualizeDescription contextualizeThisDescription(int newLanguageRefsetId, 
			int acceptabilityId) throws Exception {

		//TODO: decide how to validate refsets
		//validateRefsetAsSpec(newLanguageRefsetId);

		I_TermFactory tf = Terms.get();
		//TODO move config to parameter
		I_ConfigAceFrame config = Terms.get().getActiveAceFrameConfig();
		I_HelpRefsets refsetHelper = tf.getRefsetHelper(config);

		I_ContextualizeDescription newContextualizedDescription = new ContextualizedDescription(
				descId, conceptId, newLanguageRefsetId);

		if (newContextualizedDescription.getLanguageExtension() != null) {
			if (newContextualizedDescription.getAcceptabilityId() != acceptabilityId) {
				newContextualizedDescription.setAcceptabilityId(acceptabilityId);
				newContextualizedDescription.persistChanges();
			}
			return newContextualizedDescription;
		} else {
			refsetHelper.newRefsetExtension(newLanguageRefsetId, 
					descId, EConcept.REFSET_TYPES.CID, 
					new RefsetPropertyMap().with(RefsetPropertyMap.REFSET_PROPERTY.CID_ONE, acceptabilityId), config);

			for (I_ExtendByRef extension : tf.getAllExtensionsForComponent(descId, true)) {
				if (extension.getRefsetId() == newLanguageRefsetId &&
						extension.getMutableParts().iterator().next().getTime() == Long.MAX_VALUE) {
					tf.addUncommittedNoChecks(extension);
				}
			}

			newContextualizedDescription = new ContextualizedDescription(
					descId, conceptId, newLanguageRefsetId);
			return newContextualizedDescription;
		}
	}

	public List<? extends I_DescriptionPart> getDescriptionParts(){
		if (descriptionVersioned!=null)
			return descriptionVersioned.getMutableParts();

		return null;
	}

	public List<? extends I_ExtendByRefPart> getLanguageRefsetParts(){
		if (languageExtension!=null){
			return languageExtension.getMutableParts();
		}
		return null;
	}
	private static void validateRefsetAsSpec(int languageRefsetId) throws Exception {
		I_TermFactory tf = Terms.get();
		I_GetConceptData languageRefsetConcept = tf.getConcept(languageRefsetId);
		I_GetConceptData refsetTypeConcept = tf.getConcept(
				RefsetAuxiliary.Concept.LANGUAGE_SPEC_EXTENSION.getUids());
		I_ConfigAceFrame config = tf.getActiveAceFrameConfig();
		Set<? extends I_GetConceptData> refsetTypes = getSourceRelTarget(languageRefsetConcept, config, 
				RefsetAuxiliary.Concept.REFSET_TYPE_REL.localize().getNid());
		boolean isValid = false;
		for (I_GetConceptData refsetType : refsetTypes) {
			if (refsetType.getConceptNid() == refsetTypeConcept.getConceptNid()) {
				isValid = true;
			}
		}
		if (!isValid) throw new Exception("Refset type must be a refset spec");
		return;
	}

	private static void validateRefsetAsLanguageEnum(int languageRefsetId) throws Exception {
		/*	I_TermFactory tf = Terms.get();
		I_GetConceptData languageRefsetConcept = tf.getConcept(languageRefsetId);
		I_GetConceptData refsetTypeConcept = tf.getConcept(
				RefsetAuxiliary.Concept.LANGUAGE_ENUMERATION_EXTENSION.getUids());
		I_ConfigAceFrame config = tf.getActiveAceFrameConfig();
		Set<? extends I_GetConceptData> refsetTypes = getSourceRelTarget(languageRefsetConcept, config, 
				RefsetAuxiliary.Concept.REFSET_TYPE_REL.localize().getNid());
		boolean isValid = false;
		for (I_GetConceptData refsetType : refsetTypes) {
			if (refsetType.getConceptId() == refsetTypeConcept.getConceptId()) {
				isValid = true;
			}
		}
		if (!isValid) throw new Exception("Refset type must be a refset enum");*/
		return;
	}

	private static Set<? extends I_GetConceptData> getSourceRelTarget(I_GetConceptData refsetIdentityConcept,
			I_ConfigAceFrame config, int refsetIdentityNid) throws IOException, TerminologyException {
		I_TermFactory tf = Terms.get();
		I_IntSet allowedTypes = tf.newIntSet();
		allowedTypes.add(refsetIdentityNid);
		Set<? extends I_GetConceptData> matchingConcepts = refsetIdentityConcept.getSourceRelTargets(
				config.getAllowedStatus(),
				allowedTypes, config.getViewPositionSetReadOnly(), Precedence.TIME,
				config.getConflictResolutionStrategy());
		return matchingConcepts;
	}

	/* (non-Javadoc)
	 * @see org.ihtsdo.project.refset.I_ContextualizeDescription#getDescId()
	 */
	public int getDescId() {
		return descId;
	}

	/* (non-Javadoc)
	 * @see org.ihtsdo.project.refset.I_ContextualizeDescription#getConceptId()
	 */
	public int getConceptId() {
		return conceptId;
	}

	/* (non-Javadoc)
	 * @see org.ihtsdo.project.refset.I_ContextualizeDescription#getTypeId()
	 */
	public int getTypeId() {
		return typeId;
	}

	/* (non-Javadoc)
	 * @see org.ihtsdo.project.refset.I_ContextualizeDescription#setTypeNid(int)
	 */
	public void setTypeId(int typeId) {
		this.typeId = typeId;
	}

	/* (non-Javadoc)
	 * @see org.ihtsdo.project.refset.I_ContextualizeDescription#getLang()
	 */
	public String getLang() {
		return lang;
	}

	/* (non-Javadoc)
	 * @see org.ihtsdo.project.refset.I_ContextualizeDescription#setLang(java.lang.String)
	 */
	public void setLang(String lang) {
		this.lang = lang;
	}

	/* (non-Javadoc)
	 * @see org.ihtsdo.project.refset.I_ContextualizeDescription#getText()
	 */
	public String getText() {
		return text;
	}

	/* (non-Javadoc)
	 * @see org.ihtsdo.project.refset.I_ContextualizeDescription#setText(java.lang.String)
	 */
	public void setText(String text) {
		this.text = text;
	}

	/* (non-Javadoc)
	 * @see org.ihtsdo.project.refset.I_ContextualizeDescription#getAcceptabilityId()
	 */
	public int getAcceptabilityId() {
		return acceptabilityId;
	}

	/* (non-Javadoc)
	 * @see org.ihtsdo.project.refset.I_ContextualizeDescription#setAcceptabilityId(int)
	 */
	public void setAcceptabilityId(int acceptabilityId) {
		this.acceptabilityId = acceptabilityId;
	}

	/* (non-Javadoc)
	 * @see org.ihtsdo.project.refset.I_ContextualizeDescription#getLanguageRefsetId()
	 */
	public int getLanguageRefsetId() {
		return languageRefsetId;
	}

	/* (non-Javadoc)
	 * @see org.ihtsdo.project.refset.I_ContextualizeDescription#getDescriptionVersioned()
	 */
	public I_DescriptionVersioned getDescriptionVersioned() {
		return descriptionVersioned;
	}

	/* (non-Javadoc)
	 * @see org.ihtsdo.project.refset.I_ContextualizeDescription#getDescriptionPart()
	 */
	public I_DescriptionPart getDescriptionPart() {
		return descriptionPart;
	}

	/* (non-Javadoc)
	 * @see org.ihtsdo.project.refset.I_ContextualizeDescription#getLanguageExtension()
	 */
	public I_ExtendByRef getLanguageExtension() {
		return languageExtension;
	}

	/* (non-Javadoc)
	 * @see org.ihtsdo.project.refset.I_ContextualizeDescription#getLanguageExtensionPart()
	 */
	public I_ExtendByRefPartCid getLanguageExtensionPart() {
		return languageExtensionPart;
	}

	/* (non-Javadoc)
	 * @see org.ihtsdo.project.refset.I_ContextualizeDescription#getConcept()
	 */
	public I_GetConceptData getConcept() {
		return concept;
	}

	/* (non-Javadoc)
	 * @see org.ihtsdo.project.refset.I_ContextualizeDescription#getExtensionStatusId()
	 */
	public int getExtensionStatusId() {
		return extensionStatusId;
	}

	/* (non-Javadoc)
	 * @see org.ihtsdo.project.refset.I_ContextualizeDescription#setExtensionStatusId(int)
	 */
	public void setExtensionStatusId(int extensionStatusId) {
		this.extensionStatusId = extensionStatusId;
	}

	/* (non-Javadoc)
	 * @see org.ihtsdo.project.refset.I_ContextualizeDescription#getUuids()
	 */
	public Collection<UUID> getUuids() {
		return uuids;
	}

	/* (non-Javadoc)
	 * @see org.ihtsdo.project.refset.I_ContextualizeDescription#getDescriptionStatusId()
	 */
	public int getDescriptionStatusId() {
		return descriptionStatusId;
	}

	/* (non-Javadoc)
	 * @see org.ihtsdo.project.refset.I_ContextualizeDescription#setDescriptionStatusId(int)
	 */
	public void setDescriptionStatusId(int descriptionStatusId) {
		this.descriptionStatusId = descriptionStatusId;
	}

	/* (non-Javadoc)
	 * @see org.ihtsdo.project.refset.I_ContextualizeDescription#setInitialCaseSignificant(boolean)
	 */
	public void setInitialCaseSignificant(boolean isInitialCaseSignificant) {
		this.isInitialCaseSignificant = isInitialCaseSignificant;
	}

	/* (non-Javadoc)
	 * @see org.ihtsdo.project.refset.I_ContextualizeDescription#isInitialCaseSignificant()
	 */
	public boolean isInitialCaseSignificant() {
		return isInitialCaseSignificant;
	}

	/* (non-Javadoc)
	 * @see org.ihtsdo.project.refset.I_ContextualizeDescription#toString()
	 */
	public String toString() {
		return text;
	}
}
