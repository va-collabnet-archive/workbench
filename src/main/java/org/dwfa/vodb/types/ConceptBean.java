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

import org.dwfa.ace.IntList;
import org.dwfa.ace.IntSet;
import org.dwfa.ace.api.I_AmTermComponent;
import org.dwfa.ace.api.I_ConceptAttributePart;
import org.dwfa.ace.api.I_ConceptAttributeTuple;
import org.dwfa.ace.api.I_ConceptAttributeVersioned;
import org.dwfa.ace.api.I_DescriptionPart;
import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_DescriptionVersioned;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IdPart;
import org.dwfa.ace.api.I_IdVersioned;
import org.dwfa.ace.api.I_ImagePart;
import org.dwfa.ace.api.I_ImageVersioned;
import org.dwfa.ace.api.I_RelPart;
import org.dwfa.ace.api.I_RelTuple;
import org.dwfa.ace.api.I_RelVersioned;
import org.dwfa.ace.config.AceConfig;
import org.dwfa.ace.config.AceFrameConfig;
import org.dwfa.tapi.TerminologyException;

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
		return get(AceConfig.vodb.uuidToNative(uid));
	}

	public static ConceptBean get(Collection<UUID> uids)
			throws TerminologyException, IOException {
		return get(AceConfig.vodb.uuidToNative(uids));
	}

	private int conceptId;

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

	private IntSet uncommittedIds;

	private List<UUID> uids;

	private ConceptBean(int conceptId) {
		super();
		this.conceptId = conceptId;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.dwfa.vodb.types.I_GetConceptData#getConcept()
	 */
	public I_ConceptAttributeVersioned getConceptAttributes() throws DatabaseException {
		if (conceptAttributes == null) {
			conceptAttributes = AceConfig.vodb.getConcept(conceptId);
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
	public List<I_ConceptAttributeTuple> getConceptTuples(IntSet allowedStatus,
			Set<Position> positions) throws DatabaseException {
		List<I_ConceptAttributeTuple> returnTuples = new ArrayList<I_ConceptAttributeTuple>();
		getConceptAttributes().addTuples(allowedStatus, positions, returnTuples);
		return returnTuples;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.dwfa.vodb.types.I_GetConceptData#getDescriptionTuples(org.dwfa.ace.IntSet,
	 *      org.dwfa.ace.IntSet, java.util.Set)
	 */
	public List<I_DescriptionTuple> getDescriptionTuples(IntSet allowedStatus,
			IntSet allowedTypes, Set<Position> positions)
			throws DatabaseException {
		List<I_DescriptionTuple> returnRels = new ArrayList<I_DescriptionTuple>();
		for (I_DescriptionVersioned desc : getDescriptions()) {
			desc.addTuples(allowedStatus, allowedTypes, positions, returnRels);
		}
		return returnRels;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.dwfa.vodb.types.I_GetConceptData#getSourceRelTuples(org.dwfa.ace.IntSet,
	 *      org.dwfa.ace.IntSet, java.util.Set)
	 */
	public List<I_RelTuple> getSourceRelTuples(IntSet allowedStatus,
			IntSet allowedTypes, Set<Position> positions, boolean addUncommitted)
			throws DatabaseException {
		List<I_RelTuple> returnRels = new ArrayList<I_RelTuple>();
		for (I_RelVersioned rel : getSourceRels()) {
			rel.addTuples(allowedStatus, allowedTypes, positions, returnRels,
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
	public List<I_RelTuple> getDestRelTuples(IntSet allowedStatus,
			IntSet allowedTypes, Set<Position> positions, boolean addUncommitted)
			throws DatabaseException {
		List<I_RelTuple> returnRels = new ArrayList<I_RelTuple>();
		for (I_RelVersioned rel : getDestRels()) {
			/*
			 * if ((conceptId == -2147444184) && (rel.getC1Id() == -2147326003)) {
			 * System.out.println("getSourceRelTuples for SNOMED CT Concept"); }
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
	public List<I_DescriptionVersioned> getDescriptions() throws DatabaseException {
		if (descriptions == null) {
			descriptions = AceConfig.vodb.getDescriptions(conceptId);
		}
		return descriptions;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.dwfa.vodb.types.I_GetConceptData#getDestRels()
	 */
	public List<I_RelVersioned> getDestRels() throws DatabaseException {
		if (destRels == null) {
			destRels = AceConfig.vodb.getDestRels(conceptId);
		}
		return destRels;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.dwfa.vodb.types.I_GetConceptData#getSourceRels()
	 */
	public List<I_RelVersioned> getSourceRels() throws DatabaseException {
		if (sourceRels == null) {
			sourceRels = AceConfig.vodb.getSrcRels(conceptId);
		}
		return sourceRels;
	}

	public String toString() {
		try {
			return getInitialText();
		} catch (DatabaseException e) {
			e.printStackTrace();
			return e.toString();
		} catch (IndexOutOfBoundsException e) {
			return "No desc for: " + conceptId;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.dwfa.vodb.types.I_GetConceptData#getInitialText()
	 */
	public String getInitialText() throws DatabaseException {
		try {
			List<I_DescriptionVersioned> localDesc = getDescriptions();
			I_DescriptionVersioned tdv = localDesc.get(0);
			List<I_DescriptionPart> versions = tdv.getVersions();
			I_DescriptionPart first = versions.get(0);
			return first.getText();
		} catch (IndexOutOfBoundsException e) {
			return "No desc for: " + conceptId;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.dwfa.vodb.types.I_GetConceptData#isLeaf(org.dwfa.ace.config.AceFrameConfig)
	 */
	public boolean isLeaf(AceFrameConfig aceConfig, boolean addUncommitted)
			throws DatabaseException {
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
				if (AceConfig.vodb.hasDestRels(conceptId)) {
					return false;
				}
			}
			if (sourceRels != null) {
				if (sourceRels.size() > 0) {
					return false;
				}
			} else {
				if (AceConfig.vodb.hasSrcRels(conceptId)) {
					return false;
				}
			}
		}
		return true;
	}

	private boolean hasDestRelTuples(IntSet allowedStatus, IntSet destRelTypes,
			Set<Position> positions, boolean addUncommitted)
			throws DatabaseException {
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
				return AceConfig.vodb.hasDestRelTuple(conceptId, allowedStatus,
						destRelTypes, positions);
			}
		}
		return false;
	}

	private boolean hasSourceRelTuples(IntSet allowedStatus,
			IntSet sourceRelTypes, Set<Position> positions,
			boolean addUncommitted) throws DatabaseException {
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
				return AceConfig.vodb.hasSrcRelTuple(conceptId, allowedStatus,
						sourceRelTypes, positions);
			}
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.dwfa.vodb.types.I_GetConceptData#getImages()
	 */
	public List<I_ImageVersioned> getImages() throws DatabaseException {
		if (images == null) {
			images = AceConfig.vodb.getImages(conceptId);
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
	public void commit(int version, Set<TimePathId> values)
			throws DatabaseException {
		// handle the parts first...
		if (images != null) {
			for (I_ImageVersioned image : images) {
				boolean changed = false;
				for (I_ImagePart p : image.getVersions()) {
					if (p.getVersion() == Integer.MAX_VALUE) {
						p.setVersion(version);
						values.add(new TimePathId(version, p.getPathId()));
						changed = true;
					}
				}
				if (changed) {
					AceConfig.vodb.writeImage(image);
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
				}
				if (changed) {
					AceConfig.vodb.writeConcept(conceptAttributes);
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
					}
				}
				if (changed) {
					AceConfig.vodb.writeDescription(desc);
				}
			}
		}
		if (sourceRels != null) {
			for (I_RelVersioned rel : sourceRels) {
				boolean changed = false;
				for (I_RelPart p : rel.getVersions()) {
					if (p.getVersion() == Integer.MAX_VALUE) {
						p.setVersion(version);
						values.add(new TimePathId(version, p.getPathId()));
						changed = true;
					}
				}
				if (changed) {
					AceConfig.vodb.writeRel(rel);
				}
			}
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
				AceConfig.vodb.writeImage(image);
			}
			uncommittedImages = null;
			images = null;
		}
		if (uncommittedConceptAttributes != null) {
			for (I_ConceptAttributePart p : uncommittedConceptAttributes.getVersions()) {
				if (p.getVersion() == Integer.MAX_VALUE) {
					p.setVersion(version);
					values.add(new TimePathId(version, p.getPathId()));
				}
			}
			AceConfig.vodb.writeConcept(uncommittedConceptAttributes);
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
				AceConfig.vodb.writeDescription(desc);
			}
			uncommittedDescriptions = null;
			descriptions = null;
		}
		if (uncommittedSourceRels != null) {
			for (I_RelVersioned rel : uncommittedSourceRels) {
				for (I_RelPart p : rel.getVersions()) {
					if (p.getVersion() == Integer.MAX_VALUE) {
						p.setVersion(version);
						values.add(new TimePathId(version, p.getPathId()));
					}
				}
				AceConfig.vodb.writeRel(rel);
			}
			uncommittedSourceRels = null;
			sourceRels = null;
		}
		if (uncommittedIds != null) {
			for (int id : uncommittedIds.getSetValues()) {
				I_IdVersioned idv = AceConfig.vodb.getId(id);
				for (I_IdPart p : idv.getVersions()) {
					if (p.getVersion() == Integer.MAX_VALUE) {
						p.setVersion(version);
						values.add(new TimePathId(version, p.getPathId()));
					}
				}
				AceConfig.vodb.writeId(idv);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.dwfa.vodb.types.I_Transact#abort()
	 */
	public void abort() throws DatabaseException {
		// remove uncommitted brand new components...
		uncommittedConceptAttributes = null;
		uncommittedDescriptions = null;
		uncommittedSourceRels = null;
		uncommittedImages = null;

		if (uncommittedIds != null) {
			boolean delete = true;
			for (int id : uncommittedIds.getSetValues()) {
				I_IdVersioned idv = AceConfig.vodb.getId(id);
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
					AceConfig.vodb.deleteId(idv);
				} else {
					AceConfig.vodb.writeId(idv);
				}
			}
			uncommittedIds = null;
		}

		// remove uncommitted parts...
		for (ListIterator<I_ConceptAttributePart> partItr = conceptAttributes.getVersions()
				.listIterator(); partItr.hasNext();) {
			I_ConceptAttributePart part = partItr.next();
			if (part.getVersion() == Integer.MAX_VALUE) {
				partItr.remove();
			}
		}

		for (I_DescriptionVersioned desc : descriptions) {
			for (ListIterator<I_DescriptionPart> partItr = desc.getVersions()
					.listIterator(); partItr.hasNext();) {
				I_DescriptionPart part = partItr.next();
				if (part.getVersion() == Integer.MAX_VALUE) {
					partItr.remove();
				}
			}
		}

		for (I_RelVersioned srcRel : sourceRels) {
			for (ListIterator<I_RelPart> partItr = srcRel.getVersions()
					.listIterator(); partItr.hasNext();) {
				I_RelPart part = partItr.next();
				if (part.getVersion() == Integer.MAX_VALUE) {
					partItr.remove();
				}
			}
		}

		for (I_RelVersioned srcRel : destRels) {
			for (ListIterator<I_RelPart> partItr = srcRel.getVersions()
					.listIterator(); partItr.hasNext();) {
				I_RelPart part = partItr.next();
				if (part.getVersion() == Integer.MAX_VALUE) {
					partItr.remove();
				}
			}
		}

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

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.dwfa.vodb.types.I_GetConceptData#getUid()
	 */
	public List<UUID> getUids() throws DatabaseException {
		if (uids == null) {
			uids = AceConfig.vodb.nativeToUuid(conceptId);
		}
		return uids;
	}

	public static void writeConceptBeanList(ObjectOutputStream out,
			List<ConceptBean> list) throws IOException {
		out.writeInt(list.size());
		for (I_GetConceptData cb : list) {
			try {
				out.writeObject(cb.getUids());
			} catch (DatabaseException e) {
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

	public void setUncommittedConceptAttributes(I_ConceptAttributeVersioned uncommittedConcept) {
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

	public I_RelVersioned getSourceRel(int id) throws DatabaseException {
		for (I_RelVersioned r : getSourceRels()) {
			if (r.getRelId() == id) {
				return r;
			}
		}
		return null;
	}

	public I_RelVersioned getDestRel(int id) throws DatabaseException {
		for (I_RelVersioned r : getDestRels()) {
			if (r.getRelId() == id) {
				return r;
			}
		}
		return null;
	}

	public Set<I_DescriptionTuple> getCommonDescTuples(AceFrameConfig config)
			throws DatabaseException {
		Set<I_DescriptionTuple> commonTuples = null;
		for (Position p : config.getViewPositionSet()) {
			Set<Position> positionSet = new HashSet<Position>();
			positionSet.add(p);
			List<I_DescriptionTuple> tuplesForPosition = getDescriptionTuples(config
					.getAllowedStatus(), null, positionSet);
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

	public Set<I_RelTuple> getCommonRelTuples(AceFrameConfig config)
			throws DatabaseException {
		Set<I_RelTuple> commonTuples = null;
		for (Position p : config.getViewPositionSet()) {
			Set<Position> positionSet = new HashSet<Position>();
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

	public Set<I_ConceptAttributeTuple> getCommonConceptAttributeTuples(AceFrameConfig config) throws DatabaseException {
		Set<I_ConceptAttributeTuple> commonTuples = null;
		for (Position p : config.getViewPositionSet()) {
			Set<Position> positionSet = new HashSet<Position>();
			positionSet.add(p);
			List<I_ConceptAttributeTuple> tuplesForPosition = getConceptTuples(config
					.getAllowedStatus(), positionSet);
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

	public I_IdVersioned getId() throws DatabaseException {
		if (id == null) {
			id = AceConfig.vodb.getId(conceptId);
		}
		return id;
	}

	public IntSet getUncommittedIds() {
		if (uncommittedIds == null) {
			uncommittedIds = new IntSet();
		}
		return uncommittedIds;
	}

	public I_DescriptionTuple getDescTuple(IntList prefOrder, AceFrameConfig config)
			throws DatabaseException {
		Collection<I_DescriptionTuple> descriptions = getDescriptionTuples(config
				.getAllowedStatus(), config.getDescTypes(), config
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

	public boolean isUncommitted() throws DatabaseException {
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
			for (I_ConceptAttributePart p : uncommittedConceptAttributes.getVersions()) {
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
				I_IdVersioned idv = AceConfig.vodb.getId(id);
				for (I_IdPart p : idv.getVersions()) {
					if (p.getVersion() == Integer.MAX_VALUE) {
						return true;
					}
				}
			}
		}
		return false;
	}

}
