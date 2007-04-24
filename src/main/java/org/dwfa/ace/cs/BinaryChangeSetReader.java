package org.dwfa.ace.cs;

import java.io.BufferedInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

import org.dwfa.ace.ACE;
import org.dwfa.ace.AceLog;
import org.dwfa.ace.api.I_ImagePart;
import org.dwfa.ace.api.I_Path;
import org.dwfa.ace.api.I_Position;
import org.dwfa.ace.api.I_RelVersioned;
import org.dwfa.ace.api.TimePathId;
import org.dwfa.ace.api.cs.I_Count;
import org.dwfa.ace.api.cs.I_ReadChangeSet;
import org.dwfa.ace.config.AceConfig;
import org.dwfa.ace.utypes.UniversalAceBean;
import org.dwfa.ace.utypes.UniversalAceConceptAttributes;
import org.dwfa.ace.utypes.UniversalAceConceptAttributesPart;
import org.dwfa.ace.utypes.UniversalAceDescription;
import org.dwfa.ace.utypes.UniversalAceDescriptionPart;
import org.dwfa.ace.utypes.UniversalAceIdentification;
import org.dwfa.ace.utypes.UniversalAceIdentificationPart;
import org.dwfa.ace.utypes.UniversalAceImage;
import org.dwfa.ace.utypes.UniversalAceImagePart;
import org.dwfa.ace.utypes.UniversalAcePath;
import org.dwfa.ace.utypes.UniversalAcePosition;
import org.dwfa.ace.utypes.UniversalAceRelationship;
import org.dwfa.ace.utypes.UniversalAceRelationshipPart;
import org.dwfa.tapi.NoMappingException;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.vodb.ToIoException;
import org.dwfa.vodb.bind.ThinVersionHelper;
import org.dwfa.vodb.types.ConceptBean;
import org.dwfa.vodb.types.Path;
import org.dwfa.vodb.types.Position;
import org.dwfa.vodb.types.ThinConPart;
import org.dwfa.vodb.types.ThinConVersioned;
import org.dwfa.vodb.types.ThinDescPart;
import org.dwfa.vodb.types.ThinDescVersioned;
import org.dwfa.vodb.types.ThinIdPart;
import org.dwfa.vodb.types.ThinIdVersioned;
import org.dwfa.vodb.types.ThinImagePart;
import org.dwfa.vodb.types.ThinImageVersioned;
import org.dwfa.vodb.types.ThinRelPart;
import org.dwfa.vodb.types.ThinRelVersioned;

import com.sleepycat.je.DatabaseException;

public class BinaryChangeSetReader implements I_ReadChangeSet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private File changeSetFile;
	
	private I_Count counter;

	public BinaryChangeSetReader() {
		super();
	}

	public void read() throws IOException, ClassNotFoundException {
		try {
			FileInputStream fis = new FileInputStream(changeSetFile);
			BufferedInputStream bis = new BufferedInputStream(fis);
			ObjectInputStream ois = new ObjectInputStream(bis);
			Class readerClass = (Class) ois.readObject();
			if (BinaryChangeSetReader.class.isAssignableFrom(readerClass)) {
				AceLog.getEditLog().fine(
						"Now reading change set with BinaryChangeSetReader");
			} else {
				AceLog.getAppLog()
						.warning("ReaderClass "
									+ readerClass.getName()
									+ " is not assignable from BinaryChangeSetReader...");
			}

			Set<TimePathId> values = new HashSet<TimePathId>();
			try {
				while (true) {
					long time = ois.readLong();
					Object obj = ois.readObject();
					if (counter != null) {
						counter.increment();
					}
					if (UniversalAceBean.class.isAssignableFrom(obj.getClass())) {
						AceLog.getEditLog().fine("Read UniversalAceBean... " + obj);
						ACE.addImported(commitAceBean((UniversalAceBean) obj, time, values));
					} else if (UniversalAcePath.class.isAssignableFrom(obj.getClass())) {
						AceLog.getEditLog().fine("Read UniversalAcePath... " + obj);
						commitAcePath((UniversalAcePath) obj, time);
					} else {
						throw new IOException("Can't handle class: "
								+ obj.getClass().getName());
					}

				}
			} catch (EOFException ex) {

			}
			AceConfig.vodb.addTimeBranchValues(values);
		} catch (DatabaseException e) {
			throw new ToIoException(e);
		} catch (TerminologyException e) {
			throw new ToIoException(e);
		}
	}

	private void commitAcePath(UniversalAcePath path, long time)
			throws DatabaseException, TerminologyException, IOException {
		AceLog.getEditLog().fine("Importing new universal path: \n" + path);
		List<I_Position> origins = null;
		try {
			I_Path newPath = AceConfig.vodb.getPath(getNid(path.getPathId()));
			AceLog.getEditLog().fine(
					"Importing path that already exists: \n" + path + "\n\n"
							+ newPath);
		} catch (DatabaseException e) {
			if (path.getOrigins() != null) {
				origins = new ArrayList<I_Position>(path.getOrigins().size());
				for (UniversalAcePosition pos : path.getOrigins()) {
					I_Path thinPath = AceConfig.vodb.getPath(getNid(pos
							.getPathId()));
					origins.add(new Position(ThinVersionHelper.convert(pos
							.getTime()), thinPath));
				}
			}
			Path newPath = new Path(getNid(path.getPathId()), origins);
			AceLog.getEditLog().fine("writing new path: \n" + newPath);
			AceConfig.vodb.writePath(newPath);
		}

	}

	private ConceptBean commitAceBean(UniversalAceBean bean, long time, Set<TimePathId> values)
			throws IOException, ClassNotFoundException {
		try {
			// Do all the commiting...
			commitUncommittedIds(time, bean, values);
			commitUncommittedDescriptions(time, bean, values);
			commitUncommittedRelationships(time, bean, values);
			commitUncommittedConceptAttributes(time, bean, values);
			commitUncommittedImages(time, bean, values);

			commitDescriptionChanges(time, bean, values);
			commitRelationshipChanges(time, bean, values);
			commitConceptAttributeChanges(time, bean, values);
			commitImageChanges(time, bean, values);

			ConceptBean localBean = ConceptBean.get(bean.getId().getUIDs());
			localBean.flush();
			return localBean;
		} catch (DatabaseException e) {
			throw new ToIoException(e);
		} catch (TerminologyException e) {
			throw new ToIoException(e);
		}
	}

	private int getNid(Collection<UUID> uids) throws TerminologyException,
			IOException {
		return AceConfig.vodb.uuidToNative(uids);
	}

	private void commitConceptAttributeChanges(long time, UniversalAceBean bean, Set<TimePathId> values)
			throws DatabaseException, TerminologyException, IOException {
		if (bean.getConceptAttributes() != null) {
			UniversalAceConceptAttributes attributes = bean
					.getConceptAttributes();
			ThinConVersioned thinAttributes = (ThinConVersioned) AceConfig.vodb
					.getConcept(getNid(attributes.getConId()));
			boolean changed = false;
			for (UniversalAceConceptAttributesPart part : attributes
					.getVersions()) {
				if (part.getTime() == Long.MAX_VALUE) {
					changed = true;
					ThinConPart newPart = new ThinConPart();
					newPart.setPathId(getNid(part.getPathId()));
					newPart.setConceptStatus(getNid(part.getConceptStatus()));
					newPart.setDefined(part.isDefined());
					newPart.setVersion(ThinVersionHelper.convert(time));
					thinAttributes.addVersion(newPart);
					values.add(new TimePathId(newPart.getVersion(), newPart.getPathId()));
				}
			}
			if (changed) {
				AceConfig.vodb.writeConcept(thinAttributes);
				AceLog.getEditLog().fine(
						"Importing changed attributes: \n" + thinAttributes);
			}
		}
	}

	private void commitUncommittedConceptAttributes(long time,
			UniversalAceBean bean, Set<TimePathId> values) throws DatabaseException,
			TerminologyException, IOException {
		if (bean.getUncommittedConceptAttributes() != null) {
			UniversalAceConceptAttributes attributes = bean
					.getUncommittedConceptAttributes();
			ThinConVersioned thinAttributes = new ThinConVersioned(
					getNid(attributes.getConId()), attributes.versionCount());
			for (UniversalAceConceptAttributesPart part : attributes
					.getVersions()) {
				ThinConPart newPart = new ThinConPart();
				newPart.setPathId(getNid(part.getPathId()));
				newPart.setConceptStatus(getNid(part.getConceptStatus()));
				newPart.setDefined(part.isDefined());
				if (part.getTime() == Long.MAX_VALUE) {
					newPart.setVersion(ThinVersionHelper.convert(time));
				} else {
					newPart.setVersion(ThinVersionHelper
							.convert(part.getTime()));
				}
				values.add(new TimePathId(newPart.getVersion(), newPart.getPathId()));
				thinAttributes.addVersion(newPart);
			}
			try {
				ThinConVersioned oldVersioned = (ThinConVersioned) AceConfig.vodb
						.getConcept(thinAttributes.getConId());
				oldVersioned.merge(thinAttributes);
				AceLog.getEditLog().fine(
						"Merging attributes with existing (should have been null): \n"
								+ thinAttributes + "\n\n" + oldVersioned);
			} catch (DatabaseException e) {
				// expected exception...
			}
			AceConfig.vodb.writeConcept(thinAttributes);
			AceLog.getEditLog().fine("Importing attributes: \n" + thinAttributes);
		}
	}

	private void commitImageChanges(long time, UniversalAceBean bean, Set<TimePathId> values)
			throws DatabaseException, TerminologyException, IOException {
		for (UniversalAceImage image : bean.getImages()) {
			ThinImageVersioned thinImage = (ThinImageVersioned) AceConfig.vodb
					.getImage(getNid(image.getImageId()));
			boolean changed = false;
			for (UniversalAceImagePart part : image.getVersions()) {
				if (part.getTime() == Long.MAX_VALUE) {
					changed = true;
					ThinImagePart newPart = new ThinImagePart();
					newPart.setPathId(getNid(part.getPathId()));
					newPart.setStatusId(getNid(part.getStatusId()));
					newPart.setTextDescription(part.getTextDescription());
					newPart.setTypeId(getNid(part.getTypeId()));
					newPart.setVersion(ThinVersionHelper.convert(time));
					thinImage.addVersion(newPart);
					values.add(new TimePathId(newPart.getVersion(), newPart.getPathId()));
				}
			}
			if (changed) {
				AceConfig.vodb.writeImage(thinImage);
				AceLog.getEditLog().fine("Importing image changes: \n" + thinImage);
			}
		}
	}

	private void commitUncommittedImages(long time, UniversalAceBean bean, Set<TimePathId> values)
			throws DatabaseException, TerminologyException, IOException {
		for (UniversalAceImage image : bean.getUncommittedImages()) {
			ThinImageVersioned thinImage = new ThinImageVersioned(getNid(image
					.getImageId()), image.getImage(),
					new ArrayList<I_ImagePart>(), image.getFormat(),
					getNid(image.getConceptId()));
			for (UniversalAceImagePart part : image.getVersions()) {
				ThinImagePart newPart = new ThinImagePart();
				newPart.setPathId(getNid(part.getPathId()));
				newPart.setStatusId(getNid(part.getStatusId()));
				newPart.setTextDescription(part.getTextDescription());
				newPart.setTypeId(getNid(part.getTypeId()));
				if (part.getTime() == Long.MAX_VALUE) {
					newPart.setVersion(ThinVersionHelper.convert(time));
				} else {
					newPart.setVersion(ThinVersionHelper
							.convert(part.getTime()));
				}
				values.add(new TimePathId(newPart.getVersion(), newPart.getPathId()));
				thinImage.addVersion(newPart);
			}
			try {
				ThinImageVersioned oldVersioned = (ThinImageVersioned) AceConfig.vodb
						.getImage(thinImage.getImageId());
				oldVersioned.merge(thinImage);
				AceLog.getEditLog().fine(
						"Merging image with existing (should have been null): \n"
								+ thinImage + "\n\n" + oldVersioned);
			} catch (DatabaseException e) {
				// expected exception...
			}
			AceConfig.vodb.writeImage(thinImage);
			AceLog.getEditLog().fine("Importing image: \n" + thinImage);
		}
	}

	private void commitUncommittedRelationships(long time, UniversalAceBean bean, Set<TimePathId> values)
			throws DatabaseException, TerminologyException, IOException {
		for (UniversalAceRelationship rel : bean.getUncommittedSourceRels()) {
			ThinRelVersioned thinRel = new ThinRelVersioned(getNid(rel
					.getRelId()), getNid(rel.getC1Id()), getNid(rel.getC2Id()),
					rel.versionCount());
			for (UniversalAceRelationshipPart part : rel.getVersions()) {
				ThinRelPart newPart = new ThinRelPart();
				newPart.setCharacteristicId(getNid(part.getCharacteristicId()));
				newPart.setGroup(part.getGroup());
				newPart.setPathId(getNid(part.getPathId()));
				newPart.setRefinabilityId(getNid(part.getRefinabilityId()));
				newPart.setRelTypeId(getNid(part.getRelTypeId()));
				newPart.setStatusId(getNid(part.getStatusId()));
				newPart.setVersion(ThinVersionHelper.convert(part.getTime()));
				if (part.getTime() == Long.MAX_VALUE) {
					newPart.setVersion(ThinVersionHelper.convert(time));
				} else {
					newPart.setVersion(ThinVersionHelper
							.convert(part.getTime()));
				}
				values.add(new TimePathId(newPart.getVersion(), newPart.getPathId()));
				thinRel.addVersion(newPart);
			}
			try {
				ThinRelVersioned oldVersioned = (ThinRelVersioned) AceConfig.vodb
						.getRel(thinRel.getRelId());
				oldVersioned.merge(thinRel);
				AceLog.getEditLog().fine(
						"Merging rel with existing (should have been null): \n"
								+ thinRel + "\n\n" + oldVersioned);
			} catch (DatabaseException e) {
				// expected exception...
			}
			AceConfig.vodb.writeRel(thinRel);
			if (AceLog.getEditLog().isLoggable(Level.FINE)) {
				AceLog.getEditLog().fine("Importing rel: \n" + thinRel);
				List<I_RelVersioned> destRels = AceConfig.vodb.getDestRels(thinRel.getC2Id());
				if (destRels.contains(thinRel)) {
					AceLog.getEditLog().fine("found in dest rels.");
				} else {
					AceLog.getEditLog().severe("NOT found in dest rels: " + destRels);
				}
			}
		}
	}

	private void commitRelationshipChanges(long time, UniversalAceBean bean, Set<TimePathId> values)
			throws DatabaseException, TerminologyException, IOException {
		for (UniversalAceRelationship rel : bean.getSourceRels()) {
			ThinRelVersioned thinRel = (ThinRelVersioned) AceConfig.vodb
					.getRel(getNid(rel.getRelId()));
			boolean changed = false;
			for (UniversalAceRelationshipPart part : rel.getVersions()) {
				if (part.getTime() == Long.MAX_VALUE) {
					changed = true;
					ThinRelPart newPart = new ThinRelPart();
					newPart.setCharacteristicId(getNid(part
							.getCharacteristicId()));
					newPart.setGroup(part.getGroup());
					newPart.setPathId(getNid(part.getPathId()));
					newPart.setRefinabilityId(getNid(part.getRefinabilityId()));
					newPart.setRelTypeId(getNid(part.getRelTypeId()));
					newPart.setStatusId(getNid(part.getStatusId()));
					newPart.setVersion(ThinVersionHelper.convert(time));
					thinRel.addVersion(newPart);
					values.add(new TimePathId(newPart.getVersion(), newPart.getPathId()));
				}
			}
			if (changed) {
				AceConfig.vodb.writeRel(thinRel);
				AceLog.getEditLog().fine("Importing rel: \n" + thinRel);
			}
		}
	}

	private void commitDescriptionChanges(long time, UniversalAceBean bean, Set<TimePathId> values)
			throws DatabaseException, TerminologyException, IOException {
		for (UniversalAceDescription desc : bean.getDescriptions()) {
			ThinDescVersioned thinDesc = (ThinDescVersioned) AceConfig.vodb
					.getDescription(getNid(desc.getDescId()));
			boolean changed = false;
			for (UniversalAceDescriptionPart part : desc.getVersions()) {
				if (part.getTime() == Long.MAX_VALUE) {
					changed = true;
					ThinDescPart newPart = new ThinDescPart();
					newPart.setInitialCaseSignificant(part
							.getInitialCaseSignificant());
					newPart.setLang(part.getLang());
					newPart.setPathId(getNid(part.getPathId()));
					newPart.setStatusId(getNid(part.getStatusId()));
					newPart.setText(part.getText());
					newPart.setTypeId(getNid(part.getTypeId()));
					newPart.setVersion(ThinVersionHelper.convert(time));
					values.add(new TimePathId(newPart.getVersion(), newPart.getPathId()));
					thinDesc.addVersion(newPart);
				}
			}
			if (changed) {
				AceConfig.vodb.writeDescription(thinDesc);
				AceLog.getEditLog().fine("Importing desc changes: \n" + thinDesc);
			}
		}
	}

	private void commitUncommittedDescriptions(long time, UniversalAceBean bean, Set<TimePathId> values)
			throws DatabaseException, TerminologyException, IOException {
		for (UniversalAceDescription desc : bean.getUncommittedDescriptions()) {
			ThinDescVersioned thinDesc = new ThinDescVersioned(getNid(desc
					.getDescId()), getNid(desc.getConceptId()), desc
					.getVersions().size());
			for (UniversalAceDescriptionPart part : desc.getVersions()) {
				ThinDescPart newPart = new ThinDescPart();
				newPart.setInitialCaseSignificant(part
						.getInitialCaseSignificant());
				newPart.setLang(part.getLang());
				newPart.setPathId(getNid(part.getPathId()));
				newPart.setStatusId(getNid(part.getStatusId()));
				newPart.setText(part.getText());
				newPart.setTypeId(getNid(part.getTypeId()));
				newPart.setVersion(ThinVersionHelper.convert(part.getTime()));
				if (part.getTime() == Long.MAX_VALUE) {
					newPart.setVersion(ThinVersionHelper.convert(time));
				} else {
					newPart.setVersion(ThinVersionHelper
							.convert(part.getTime()));
				}
				values.add(new TimePathId(newPart.getVersion(), newPart.getPathId()));
				thinDesc.addVersion(newPart);
			}
			try {
				ThinDescVersioned oldDescVersioned = (ThinDescVersioned) AceConfig.vodb
						.getDescription(thinDesc.getDescId());
				oldDescVersioned.merge(thinDesc);
				AceLog.getEditLog().fine(
						"Merging desc with existing (should have been null): \n"
								+ thinDesc + "\n\n" + oldDescVersioned);
			} catch (DatabaseException e) {
				// expected exception...
			}
			AceConfig.vodb.writeDescription(thinDesc);
			AceLog.getEditLog().fine("Importing desc: \n" + thinDesc);
		}
	}

	private void commitUncommittedIds(long time, UniversalAceBean bean, Set<TimePathId> values)
			throws DatabaseException, TerminologyException, IOException {
		for (UniversalAceIdentification id : bean.getUncommittedIds()) {
			AceLog.getEditLog().fine("commitUncommittedIds: " + id);
			ThinIdVersioned tid = null;
			for (UniversalAceIdentificationPart part : id.getVersions()) {
				I_Path path = AceConfig.vodb.getPath(AceConfig.vodb
						.uuidToNative(part.getPathId()));
				if (tid == null) {
					try {
						int nid = getNid(id.getUIDs());
						AceLog.getEditLog().fine(
								"Uncommitted id already exists: \n" + id);
						tid = (ThinIdVersioned) AceConfig.vodb.getId(nid);
						AceLog.getEditLog().fine("found ThinIdVersioned: " + tid);
					} catch (NoMappingException ex) {
						/*
						 * Generate on the ARCHITECTONIC_BRANCH for now, it will
						 * get overwritten when the id is written to the
						 * database, with the proper branch and version values.
						 */
						int nid = AceConfig.vodb
								.uuidToNativeWithGeneration(id.getUIDs(), Integer.MAX_VALUE,
										path, ThinVersionHelper.convert(time));
						tid = new ThinIdVersioned(nid, id.getVersions().size());
						AceLog.getEditLog().fine("created ThinIdVersioned: " + tid);
					}
				}
				ThinIdPart newPart = new ThinIdPart();
				newPart.setIdStatus(getNid(part.getIdStatus()));
				newPart.setPathId(AceConfig.vodb
								.uuidToNative(part.getPathId()));
				newPart.setSource(getNid(part.getSource()));
				newPart.setSourceId(part.getSourceId());
				if (part.getTime() == Long.MAX_VALUE) {
					newPart.setVersion(ThinVersionHelper.convert(time));
				} else {
					newPart.setVersion(ThinVersionHelper
							.convert(part.getTime()));
				}
				values.add(new TimePathId(newPart.getVersion(), newPart.getPathId()));
				tid.addVersion(newPart);
				AceLog.getEditLog().fine(" add version: " + newPart);
			}
			/*
			 * The ARCHITECTONIC_BRANCH will be overridden here with the proper
			 * branch and version values here...
			 */
			AceConfig.vodb.writeId(tid);
		}
	}

	public File getChangeSetFile() {
		return changeSetFile;
	}

	public void setChangeSetFile(File changeSetFile) {
		this.changeSetFile = changeSetFile;
	}

	public void setCounter(I_Count counter) {
		this.counter = counter;
	}

}
