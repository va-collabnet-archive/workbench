package org.dwfa.vodb.types;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.logging.Level;

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
import org.dwfa.ace.api.TimePathId;
import org.dwfa.ace.config.AceConfig;
import org.dwfa.ace.log.AceLog;
import org.dwfa.ace.utypes.UniversalAceBean;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.vodb.ToIoException;

import com.sleepycat.je.DatabaseException;

public class ConceptBean implements I_AmTermComponent, I_GetConceptData,
		I_Transact {
	private static WeakHashMap<ConceptBean, WeakReference<ConceptBean>> cbeans = new WeakHashMap<ConceptBean, WeakReference<ConceptBean>>();

	public static ConceptBean get(int conceptId) {
		ConceptBean cb = new ConceptBean(conceptId);
		WeakReference<ConceptBean> ref = cbeans.get(cb);
		if (ref != null) {
			cb = ref.get();
		} else {
			synchronized (cbeans) {
				ref = cbeans.get(cb);
				if (ref == null) {
					cbeans.put(cb, new WeakReference<ConceptBean>(cb));
				} else {
					cb = ref.get();
				}
			}
		}
		return cb;
	}

	public static ConceptBean get(UUID uid) throws TerminologyException,
			IOException {
		return get(AceConfig.getVodb().uuidToNative(uid));
	}

	public static ConceptBean get(Collection<UUID> uids)
			throws TerminologyException, IOException {
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


	private I_IdVersioned id;

	private I_ConceptAttributeVersioned conceptAttributes;

	private List<I_DescriptionVersioned> descriptions;

	private List<I_RelVersioned> sourceRels;

	private List<I_RelVersioned> destRels;

	private List<I_ImageVersioned> images;

	private List<I_ImageVersioned> uncommittedImages;

	private List<I_RelVersioned> uncommittedSourceRels;

	private List<I_DescriptionVersioned> uncommittedDescriptions;

	private I_ConceptAttributeVersioned uncommittedConceptAttributes;

	private I_IntSet uncommittedIds;

	private List<UUID> uids;

	private boolean primordial = false;

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
				conceptAttributes = AceConfig.getVodb().getConceptAttributes(conceptId);
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
		List<I_ConceptAttributeTuple> returnTuples = new ArrayList<I_ConceptAttributeTuple>();
		getConceptAttributes().addTuples(allowedStatus, positionSet,
				returnTuples);
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
		List<I_DescriptionTuple> returnDescriptions = new ArrayList<I_DescriptionTuple>();
		for (I_DescriptionVersioned desc : getDescriptions()) {
			desc.addTuples(allowedStatus, allowedTypes, positionSet,
					returnDescriptions);
		}
		return returnDescriptions;
	}

	public List<I_ImageTuple> getImageTuples(I_IntSet allowedStatus, I_IntSet allowedTypes, Set<I_Position> positions) throws IOException {
      List<I_ImageTuple> returnTuples = new ArrayList<I_ImageTuple>();
      for (I_ImageVersioned img : getImages()) {
         img.addTuples(allowedStatus, allowedTypes, positions,
               returnTuples);
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
			return new ArrayList<I_DescriptionVersioned>();
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
			List<I_DescriptionVersioned> localDesc = getDescriptions();
			I_DescriptionVersioned tdv = localDesc.get(0);
			List<I_DescriptionPart> versions = tdv.getVersions();
			I_DescriptionPart first = versions.get(0);
			return first.getText();
		} catch (IndexOutOfBoundsException e) {
			try {
				List<I_DescriptionVersioned> localDesc = getUncommittedDescriptions();
				I_DescriptionVersioned tdv = localDesc.get(0);
				List<I_DescriptionPart> versions = tdv.getVersions();
				I_DescriptionPart first = versions.get(0);
				return first.getText();
			} catch (IndexOutOfBoundsException e2) {
				return "No desc for: " + conceptId;
			}
		}
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
							"Retrieved images: " + images + " for: " + conceptId);
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.dwfa.vodb.types.I_Transact#commit(int, java.util.Set)
	 */
	public void commit(int version, Set<TimePathId> values) throws IOException {
		// handle the parts first...
		if (AceLog.getEditLog().isLoggable(Level.FINE)) {
			AceLog.getEditLog().fine("Starting commit for ConceptBean: " + this);
		}
		StringBuffer buff = null;
		if (AceLog.getEditLog().isLoggable(Level.FINE)) {
			buff = new StringBuffer();
		}
		try {
			if (images != null) {
				for (I_ImageVersioned image : images) {
					boolean changed = false;
					for (I_ImagePart p : image.getVersions()) {
						if (p.getVersion() == Integer.MAX_VALUE) {
							p.setVersion(version);
							values.add(new TimePathId(version, p.getPathId()));
							changed = true;
							if (buff != null) {
								buff.append("\n  Committing: " + p);
							}
						}
					}
					if (changed) {
						AceConfig.getVodb().writeImage(image);
					}
				}
			}
			if (conceptAttributes != null) {
				for (I_ConceptAttributePart p : conceptAttributes.getVersions()) {
					boolean changed = false;
					if (p.getVersion() == Integer.MAX_VALUE) {
						p.setVersion(version);
						values.add(new TimePathId(version, p.getPathId()));
						changed = true;
						if (buff != null) {
							buff.append("\n  Committing: " + p);
						}
					}
					if (changed) {
						AceConfig.getVodb().writeConceptAttributes(conceptAttributes);
					}
				}
			}
			if (descriptions != null) {
				for (I_DescriptionVersioned desc : descriptions) {
					boolean changed = false;
					for (I_DescriptionPart p : desc.getVersions()) {
						if (p.getVersion() == Integer.MAX_VALUE) {
							p.setVersion(version);
							values.add(new TimePathId(version, p.getPathId()));
							changed = true;
							if (buff != null) {
								buff.append("\n  Committing: " + p);
							}
						}
					}
					if (changed) {
						AceConfig.getVodb().writeDescription(desc);
					}
				}
			}
			if (sourceRels != null) {
				flushDestRelsOnTargetBeans(version, values);
			}
			destRels = null;

			if (uncommittedImages != null) {
				for (I_ImageVersioned image : uncommittedImages) {
					for (I_ImagePart p : image.getVersions()) {
						if (p.getVersion() == Integer.MAX_VALUE) {
							p.setVersion(version);
							values.add(new TimePathId(version, p.getPathId()));
						}
					}
					AceConfig.getVodb().writeImage(image);
					if (buff != null) {
						buff.append("\n  Committing: " + image);
					}
				}
				uncommittedImages = null;
				images = null;
			}
			if (uncommittedConceptAttributes != null) {
				for (I_ConceptAttributePart p : uncommittedConceptAttributes
						.getVersions()) {
					if (p.getVersion() == Integer.MAX_VALUE) {
						p.setVersion(version);
						values.add(new TimePathId(version, p.getPathId()));
						if (buff != null) {
							buff.append("\n  Committing: " + p);
						}
					}
				}
				AceConfig.getVodb().writeConceptAttributes(uncommittedConceptAttributes);
				uncommittedConceptAttributes = null;
				conceptAttributes = null;
			}
			if (uncommittedDescriptions != null) {
				for (I_DescriptionVersioned desc : uncommittedDescriptions) {
					for (I_DescriptionPart p : desc.getVersions()) {
						if (p.getVersion() == Integer.MAX_VALUE) {
							p.setVersion(version);
							values.add(new TimePathId(version, p.getPathId()));
						}
					}
					AceConfig.getVodb().writeDescription(desc);
					if (buff != null) {
						buff.append("\n  Committing: " + desc);
					}
				}
				uncommittedDescriptions = null;
				descriptions = null;
			}
			if (uncommittedSourceRels != null) {
				for (I_RelVersioned rel : uncommittedSourceRels) {
					ConceptBean destBean = ConceptBean.get(rel.getC2Id());
					destBean.flushDestRels();
					for (I_RelPart p : rel.getVersions()) {
						if (p.getVersion() == Integer.MAX_VALUE) {
							p.setVersion(version);
							values.add(new TimePathId(version, p.getPathId()));
						}
					}
					AceConfig.getVodb().writeRel(rel);
					if (buff != null) {
						buff.append("\n  Committing: " + rel);
					}
				}
				uncommittedSourceRels = null;
				sourceRels = null;
			}
			if (uncommittedIds != null) {
				for (int id : uncommittedIds.getSetValues()) {
					I_IdVersioned idv = AceConfig.getVodb().getId(id);
					for (I_IdPart p : idv.getVersions()) {
						if (p.getVersion() == Integer.MAX_VALUE) {
							p.setVersion(version);
							values.add(new TimePathId(version, p.getPathId()));
						}
					}
					AceConfig.getVodb().writeId(idv);
					if (buff != null) {
						buff.append("\n  Committing: " + idv);
					}
				}
            id = null;
			}
			setPrimordial(false);
		} catch (DatabaseException e) {
			throw new ToIoException(e);
		}
		if (AceLog.getAppLog().isLoggable(Level.FINE)) {
			AceLog.getAppLog().fine("Finished commit for ConceptBean: " + this);
		}
		if (AceLog.getEditLog().isLoggable(Level.FINE)) {
			AceLog.getEditLog().fine(buff.toString());
		}
	}

	private void flushDestRelsOnTargetBeans(int version, Set<TimePathId> values)
			throws DatabaseException, IOException {
		for (I_RelVersioned rel : getSourceRels()) {
			boolean changed = false;
			for (I_RelPart p : rel.getVersions()) {
				if (p.getVersion() == Integer.MAX_VALUE) {
					ConceptBean destBean = ConceptBean.get(rel.getC2Id());
					destBean.flushDestRels();
					p.setVersion(version);
					values.add(new TimePathId(version, p.getPathId()));
					changed = true;
				}
			}
			if (changed) {
				AceConfig.getVodb().writeRel(rel);
			}
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
		conceptAttributes = null;
		uncommittedConceptAttributes = null;
		descriptions = null;
		uncommittedDescriptions = null;
		sourceRels = null;
		uncommittedSourceRels = null;
		images = null;
		uncommittedImages = null;
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
		Set<I_DescriptionTuple> commonTuples = null;
		for (I_Position p : config.getViewPositionSet()) {
			Set<I_Position> positionSet = new HashSet<I_Position>();
			positionSet.add(p);
			List<I_DescriptionTuple> tuplesForPosition = getDescriptionTuples(
					config.getAllowedStatus(), null, positionSet);
			if (commonTuples == null) {
				commonTuples = new HashSet<I_DescriptionTuple>();
				commonTuples.addAll(tuplesForPosition);
			} else {
				commonTuples.retainAll(tuplesForPosition);
			}
		}
		if (commonTuples == null) {
			commonTuples = new HashSet<I_DescriptionTuple>();
		}
		return commonTuples;
	}

	public Set<I_RelTuple> getCommonRelTuples(I_ConfigAceFrame config)
			throws IOException {
		Set<I_RelTuple> commonTuples = null;
		for (I_Position p : config.getViewPositionSet()) {
			Set<I_Position> positionSet = new HashSet<I_Position>();
			positionSet.add(p);
			List<I_RelTuple> tuplesForPosition = getSourceRelTuples(config
					.getAllowedStatus(), null, positionSet, false);
			if (commonTuples == null) {
				commonTuples = new HashSet<I_RelTuple>();
				commonTuples.addAll(tuplesForPosition);
			} else {
				commonTuples.retainAll(tuplesForPosition);
			}
		}
		if (commonTuples == null) {
			commonTuples = new HashSet<I_RelTuple>();
		}
		return commonTuples;
	}

	public Set<I_ConceptAttributeTuple> getCommonConceptAttributeTuples(
			I_ConfigAceFrame config) throws IOException {
		Set<I_ConceptAttributeTuple> commonTuples = null;
		for (I_Position p : config.getViewPositionSet()) {
			Set<I_Position> positionSet = new HashSet<I_Position>();
			positionSet.add(p);
			List<I_ConceptAttributeTuple> tuplesForPosition = getConceptAttributeTuples(
					config.getAllowedStatus(), positionSet);
			if (commonTuples == null) {
				commonTuples = new HashSet<I_ConceptAttributeTuple>();
				commonTuples.addAll(tuplesForPosition);
			} else {
				commonTuples.retainAll(tuplesForPosition);
			}
		}
		if (commonTuples == null) {
			commonTuples = new HashSet<I_ConceptAttributeTuple>();
		}
		return commonTuples;
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
		Collection<I_DescriptionTuple> descriptions = getDescriptionTuples(
				config.getAllowedStatus(), config.getDescTypes(), config
						.getViewPositionSet());
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
			for (I_ImageVersioned image : uncommittedImages) {
				for (I_ImagePart p : image.getVersions()) {
					if (p.getVersion() == Integer.MAX_VALUE) {
						return true;
					}
				}
			}
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
			for (I_DescriptionVersioned desc : uncommittedDescriptions) {
				for (I_DescriptionPart p : desc.getVersions()) {
					if (p.getVersion() == Integer.MAX_VALUE) {
						return true;
					}
				}
			}
		}
		if (uncommittedSourceRels != null) {
			for (I_RelVersioned rel : uncommittedSourceRels) {
				for (I_RelPart p : rel.getVersions()) {
					if (p.getVersion() == Integer.MAX_VALUE) {
						return true;
					}
				}
			}
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

	private void flushDestRels() {
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

	public boolean isParentOf(I_GetConceptData child, I_IntSet allowedStatus,
			I_IntSet allowedTypes, Set<I_Position> positions,
			boolean addUncommitted) throws IOException {
		Set parents = child.getSourceRelTargets(allowedStatus, allowedTypes, positions, addUncommitted);
		if (parents.contains(this)) {
			return true;
		}
		for (I_GetConceptData childParent: child.getSourceRelTargets(allowedStatus, allowedTypes, positions, addUncommitted)) {
			if (this.isParentOf(childParent, allowedStatus, allowedTypes, positions, addUncommitted)) {
				return true;
			}
		}
		return false;
	}

}
