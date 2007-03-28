package org.dwfa.vodb.types;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.WeakHashMap;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.dwfa.ace.IntList;
import org.dwfa.ace.IntSet;
import org.dwfa.ace.TermLabelMaker;
import org.dwfa.ace.config.AceConfig;
import org.dwfa.ace.config.AceFrameConfig;
import org.dwfa.ace.gui.concept.ConflictPanel.ConflictColors;
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

	private ThinIdVersioned id;

	private ThinConVersioned concept;

	private List<ThinDescVersioned> descriptions;

	private List<ThinRelVersioned> sourceRels;

	private List<ThinRelVersioned> destRels;

	private List<ThinImageVersioned> images;

	private List<ThinImageVersioned> uncommittedImages;

	private List<ThinRelVersioned> uncommittedSourceRels;

	private List<ThinDescVersioned> uncommittedDescriptions;

	private ThinConVersioned uncommittedConcept;

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
	public ThinConVersioned getConcept() throws DatabaseException {
		if (concept == null) {
			concept = AceConfig.vodb.getConcept(conceptId);
		}
		return concept;
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
	public List<ThinConTuple> getConceptTuples(IntSet allowedStatus,
			Set<Position> positions) {
		throw new UnsupportedOperationException();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.dwfa.vodb.types.I_GetConceptData#getDescriptionTuples(org.dwfa.ace.IntSet,
	 *      org.dwfa.ace.IntSet, java.util.Set)
	 */
	public List<ThinDescTuple> getDescriptionTuples(IntSet allowedStatus,
			IntSet allowedTypes, Set<Position> positions)
			throws DatabaseException {
		List<ThinDescTuple> returnRels = new ArrayList<ThinDescTuple>();
		for (ThinDescVersioned desc : getDescriptions()) {
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
	public List<ThinRelTuple> getSourceRelTuples(IntSet allowedStatus,
			IntSet allowedTypes, Set<Position> positions, boolean addUncommitted)
			throws DatabaseException {
		List<ThinRelTuple> returnRels = new ArrayList<ThinRelTuple>();
		for (ThinRelVersioned rel : getSourceRels()) {
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
	public List<ThinRelTuple> getDestRelTuples(IntSet allowedStatus,
			IntSet allowedTypes, Set<Position> positions, boolean addUncommitted)
			throws DatabaseException {
		List<ThinRelTuple> returnRels = new ArrayList<ThinRelTuple>();
		for (ThinRelVersioned rel : getDestRels()) {
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
	public List<ThinDescVersioned> getDescriptions() throws DatabaseException {
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
	public List<ThinRelVersioned> getDestRels() throws DatabaseException {
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
	public List<ThinRelVersioned> getSourceRels() throws DatabaseException {
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
			List<ThinDescVersioned> localDesc = getDescriptions();
			ThinDescVersioned tdv = localDesc.get(0);
			List<ThinDescPart> versions = tdv.getVersions();
			ThinDescPart first = versions.get(0);
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
			List<ThinRelTuple> returnRels = new ArrayList<ThinRelTuple>();
			if (destRels != null) {
				for (ThinRelVersioned rel : destRels) {
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
			List<ThinRelTuple> returnRels = new ArrayList<ThinRelTuple>();
			if (sourceRels != null) {
				for (ThinRelVersioned rel : sourceRels) {
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
	public List<ThinImageVersioned> getImages() throws DatabaseException {
		if (images == null) {
			images = AceConfig.vodb.getImages(conceptId);
		}
		return images;
	}

	public List<ThinImageVersioned> getUncommittedImages() {
		if (uncommittedImages == null) {
			uncommittedImages = new ArrayList<ThinImageVersioned>();
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
			for (ThinImageVersioned image : images) {
				boolean changed = false;
				for (ThinImagePart p : image.getVersions()) {
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
		if (concept != null) {
			for (ThinConPart p : concept.getVersions()) {
				boolean changed = false;
				if (p.getVersion() == Integer.MAX_VALUE) {
					p.setVersion(version);
					values.add(new TimePathId(version, p.getPathId()));
					changed = true;
				}
				if (changed) {
					AceConfig.vodb.writeConcept(concept);
				}
			}
		}
		if (descriptions != null) {
			for (ThinDescVersioned desc : descriptions) {
				boolean changed = false;
				for (ThinDescPart p : desc.getVersions()) {
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
			for (ThinRelVersioned rel : sourceRels) {
				boolean changed = false;
				for (ThinRelPart p : rel.getVersions()) {
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
			for (ThinImageVersioned image : uncommittedImages) {
				for (ThinImagePart p : image.getVersions()) {
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
		if (uncommittedConcept != null) {
			for (ThinConPart p : uncommittedConcept.getVersions()) {
				if (p.getVersion() == Integer.MAX_VALUE) {
					p.setVersion(version);
					values.add(new TimePathId(version, p.getPathId()));
				}
			}
			AceConfig.vodb.writeConcept(uncommittedConcept);
			uncommittedConcept = null;
			concept = null;
		}
		if (uncommittedDescriptions != null) {
			for (ThinDescVersioned desc : uncommittedDescriptions) {
				for (ThinDescPart p : desc.getVersions()) {
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
			for (ThinRelVersioned rel : uncommittedSourceRels) {
				for (ThinRelPart p : rel.getVersions()) {
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
				ThinIdVersioned idv = AceConfig.vodb.getId(id);
				for (ThinIdPart p : idv.getVersions()) {
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
		uncommittedConcept = null;
		uncommittedDescriptions = null;
		uncommittedSourceRels = null;
		uncommittedImages = null;

		if (uncommittedIds != null) {
			boolean delete = true;
			for (int id : uncommittedIds.getSetValues()) {
				ThinIdVersioned idv = AceConfig.vodb.getId(id);
				for (ListIterator<ThinIdPart> itr = idv.getVersions()
						.listIterator(); itr.hasNext();) {
					ThinIdPart p = itr.next();
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
		for (ListIterator<ThinConPart> partItr = concept.getVersions()
				.listIterator(); partItr.hasNext();) {
			ThinConPart part = partItr.next();
			if (part.getVersion() == Integer.MAX_VALUE) {
				partItr.remove();
			}
		}

		for (ThinDescVersioned desc : descriptions) {
			for (ListIterator<ThinDescPart> partItr = desc.getVersions()
					.listIterator(); partItr.hasNext();) {
				ThinDescPart part = partItr.next();
				if (part.getVersion() == Integer.MAX_VALUE) {
					partItr.remove();
				}
			}
		}

		for (ThinRelVersioned srcRel : sourceRels) {
			for (ListIterator<ThinRelPart> partItr = srcRel.getVersions()
					.listIterator(); partItr.hasNext();) {
				ThinRelPart part = partItr.next();
				if (part.getVersion() == Integer.MAX_VALUE) {
					partItr.remove();
				}
			}
		}

		for (ThinRelVersioned srcRel : destRels) {
			for (ListIterator<ThinRelPart> partItr = srcRel.getVersions()
					.listIterator(); partItr.hasNext();) {
				ThinRelPart part = partItr.next();
				if (part.getVersion() == Integer.MAX_VALUE) {
					partItr.remove();
				}
			}
		}

		for (ThinImageVersioned img : images) {
			for (ListIterator<ThinImagePart> partItr = img.getVersions()
					.listIterator(); partItr.hasNext();) {
				ThinImagePart part = partItr.next();
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

	public ThinConVersioned getUncommittedConcept() {
		return uncommittedConcept;
	}

	public void setUncommittedConcept(ThinConVersioned uncommittedConcept) {
		this.uncommittedConcept = uncommittedConcept;
	}

	public List<ThinDescVersioned> getUncommittedDescriptions() {
		if (uncommittedDescriptions == null) {
			uncommittedDescriptions = new ArrayList<ThinDescVersioned>();
		}
		return uncommittedDescriptions;
	}

	public List<ThinRelVersioned> getUncommittedSourceRels() {
		if (uncommittedSourceRels == null) {
			uncommittedSourceRels = new ArrayList<ThinRelVersioned>();
		}
		return uncommittedSourceRels;
	}

	public ThinRelVersioned getSourceRel(int id) throws DatabaseException {
		for (ThinRelVersioned r : getSourceRels()) {
			if (r.getRelId() == id) {
				return r;
			}
		}
		return null;
	}

	public ThinRelVersioned getDestRel(int id) throws DatabaseException {
		for (ThinRelVersioned r : getDestRels()) {
			if (r.getRelId() == id) {
				return r;
			}
		}
		return null;
	}

	public List<JLabel> getCommonLabels(boolean showLongForm,
			boolean showStatus, AceFrameConfig config) throws DatabaseException {
		List<JLabel> labelList = new ArrayList<JLabel>();
		Set<ThinDescTuple> commonDescTuples = getCommonDescTuples(config);

		if (commonDescTuples != null) {
			for (ThinDescTuple t : commonDescTuples) {
				JLabel descLabel = TermLabelMaker.newLabel(t, showLongForm,
						showStatus);
				setBorder(descLabel, null);
				labelList.add(descLabel);
			}
		}
		Set<ThinRelTuple> commonRelTuples = getCommonRelTuples(config);
		if (commonRelTuples != null) {
			for (ThinRelTuple t : commonRelTuples) {
				JLabel relLabel = TermLabelMaker.newLabel(t, showLongForm,
						showStatus);
				setBorder(relLabel, null);
				labelList.add(relLabel);
			}
		}

		return labelList;
	}

	private Set<ThinDescTuple> getCommonDescTuples(AceFrameConfig config)
			throws DatabaseException {
		Set<ThinDescTuple> commonTuples = null;
		for (Position p : config.getViewPositionSet()) {
			Set<Position> positionSet = new HashSet<Position>();
			positionSet.add(p);
			List<ThinDescTuple> tuplesForPosition = getDescriptionTuples(config
					.getAllowedStatus(), null, positionSet);
			if (commonTuples == null) {
				commonTuples = new HashSet<ThinDescTuple>();
				commonTuples.addAll(tuplesForPosition);
			} else {
				commonTuples.retainAll(tuplesForPosition);
			}
		}
		if (commonTuples == null) {
			commonTuples = new HashSet<ThinDescTuple>();
		}
		return commonTuples;
	}

	private Set<ThinRelTuple> getCommonRelTuples(AceFrameConfig config)
			throws DatabaseException {
		Set<ThinRelTuple> commonTuples = null;
		for (Position p : config.getViewPositionSet()) {
			Set<Position> positionSet = new HashSet<Position>();
			positionSet.add(p);
			List<ThinRelTuple> tuplesForPosition = getSourceRelTuples(config
					.getAllowedStatus(), null, positionSet, false);
			if (commonTuples == null) {
				commonTuples = new HashSet<ThinRelTuple>();
				commonTuples.addAll(tuplesForPosition);
			} else {
				commonTuples.retainAll(tuplesForPosition);
			}
		}
		if (commonTuples == null) {
			commonTuples = new HashSet<ThinRelTuple>();
		}
		return commonTuples;
	}

	public Collection<JLabel> getConflictingLabels(boolean showLongForm,
			boolean showStatus, AceFrameConfig config, ConflictColors colors,
			Map<ThinDescTuple, Color> descColorMap,
			Map<ThinRelTuple, Color> relColorMap) throws DatabaseException {
		Set<ThinDescTuple> allDescTuples = new HashSet<ThinDescTuple>();
		Set<ThinRelTuple> allRelTuples = new HashSet<ThinRelTuple>();
		for (Position p : config.getViewPositionSet()) {
			Set<Position> positionSet = new HashSet<Position>();
			positionSet.add(p);
			List<ThinDescTuple> descTuplesForPosition = getDescriptionTuples(
					config.getAllowedStatus(), null, positionSet);
			allDescTuples.addAll(descTuplesForPosition);
			List<ThinRelTuple> relTuplesForPosition = getSourceRelTuples(config
					.getAllowedStatus(), null, positionSet, false);
			allRelTuples.addAll(relTuplesForPosition);
		}
		Set<ThinDescTuple> commonDescTuples = getCommonDescTuples(config);
		allDescTuples.removeAll(commonDescTuples);

		Set<ThinRelTuple> commonRelTuples = getCommonRelTuples(config);
		allRelTuples.removeAll(commonRelTuples);

		Collection<JLabel> labelList = new ArrayList<JLabel>(allDescTuples
				.size());

		for (ThinDescTuple t : allDescTuples) {
			JLabel descLabel = TermLabelMaker.newLabel(t, showLongForm,
					showStatus);
			Color conflictColor = colors.getColor();
			descColorMap.put(t, conflictColor);
			setBorder(descLabel, conflictColor);
			labelList.add(descLabel);
		}
		for (ThinRelTuple t : allRelTuples) {
			JLabel relLabel = TermLabelMaker.newLabel(t, showLongForm,
					showStatus);
			Color conflictColor = colors.getColor();
			relColorMap.put(t, conflictColor);
			setBorder(relLabel, conflictColor);
			labelList.add(relLabel);
		}

		return labelList;
	}

	public JPanel getVersionView(Position p, AceFrameConfig config,
			Map<ThinDescTuple, Color> desColorMap,
			Map<ThinRelTuple, Color> relColorMap) throws DatabaseException {
		JPanel versionView = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.NONE;
		c.anchor = GridBagConstraints.NORTHWEST;
		c.weightx = 0;
		c.weighty = 0;
		c.gridx = 0;
		c.gridy = 0;
		Set<Position> posSet = new HashSet<Position>(1);
		posSet.add(p);
		List<ThinDescTuple> descList = getDescriptionTuples(config
				.getAllowedStatus(), null, posSet);
		for (ThinDescTuple t : descList) {
			JLabel tLabel = TermLabelMaker.newLabel(t, false, false);
			Color conflictColor = desColorMap.get(t);
			setBorder(tLabel, conflictColor);
			versionView.add(tLabel, c);
			c.gridy++;
		}
		List<ThinRelTuple> relList = getSourceRelTuples(config
				.getAllowedStatus(), null, posSet, false);
		for (ThinRelTuple t : relList) {
			JLabel tLabel = TermLabelMaker.newLabel(t, false, false);
			Color conflictColor = relColorMap.get(t);
			setBorder(tLabel, conflictColor);
			versionView.add(tLabel, c);
			c.gridy++;
		}
		c.weighty = 1.0;
		versionView.add(new JPanel(), c);
		versionView.setBorder(BorderFactory.createTitledBorder(p.toString()));
		return versionView;
	}

	private void setBorder(JLabel tLabel, Color conflictColor) {
		if (conflictColor == null) {
			conflictColor = Color.white;
		}
		Dimension size = tLabel.getSize();
		tLabel.setBorder(BorderFactory.createCompoundBorder(BorderFactory
				.createRaisedBevelBorder(), BorderFactory.createCompoundBorder(
				BorderFactory.createMatteBorder(1, 5, 1, 5, conflictColor),
				BorderFactory.createEmptyBorder(1, 3, 1, 3))));
		size.width = size.width + 18;
		size.height = size.height + 6;
		tLabel.setSize(size);
		tLabel.setPreferredSize(size);
		tLabel.setMaximumSize(size);
		tLabel.setMinimumSize(size);
	}

	public ThinIdVersioned getId() throws DatabaseException {
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

	public ThinDescTuple getDescTuple(IntList prefOrder, AceFrameConfig config)
			throws DatabaseException {
		Collection<ThinDescTuple> descriptions = getDescriptionTuples(config
				.getAllowedStatus(), config.getDescTypes(), config
				.getViewPositionSet());
		if (prefOrder == null) {
			return descriptions.iterator().next();
		} else {
			for (int typeId : prefOrder.getListValues()) {
				for (ThinDescTuple d : descriptions) {
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
			for (ThinImageVersioned image : images) {
				for (ThinImagePart p : image.getVersions()) {
					if (p.getVersion() == Integer.MAX_VALUE) {
						return true;
					}
				}
			}
		}
		if (concept != null) {
			for (ThinConPart p : concept.getVersions()) {
				if (p.getVersion() == Integer.MAX_VALUE) {
					return true;
				}
			}
		}
		if (descriptions != null) {
			for (ThinDescVersioned desc : descriptions) {
				for (ThinDescPart p : desc.getVersions()) {
					if (p.getVersion() == Integer.MAX_VALUE) {
						return true;
					}
				}
			}
		}
		if (sourceRels != null) {
			for (ThinRelVersioned rel : sourceRels) {
				for (ThinRelPart p : rel.getVersions()) {
					if (p.getVersion() == Integer.MAX_VALUE) {
						return true;
					}
				}
			}
		}
		if (uncommittedImages != null) {
			for (ThinImageVersioned image : uncommittedImages) {
				for (ThinImagePart p : image.getVersions()) {
					if (p.getVersion() == Integer.MAX_VALUE) {
						return true;
					}
				}
			}
		}
		if (uncommittedConcept != null) {
			for (ThinConPart p : uncommittedConcept.getVersions()) {
				if (p.getVersion() == Integer.MAX_VALUE) {
					return true;
				}
			}
		}
		if (uncommittedDescriptions != null) {
			for (ThinDescVersioned desc : uncommittedDescriptions) {
				for (ThinDescPart p : desc.getVersions()) {
					if (p.getVersion() == Integer.MAX_VALUE) {
						return true;
					}
				}
			}
		}
		if (uncommittedSourceRels != null) {
			for (ThinRelVersioned rel : uncommittedSourceRels) {
				for (ThinRelPart p : rel.getVersions()) {
					if (p.getVersion() == Integer.MAX_VALUE) {
						return true;
					}
				}
			}
		}
		if (uncommittedIds != null) {
			for (int id : uncommittedIds.getSetValues()) {
				ThinIdVersioned idv = AceConfig.vodb.getId(id);
				for (ThinIdPart p : idv.getVersions()) {
					if (p.getVersion() == Integer.MAX_VALUE) {
						return true;
					}
				}
			}
		}
		return false;
	}
}
