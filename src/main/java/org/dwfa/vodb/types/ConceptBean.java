package org.dwfa.vodb.types;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

import org.dwfa.ace.ACE;
import org.dwfa.ace.api.I_AmTermComponent;
import org.dwfa.ace.api.I_ConceptAttributePart;
import org.dwfa.ace.api.I_ConceptAttributeTuple;
import org.dwfa.ace.api.I_ConceptAttributeVersioned;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_DescriptionPart;
import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_DescriptionVersioned;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IdPart;
import org.dwfa.ace.api.I_IdVersioned;
import org.dwfa.ace.api.I_ImagePart;
import org.dwfa.ace.api.I_ImageTuple;
import org.dwfa.ace.api.I_ImageVersioned;
import org.dwfa.ace.api.I_IntList;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_Position;
import org.dwfa.ace.api.I_RelPart;
import org.dwfa.ace.api.I_RelTuple;
import org.dwfa.ace.api.I_RelVersioned;
import org.dwfa.ace.api.I_Transact;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.api.TimePathId;
import org.dwfa.ace.config.AceConfig;
import org.dwfa.ace.log.AceLog;
import org.dwfa.ace.utypes.UniversalAceBean;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.vodb.ToIoException;
import org.dwfa.vodb.VodbEnv;
import org.dwfa.vodb.impl.IdentifierBdbWithSecondaryMap;

import com.sleepycat.je.DatabaseException;

public class ConceptBean implements I_AmTermComponent, I_GetConceptData,
		I_Transact {
	
	public static enum REF_TYPE {SOFT, WEAK};
	
	private static REF_TYPE refType = REF_TYPE.SOFT;

	private static HashMap<Integer, Reference<ConceptBean>> cbeans = new HashMap<Integer, Reference<ConceptBean>>();

	private static ConceptBean privateGet(int conceptId) throws IOException {
		if (cbeans.containsKey(conceptId)) {
			Reference<ConceptBean> ref = cbeans.get(conceptId);
			if (ref != null && ref.isEnqueued() == false) {
				ConceptBean cb = ref.get();
				if (cb != null) {
					return cb;
				} 				
			}
		}

		synchronized (cbeans) {
			if (cbeans.containsKey(conceptId)) {
				Reference<ConceptBean> ref = cbeans.get(conceptId);
				if (ref != null && ref.isEnqueued()) {
					cbeans.remove(conceptId);				
				} else if (ref != null) {
					ConceptBean cb = ref.get();
					if (cb != null) {
						return cb;
					} else {
						cbeans.remove(conceptId);
					}
				}
			}
			ConceptBean cb = new ConceptBean(conceptId);
			AceConfig.getVodb().setupBean(cb);
			if (refType == REF_TYPE.SOFT) {
				cbeans.put(conceptId, new SoftReference<ConceptBean>(cb));
			} else {
				cbeans.put(conceptId, new WeakReference<ConceptBean>(cb));
			}
			return cb;
		}
	}

	public static ConceptBean get(int conceptId) {
		try {
			return privateGet(conceptId);
		} catch (IOException e) {
			throw new RuntimeException();
		}
	}

	public static ConceptBean get(UUID uid) throws TerminologyException,
			IOException {
		return get(AceConfig.getVodb().uuidToNative(uid));
	}

	public static ConceptBean get(Collection<UUID> uids)
			throws TerminologyException, IOException {
		if (uids.contains(IdentifierBdbWithSecondaryMap.additionalInferredUuid)) {
			return get(AceConfig.getVodb().uuidToNative(
					IdentifierBdbWithSecondaryMap.originalInferredUuid));
		} else if (uids.contains(IdentifierBdbWithSecondaryMap.additionalStatedUuid)) {
			return get(AceConfig.getVodb().uuidToNative(
					IdentifierBdbWithSecondaryMap.originalStatedUuid));
		}
		return get(AceConfig.getVodb().uuidToNative(uids));
	}

	private int conceptId;

	private static int dataVersion = 1;

	private void writeObject(ObjectOutputStream out) throws IOException {
		out.writeInt(dataVersion);
		throw new IOException("This class is deliberately not serializable...");
	}

	private void readObject(ObjectInputStream in) throws IOException,
			ClassNotFoundException {
		int objDataVersion = in.readInt();
		if (objDataVersion == dataVersion) {
			//
		} else {
			throw new IOException("Can't handle dataversion: " + objDataVersion);
		}
		throw new IOException("This class is deliberately not serializable...");
	}

	private I_IdVersioned id = null;

	public I_ConceptAttributeVersioned conceptAttributes = null;

	public List<I_DescriptionVersioned> descriptions = null;

	public List<I_RelVersioned> sourceRels = null;

	public List<I_RelVersioned> destRels = null;

	public List<I_ImageVersioned> images = null;

	public List<I_ImageVersioned> uncommittedImages = null;

	public List<I_RelVersioned> uncommittedSourceRels = null;

	public List<I_DescriptionVersioned> uncommittedDescriptions = null;

	public I_ConceptAttributeVersioned uncommittedConceptAttributes = null;

	public I_IntSet uncommittedIds = null;

	private List<UUID> uids = null;

	private boolean primordial = false;

	private IntSet relOrigins = null;

	private ConceptBean(int conceptId) {
		super();
		this.conceptId = conceptId;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.dwfa.vodb.types.I_GetConceptData#getConcept()
	 */
	public I_ConceptAttributeVersioned getConceptAttributes()
			throws IOException {
		if (isPrimordial()) {
			return uncommittedConceptAttributes;
		}
		if (conceptAttributes == null) {
			conceptAttributes = AceConfig.getVodb().getConceptAttributes(
					conceptId);
		}
		return conceptAttributes;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.dwfa.vodb.types.I_GetConceptData#getConceptId()
	 */
	public int getConceptId() {
		return conceptId;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.dwfa.vodb.types.I_GetConceptData#getConceptTuples(org.dwfa.ace.IntSet,
	 *      java.util.Set)
	 */
	public List<I_ConceptAttributeTuple> getConceptAttributeTuples(
			I_IntSet allowedStatus, Set<I_Position> positionSet)
			throws IOException {
		return getConceptAttributeTuples(allowedStatus, positionSet, true);
	}

	public List<I_ConceptAttributeTuple> getConceptAttributeTuples(
			I_IntSet allowedStatus, Set<I_Position> positionSet,
			boolean addUncommitted) throws IOException {
		List<I_ConceptAttributeTuple> returnTuples = new ArrayList<I_ConceptAttributeTuple>();
		getConceptAttributes().addTuples(allowedStatus, positionSet,
				returnTuples, addUncommitted);
		return returnTuples;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.dwfa.vodb.types.I_GetConceptData#getDescriptionTuples(org.dwfa.ace.IntSet,
	 *      org.dwfa.ace.IntSet, java.util.Set)
	 */
	public List<I_DescriptionTuple> getDescriptionTuples(
			I_IntSet allowedStatus, I_IntSet allowedTypes,
			Set<I_Position> positionSet) throws IOException {
		return getDescriptionTuples(allowedStatus, allowedTypes, positionSet,
				true);
	}

	public List<I_DescriptionTuple> getDescriptionTuples(
			I_IntSet allowedStatus, I_IntSet allowedTypes,
			Set<I_Position> positionSet, boolean addUncommitted)
			throws IOException {
		List<I_DescriptionTuple> returnDescriptions = new ArrayList<I_DescriptionTuple>();
		for (I_DescriptionVersioned desc : getDescriptions()) {
			desc.addTuples(allowedStatus, allowedTypes, positionSet,
					returnDescriptions, addUncommitted);
		}
		return returnDescriptions;
	}

	public List<I_ImageTuple> getImageTuples(I_IntSet allowedStatus,
			I_IntSet allowedTypes, Set<I_Position> positions)
			throws IOException {
		List<I_ImageTuple> returnTuples = new ArrayList<I_ImageTuple>();
		for (I_ImageVersioned img : getImages()) {
			img.addTuples(allowedStatus, allowedTypes, positions, returnTuples);
		}
		return returnTuples;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.dwfa.vodb.types.I_GetConceptData#getSourceRelTuples(org.dwfa.ace.IntSet,
	 *      org.dwfa.ace.IntSet, java.util.Set)
	 */
	public List<I_RelTuple> getSourceRelTuples(I_IntSet allowedStatus,
			I_IntSet allowedTypes, Set<I_Position> positionSet,
			boolean addUncommitted) throws IOException {
		List<I_RelTuple> returnRels = new ArrayList<I_RelTuple>();
		for (I_RelVersioned rel : getSourceRels()) {
			rel.addTuples(allowedStatus, allowedTypes, positionSet, returnRels,
					addUncommitted);
		}
		if (addUncommitted) {
			for (I_RelVersioned rel : getUncommittedSourceRels()) {
				rel.addTuples(allowedStatus, allowedTypes, positionSet, returnRels,
						addUncommitted);
			}
		}
		return returnRels;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.dwfa.vodb.types.I_GetConceptData#getDestRelTuples(org.dwfa.ace.IntSet,
	 *      org.dwfa.ace.IntSet, java.util.Set)
	 */
	public List<I_RelTuple> getDestRelTuples(I_IntSet allowedStatus,
			I_IntSet allowedTypes, Set<I_Position> positions,
			boolean addUncommitted) throws IOException {
		List<I_RelTuple> returnRels = new ArrayList<I_RelTuple>();
		for (I_RelVersioned rel : getDestRels()) {
			/*
			 * if ((conceptId == -2147444184) && (rel.getC1Id() == -2147326003)) {
			 * AceLog.getLog().info("getSourceRelTuples for SNOMED CT Concept"); }
			 */
			rel.addTuples(allowedStatus, allowedTypes, positions, returnRels,
					addUncommitted);
		}
		return returnRels;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.dwfa.vodb.types.I_GetConceptData#getDescriptions()
	 */
	public List<I_DescriptionVersioned> getDescriptions() throws IOException {
		if (isPrimordial()) {
			if (descriptions == null) {
				return new ArrayList<I_DescriptionVersioned>();
			}
		}
		if (descriptions == null) {
			try {
				descriptions = AceConfig.getVodb().getDescriptions(conceptId);
			} catch (DatabaseException e) {
				throw new ToIoException(e);
			}
		}
		return descriptions;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.dwfa.vodb.types.I_GetConceptData#getDestRels()
	 */
	public List<I_RelVersioned> getDestRels() throws IOException {
		if (destRels == null) {
			try {
				destRels = AceConfig.getVodb().getDestRels(conceptId);
			} catch (DatabaseException e) {
				throw new ToIoException(e);
			}
		}
		return destRels;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.dwfa.vodb.types.I_GetConceptData#getSourceRels()
	 */
	public List<I_RelVersioned> getSourceRels() throws IOException {
		if (isPrimordial()) {
			return new ArrayList<I_RelVersioned>();
		}
		if (sourceRels == null) {
			try {
				sourceRels = AceConfig.getVodb().getSrcRels(conceptId);
			} catch (DatabaseException e) {
				throw new ToIoException(e);
			}
		}
		return sourceRels;
	}

	public String toString() {
		try {
			return getInitialText();
		} catch (IOException ex) {
			AceLog.getAppLog().alertAndLogException(ex);
			return ex.toString();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.dwfa.vodb.types.I_GetConceptData#getInitialText()
	 */
	public String getInitialText() throws IOException {
		try {
			if ((AceConfig.config != null)
					&& (AceConfig.config.aceFrames.get(0) != null)) {
				I_DescriptionTuple tuple = this.getDescTuple(
						AceConfig.config.aceFrames.get(0)
								.getShortLabelDescPreferenceList(),
						AceConfig.config.getAceFrames().get(0));
				if (tuple != null) {
					return tuple.getText();
				}
			}
			return getText();
		} catch (IndexOutOfBoundsException e) {
			try {
				return getText();
			} catch (IndexOutOfBoundsException e2) {
				return conceptId + " has no desc" ;
			}
		}
	}

	private int fsDescNid = Integer.MIN_VALUE;

	private int fsXmlDescNid = Integer.MIN_VALUE;

	private String getText() {
		try {
			if (getDescriptions().size() > 0) {
				return getDescriptions().get(0).getFirstTuple().getText();
			}
		} catch (IOException ex) {
			AceLog.getAppLog().nonModalAlertAndLogException(ex);
		}

		List<I_DescriptionVersioned> localDesc = getUncommittedDescriptions();
		if (localDesc.size() == 0) {
			try {
				if (fsDescNid == Integer.MIN_VALUE) {
					fsDescNid = LocalVersionedTerminology
							.get()
							.uuidToNative(
									ArchitectonicAuxiliary.Concept.XHTML_FULLY_SPECIFIED_DESC_TYPE
											.getUids());
					fsDescNid = LocalVersionedTerminology
							.get()
							.uuidToNative(
									ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE
											.getUids());
				}
				if (getDescriptions().size() > 0) {
					I_DescriptionVersioned desc = getDescriptions().get(0);
					for (I_DescriptionVersioned d : getDescriptions()) {
						for (I_DescriptionPart part : d.getVersions()) {
							if ((part.getTypeId() == fsDescNid)
									|| (part.getTypeId() == fsXmlDescNid)) {
								return part.getText();
							}
						}
					}
					return desc.getVersions().get(0).getText();
				} else {
					StringBuffer errorBuffer = new StringBuffer();
					errorBuffer.append("No descriptions for concept. uuids: "
									+ AceConfig.getVodb().getUids(conceptId)
											.toString() + " nid: "
									+ AceConfig.getVodb().uuidToNative(
											getUids()));

					int sequence = conceptId + Integer.MIN_VALUE;
					String errString = conceptId
							+ " (" + sequence + ") " + " has no descriptions " + getUids();
					getDescriptions();
					AceLog.getEditLog().severe(errorBuffer.toString());
					return errString;
				}

			} catch (Exception ex) {
				AceLog.getAppLog().nonModalAlertAndLogException(ex);
			}
		}
		I_DescriptionVersioned tdv = localDesc.get(0);
		List<I_DescriptionPart> versions = tdv.getVersions();
		I_DescriptionPart first = versions.get(0);
		return first.getText();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.dwfa.vodb.types.I_GetConceptData#isLeaf(org.dwfa.ace.config.AceFrameConfig)
	 */
	public boolean isLeaf(I_ConfigAceFrame aceConfig, boolean addUncommitted)
			throws IOException {
		try {
			if (aceConfig != null) {
				if (hasDestRelTuples(aceConfig.getAllowedStatus(), aceConfig
						.getDestRelTypes(), aceConfig.getViewPositionSet(),
						addUncommitted)) {
					return false;
				}
				if (hasSourceRelTuples(aceConfig.getAllowedStatus(), aceConfig
						.getSourceRelTypes(), aceConfig.getViewPositionSet(),
						addUncommitted)) {
					return false;
				}
			} else {
				if (destRels != null) {
					if (destRels.size() > 0) {
						return false;
					}
				} else {
					if (AceConfig.getVodb().hasDestRels(conceptId)) {
						return false;
					}
				}
				if (sourceRels != null) {
					if (sourceRels.size() > 0) {
						return false;
					}
				} else {
					if (AceConfig.getVodb().hasSrcRels(conceptId)) {
						return false;
					}
				}
			}
			return true;
		} catch (DatabaseException e) {
			throw new ToIoException(e);
		}
	}

	private boolean hasDestRelTuples(I_IntSet allowedStatus,
			I_IntSet destRelTypes, Set<I_Position> positions,
			boolean addUncommitted) throws IOException {
		if (destRelTypes.getSetValues().length > 0) {
			List<I_RelTuple> returnRels = new ArrayList<I_RelTuple>();
			if (destRels != null) {
				for (I_RelVersioned rel : destRels) {
					rel.addTuples(allowedStatus, destRelTypes, positions,
							returnRels, addUncommitted);
					if (returnRels.size() > 0) {
						return true;
					}
				}
			} else {
				try {
					return AceConfig.getVodb().hasDestRelTuple(conceptId,
							allowedStatus, destRelTypes, positions);
				} catch (DatabaseException e) {
					throw new ToIoException(e);
				}
			}
		}
		return false;
	}

	private boolean hasSourceRelTuples(I_IntSet allowedStatus,
			I_IntSet sourceRelTypes, Set<I_Position> positions,
			boolean addUncommitted) throws IOException {
		if (sourceRelTypes.getSetValues().length > 0) {
			List<I_RelTuple> returnRels = new ArrayList<I_RelTuple>();
			if (sourceRels != null) {
				for (I_RelVersioned rel : sourceRels) {
					rel.addTuples(allowedStatus, sourceRelTypes, positions,
							returnRels, addUncommitted);
					if (returnRels.size() > 0) {
						return true;
					}
				}
			} else {
				try {
					return AceConfig.getVodb().hasSrcRelTuple(conceptId,
							allowedStatus, sourceRelTypes, positions);
				} catch (DatabaseException e) {
					throw new ToIoException(e);
				}
			}
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.dwfa.vodb.types.I_GetConceptData#getImages()
	 */
	public List<I_ImageVersioned> getImages() throws IOException {
		if (images == null) {
			try {
				images = AceConfig.getVodb().getImages(conceptId);
				if (AceLog.getAppLog().isLoggable(Level.FINE)) {
					AceLog.getAppLog().fine(
							"Retrieved images: " + images + " for: "
									+ conceptId);
				}
			} catch (DatabaseException e) {
				throw new ToIoException(e);
			}
		}
		return images;
	}

	public List<I_ImageVersioned> getUncommittedImages() {
		if (uncommittedImages == null) {
			uncommittedImages = new ArrayList<I_ImageVersioned>();
		}
		return uncommittedImages;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (ConceptBean.class.isAssignableFrom(obj.getClass())) {
			ConceptBean another = (ConceptBean) obj;
			return conceptId == another.conceptId;
		}
		return false;
	}

	@Override
	public int hashCode() {
		return conceptId;
	}

	public void commit(int version, Set<TimePathId> values) throws IOException {
		try {
			if (AceLog.getEditLog().isLoggable(Level.FINE)) {
				AceLog.getEditLog().fine(
						"Starting commit for ConceptBean: " + this);
			}
			((VodbEnv) LocalVersionedTerminology.get()).commit(this, version,
					values);
			setPrimordial(false);
			if (AceLog.getAppLog().isLoggable(Level.FINE)) {
				AceLog.getAppLog().fine(
						"Finished commit for ConceptBean: " + this);
			}
		} catch (DatabaseException e) {
			throw new ToIoException(e);
		}
	}

	private void flushDestRelsOnTargetBeans() throws DatabaseException,
			IOException {
		for (I_RelVersioned rel : getSourceRels()) {
			ConceptBean destBean = ConceptBean.get(rel.getC2Id());
			destBean.flushDestRels();
		}
	}

	public void flush() throws DatabaseException, IOException {
		flushDestRelsOnTargetBeans();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.dwfa.vodb.types.I_Transact#abort()
	 */
	public void abort() throws IOException {
		// remove uncommitted brand new components...
		try {
			uncommittedConceptAttributes = null;
			uncommittedDescriptions = null;
			uncommittedSourceRels = null;
			uncommittedImages = null;
			destRels = null;

			if (uncommittedIds != null) {
				boolean delete = true;
				for (int id : uncommittedIds.getSetValues()) {
					I_IdVersioned idv = AceConfig.getVodb().getId(id);
					for (ListIterator<I_IdPart> itr = idv.getVersions()
							.listIterator(); itr.hasNext();) {
						I_IdPart p = itr.next();
						if (p.getVersion() == Integer.MAX_VALUE) {
							itr.remove();
						} else {
							delete = false;
						}
					}
					if (delete) {
						AceConfig.getVodb().deleteId(idv);
					} else {
						AceConfig.getVodb().writeId(idv);
					}
				}
				uncommittedIds = null;
			}

			// remove uncommitted parts...
			if (conceptAttributes != null) {
				for (ListIterator<I_ConceptAttributePart> partItr = conceptAttributes
						.getVersions().listIterator(); partItr.hasNext();) {
					I_ConceptAttributePart part = partItr.next();
					if (part.getVersion() == Integer.MAX_VALUE) {
						partItr.remove();
					}
				}
			}
			if (descriptions != null) {
				for (I_DescriptionVersioned desc : descriptions) {
					for (ListIterator<I_DescriptionPart> partItr = desc
							.getVersions().listIterator(); partItr.hasNext();) {
						I_DescriptionPart part = partItr.next();
						if (part.getVersion() == Integer.MAX_VALUE) {
							partItr.remove();
						}
					}
				}
			}

			if (sourceRels != null) {
				for (I_RelVersioned srcRel : sourceRels) {
					for (ListIterator<I_RelPart> partItr = srcRel.getVersions()
							.listIterator(); partItr.hasNext();) {
						I_RelPart part = partItr.next();
						if (part.getVersion() == Integer.MAX_VALUE) {
							partItr.remove();
						}
					}
				}
			}

			if (images != null) {
				for (I_ImageVersioned img : images) {
					for (ListIterator<I_ImagePart> partItr = img.getVersions()
							.listIterator(); partItr.hasNext();) {
						I_ImagePart part = partItr.next();
						if (part.getVersion() == Integer.MAX_VALUE) {
							partItr.remove();
						}
					}
				}
			}

		} catch (DatabaseException e) {
			throw new ToIoException(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.dwfa.vodb.types.I_GetConceptData#getUid()
	 */
	public List<UUID> getUids() throws IOException {
		if (uids == null) {
			try {
				uids = AceConfig.getVodb().nativeToUuid(conceptId);
			} catch (DatabaseException e) {
				throw new ToIoException(e);
			}
		}
		return uids;
	}

	public static void writeConceptBeanList(ObjectOutputStream out,
			List<ConceptBean> list) throws IOException {
		out.writeInt(list.size());
		for (I_GetConceptData cb : list) {
			try {
				out.writeObject(cb.getUids());
			} catch (IOException e) {
				IOException newEx = new IOException();
				newEx.initCause(e);
				throw newEx;
			}
		}
	}

	public static List<ConceptBean> readConceptBeanList(ObjectInputStream in)
			throws IOException, ClassNotFoundException {
		int size = in.readInt();
		List<ConceptBean> beanList = new ArrayList<ConceptBean>(size);
		for (int i = 0; i < size; i++) {
			try {
				beanList.add(ConceptBean.get((UUID) in.readObject()));
			} catch (TerminologyException e) {
				IOException newEx = new IOException();
				newEx.initCause(e);
				throw newEx;
			}
		}
		return beanList;
	}

	public I_ConceptAttributeVersioned getUncommittedConceptAttributes() {
		return uncommittedConceptAttributes;
	}

	public void setUncommittedConceptAttributes(
			I_ConceptAttributeVersioned uncommittedConcept) {
		this.uncommittedConceptAttributes = uncommittedConcept;
	}

	public List<I_DescriptionVersioned> getUncommittedDescriptions() {
		if (uncommittedDescriptions == null) {
			uncommittedDescriptions = new ArrayList<I_DescriptionVersioned>();
		}
		return uncommittedDescriptions;
	}

	public List<I_RelVersioned> getUncommittedSourceRels() {
		if (uncommittedSourceRels == null) {
			uncommittedSourceRels = new ArrayList<I_RelVersioned>();
		}
		return uncommittedSourceRels;
	}

	public I_RelVersioned getSourceRel(int id) throws IOException {
    for (I_RelVersioned r : getSourceRels()) {
      if (r.getRelId() == id) {
        return r;
      }
    }
    for (I_RelVersioned r : getUncommittedSourceRels()) {
      if (r.getRelId() == id) {
        return r;
      }
    }
		return null;
	}

	public I_RelVersioned getDestRel(int id) throws IOException {
		for (I_RelVersioned r : getDestRels()) {
			if (r.getRelId() == id) {
				return r;
			}
		}
		return null;
	}

	public Set<I_DescriptionTuple> getCommonDescTuples(I_ConfigAceFrame config)
			throws IOException {
		return ConceptBeanConflictHelper.getCommonDescTuples(this, config);
	}

	public Set<I_RelTuple> getCommonRelTuples(I_ConfigAceFrame config)
			throws IOException {
		return ConceptBeanConflictHelper.getCommonRelTuples(this, config);
	}

	public Set<I_ConceptAttributeTuple> getCommonConceptAttributeTuples(
			I_ConfigAceFrame config) throws IOException {
		return ConceptBeanConflictHelper.getCommonConceptAttributeTuples(this,
				config);
	}

	public I_IdVersioned getId() throws IOException {
		if (id == null) {
			id = AceConfig.getVodb().getId(conceptId);
		}
		return id;
	}

	public I_IntSet getUncommittedIds() {
		if (uncommittedIds == null) {
			uncommittedIds = new IntSet();
		}
		return uncommittedIds;
	}

	public I_DescriptionTuple getDescTuple(I_IntList prefOrder,
			I_ConfigAceFrame config) throws IOException {
		return getDescTuple(prefOrder, config.getAllowedStatus(), config
				.getViewPositionSet());
	}

	public I_DescriptionTuple getDescTuple(I_IntList prefOrder,
			I_IntSet allowedStatus, Set<I_Position> positionSet)
			throws IOException {
		I_IntSet typeSet = new IntSet();
		for (int nid : prefOrder.getListArray()) {
			typeSet.add(nid);
		}
		Collection<I_DescriptionTuple> descriptions = getDescriptionTuples(
				allowedStatus, typeSet, positionSet);
		if (prefOrder == null) {
			return descriptions.iterator().next();
		} else {
			for (int typeId : prefOrder.getListValues()) {
				for (I_DescriptionTuple d : descriptions) {
					if (d.getTypeId() == typeId) {
						return d;
					}
				}
			}
		}
		return null;
	}

	public boolean isUncommitted() throws IOException {
		// handle the parts first...
		if (images != null) {
			for (I_ImageVersioned image : images) {
				for (I_ImagePart p : image.getVersions()) {
					if (p.getVersion() == Integer.MAX_VALUE) {
						return true;
					}
				}
			}
		}
		if (conceptAttributes != null) {
			for (I_ConceptAttributePart p : conceptAttributes.getVersions()) {
				if (p.getVersion() == Integer.MAX_VALUE) {
					return true;
				}
			}
		}
		if (descriptions != null) {
			for (I_DescriptionVersioned desc : descriptions) {
				for (I_DescriptionPart p : desc.getVersions()) {
					if (p.getVersion() == Integer.MAX_VALUE) {
						return true;
					}
				}
			}
		}
		if (sourceRels != null) {
			for (I_RelVersioned rel : sourceRels) {
				for (I_RelPart p : rel.getVersions()) {
					if (p.getVersion() == Integer.MAX_VALUE) {
						return true;
					}
				}
			}
		}
		if (uncommittedImages != null) {
			List<I_ImageVersioned> imagesToRemove = new ArrayList<I_ImageVersioned>();
			for (I_ImageVersioned image : uncommittedImages) {
				if (image.getVersions().size() == 0) {
					uncommittedIds.remove(image.getImageId());
					imagesToRemove.add(image);
				}
				for (I_ImagePart p : image.getVersions()) {
					if (p.getVersion() == Integer.MAX_VALUE) {
						return true;
					}
				}
			}
			uncommittedImages.removeAll(imagesToRemove);
		}
		if (uncommittedConceptAttributes != null) {
			for (I_ConceptAttributePart p : uncommittedConceptAttributes
					.getVersions()) {
				if (p.getVersion() == Integer.MAX_VALUE) {
					return true;
				}
			}
		}
		if (uncommittedDescriptions != null) {
			List<I_DescriptionVersioned> descsToRemove = new ArrayList<I_DescriptionVersioned>();
			for (I_DescriptionVersioned desc : uncommittedDescriptions) {
				if (desc.getVersions().size() == 0) {
					uncommittedIds.remove(desc.getDescId());
					descsToRemove.add(desc);
				}
				for (I_DescriptionPart p : desc.getVersions()) {
					if (p.getVersion() == Integer.MAX_VALUE) {
						return true;
					}
				}
			}
			uncommittedDescriptions.removeAll(descsToRemove);
		}
		if (uncommittedSourceRels != null) {
			List<I_RelVersioned> relsToRemove = new ArrayList<I_RelVersioned>();
			for (I_RelVersioned rel : uncommittedSourceRels) {
				if (rel.getVersions().size() == 0) {
					uncommittedIds.remove(rel.getRelId());
					relsToRemove.add(rel);
				}
				for (I_RelPart p : rel.getVersions()) {
					if (p.getVersion() == Integer.MAX_VALUE) {
						return true;
					}
				}
			}
			uncommittedSourceRels.removeAll(relsToRemove);
		}
		if (uncommittedIds != null) {
			for (int id : uncommittedIds.getSetValues()) {
				I_IdVersioned idv;
				idv = AceConfig.getVodb().getId(id);
				for (I_IdPart p : idv.getVersions()) {
					if (p.getVersion() == Integer.MAX_VALUE) {
						return true;
					}
				}
			}
		}

		return false;
	}
	
	public boolean isExtensionUncommitted() throws IOException {
		for (I_Transact to : ACE.getUncommitted()) {
			if (ExtensionByReferenceBean.class.isAssignableFrom(to.getClass())) {
				ExtensionByReferenceBean ebr = (ExtensionByReferenceBean) to;
				if (ebr.getExtension().getComponentId() == this.conceptId) {
					return true;
				}
			}
		}
		return false;
	}

	public void flushDestRels() {
		if (AceLog.getEditLog().isLoggable(Level.FINE)) {
			AceLog.getEditLog().fine("Flushing destination rels for: " + this);
		}
		this.destRels = null;
	}

	public boolean isPrimordial() {
		return primordial;
	}

	public void setPrimordial(boolean primordial) {
		this.primordial = primordial;
	}

	public UniversalAceBean getUniversalAceBean() throws IOException,
			TerminologyException {
		UniversalAceBean uab = new UniversalAceBean();

		uab.setId(getId().getUniversal());

		if (conceptAttributes != null) {
			uab.setConceptAttributes(conceptAttributes.getUniversal());
		}

		for (I_DescriptionVersioned desc : getDescriptions()) {
			uab.getDescriptions().add(desc.getUniversal());
		}

		for (I_RelVersioned rel : getSourceRels()) {
			uab.getSourceRels().add(rel.getUniversal());
		}

		for (I_ImageVersioned image : getImages()) {
			uab.getImages().add(image.getUniversal());
		}

		if (uncommittedConceptAttributes != null) {
			uab.setUncommittedConceptAttributes(uncommittedConceptAttributes
					.getUniversal());
		}

		for (I_DescriptionVersioned desc : getUncommittedDescriptions()) {
			uab.getUncommittedDescriptions().add(desc.getUniversal());
		}

		for (I_RelVersioned rel : getUncommittedSourceRels()) {
			uab.getUncommittedSourceRels().add(rel.getUniversal());
		}

		for (I_ImageVersioned image : getUncommittedImages()) {
			uab.getUncommittedImages().add(image.getUniversal());
		}

		for (int nid : getUncommittedIds().getSetValues()) {
			I_IdVersioned idv = AceConfig.getVodb().getId(nid);
			uab.getUncommittedIds().add(idv.getUniversal());
		}
		return uab;
	}

	public Set<I_GetConceptData> getDestRelOrigins(I_IntSet allowedStatus,
			I_IntSet allowedTypes, Set<I_Position> positions,
			boolean addUncommitted) throws IOException {
		Set<I_GetConceptData> returnValues = new HashSet<I_GetConceptData>();
		for (I_RelTuple rel : getDestRelTuples(allowedStatus, allowedTypes,
				positions, addUncommitted)) {
			returnValues.add(ConceptBean.get(rel.getC1Id()));
		}
		return returnValues;
	}

	public Set<I_GetConceptData> getSourceRelTargets(I_IntSet allowedStatus,
			I_IntSet allowedTypes, Set<I_Position> positions,
			boolean addUncommitted) throws IOException {
		Set<I_GetConceptData> returnValues = new HashSet<I_GetConceptData>();
		for (I_RelTuple rel : getSourceRelTuples(allowedStatus, allowedTypes,
				positions, addUncommitted)) {
			returnValues.add(ConceptBean.get(rel.getC2Id()));
		}
		return returnValues;
	}

	public boolean isParentOfOrEqualTo(I_GetConceptData child,
			I_IntSet allowedStatus, I_IntSet allowedTypes,
			Set<I_Position> positions, boolean addUncommitted)
			throws IOException {
		if (this.conceptId == child.getConceptId()) {
			return true;
		}
		return isParentWithDepthCutoffStartup(child, allowedStatus, allowedTypes,
				positions, addUncommitted, 0);
	}

	public boolean isParentOf(I_GetConceptData child, I_IntSet allowedStatus,
			I_IntSet allowedTypes, Set<I_Position> positions,
			boolean addUncommitted) throws IOException {
		return isParentWithDepthCutoffStartup(child, allowedStatus, allowedTypes,
				positions, addUncommitted, 0);
	}
	
	private transient Map<List<Integer>, SoftReference<Map<Integer, Boolean>>> isParentCacheMap;
	private transient int isParentCacheMapCommitSequence = 0;
	
	private List<Integer> getParentCriterion(I_IntSet allowedStatus, I_IntSet allowedTypes,
			Set<I_Position> positions) {
		List<Integer> intList = new ArrayList<Integer>();
		
		if (allowedStatus != null) {
			for (int i: allowedStatus.getSetValues()) {
				intList.add(i);
			}
		}
		intList.add(Integer.MIN_VALUE);
		if (allowedTypes != null) {
			for (int i: allowedTypes.getSetValues()) {
				intList.add(i);
			}
		}
		intList.add(Integer.MIN_VALUE);
		if (positions != null) {
			for (I_Position p: positions) {
				intList.add(p.getPath().getConceptId());
				intList.add(p.getVersion());
			}
		}
		intList.add(Integer.MIN_VALUE);
		
		return intList;
	}
	
	private boolean isParentWithDepthCutoffStartup(I_GetConceptData child,
			I_IntSet allowedStatus, I_IntSet allowedTypes,
			Set<I_Position> positions, boolean addUncommitted, int depth)
				throws IOException {
		Map<Integer, Boolean> isParentCache = null;
		
		if (isParentCacheMap == null || isParentCacheMapCommitSequence != ACE.commitSequence) {
			isParentCacheMap = new ConcurrentHashMap<List<Integer>, SoftReference<Map<Integer,Boolean>>>();
			isParentCacheMapCommitSequence = ACE.commitSequence;
		}
		List<Integer> key = getParentCriterion(allowedStatus, allowedTypes, positions);
		if (isParentCacheMap.containsKey(key)) {
			isParentCache = isParentCacheMap.get(key).get();
		}
		if (isParentCache == null) {
			isParentCache = new ConcurrentHashMap<Integer, Boolean>();
			isParentCacheMap.put(key, new SoftReference<Map<Integer,Boolean>>(isParentCache));
		}
		return isParentWithDepthCutoff(child,
				allowedStatus, allowedTypes,
				positions, addUncommitted, depth, isParentCache);
	}

	private boolean isParentWithDepthCutoff(I_GetConceptData child,
			I_IntSet allowedStatus, I_IntSet allowedTypes,
			Set<I_Position> positions, boolean addUncommitted, int depth, Map<Integer, Boolean> isParentCache)
			throws IOException {
		if (isParentCache.containsKey(child.getConceptId())) {
			return isParentCache.get(child.getConceptId());
		}
		Set<I_GetConceptData> parents = child.getSourceRelTargets(
				allowedStatus, allowedTypes, positions, addUncommitted);
		if (depth == 90) {
			AceLog
					.getAppLog()
					.log(
							Level.SEVERE,
							"Depth "
									+ depth
									+ " is nearer to isParentOf depth max for: \nparent: "
									+ this + "\nchild: " + child
									+ "\nparents: " + parents);
		}
		if (depth == 100) {
			AceLog.getAppLog().log(
					Level.SEVERE,
					"Depth " + depth
							+ " is at isParentOf depth max for: \nparent: "
							+ this + "\nchild: " + child + "\nparents: "
							+ parents);
			isParentCache.put(child.getConceptId(), Boolean.FALSE);
			return false;
		}
		if (parents.contains(this)) {
			isParentCache.put(child.getConceptId(), Boolean.TRUE);
			return true;
		}
		for (I_GetConceptData childParent : parents) {
			if (this.isParentWithDepthCutoff(childParent, allowedStatus,
					allowedTypes, positions, addUncommitted, depth + 1, isParentCache)) {
				isParentCache.put(child.getConceptId(), Boolean.TRUE);
				return true;
			}
		}
		isParentCache.put(child.getConceptId(), Boolean.FALSE);
		return false;
	}

	public IntSet getRelOrigins() {
		return relOrigins;
	}

	public void setRelOrigins(IntSet relOrigins) {
		this.relOrigins = relOrigins;
	}

}
