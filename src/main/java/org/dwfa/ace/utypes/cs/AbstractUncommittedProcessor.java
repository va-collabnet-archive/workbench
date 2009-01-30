package org.dwfa.ace.utypes.cs;

import java.io.IOException;

import org.dwfa.ace.log.AceLog;
import org.dwfa.ace.utypes.UniversalAceBean;
import org.dwfa.ace.utypes.UniversalAceConceptAttributes;
import org.dwfa.ace.utypes.UniversalAceConceptAttributesPart;
import org.dwfa.ace.utypes.UniversalAceDescription;
import org.dwfa.ace.utypes.UniversalAceDescriptionPart;
import org.dwfa.ace.utypes.UniversalAceExtByRefBean;
import org.dwfa.ace.utypes.UniversalAceExtByRefPart;
import org.dwfa.ace.utypes.UniversalAceIdentification;
import org.dwfa.ace.utypes.UniversalAceIdentificationPart;
import org.dwfa.ace.utypes.UniversalAceImage;
import org.dwfa.ace.utypes.UniversalAceImagePart;
import org.dwfa.ace.utypes.UniversalAcePath;
import org.dwfa.ace.utypes.UniversalAceRelationship;
import org.dwfa.ace.utypes.UniversalAceRelationshipPart;
import org.dwfa.ace.utypes.UniversalIdList;

public abstract class AbstractUncommittedProcessor implements
		I_ProcessUniversalChangeSets {

	public void processAcePath(UniversalAcePath path, long time)
			throws IOException {
		AceLog.getEditLog().fine("Importing new universal path: \n" + path);
		processNewUniversalAcePath(path);
	}

	protected abstract void processNewUniversalAcePath(UniversalAcePath path);

	public void processUniversalAceBean(UniversalAceBean bean, long time)
			throws IOException {
		if (time == Long.MAX_VALUE) {
			throw new IOException("commit time = Long.MAX_VALUE");
		}
		// Process all the parts...
		processUncommittedIds(time, bean);
		processUncommittedConceptAttributes(time, bean);
		processUncommittedDescriptions(time, bean);
		processUncommittedRelationships(time, bean);
		processUncommittedImages(time, bean);

		processConceptAttributeChanges(time, bean);
		processDescriptionChanges(time, bean);
		processRelationshipChanges(time, bean);
		processImageChanges(time, bean);

	}

	public void processAceEbr(UniversalAceExtByRefBean bean, long time)
			throws IOException {
		if (time == Long.MAX_VALUE) {
			throw new IOException("commit time = Long.MAX_VALUE");
		}
		for (UniversalAceExtByRefPart part : bean.getVersions()) {
			if (part.getTime() == Long.MAX_VALUE) {
				processUncommittedUniversalAceExtByRefPart(part);
			}
		}
	}

	protected abstract void processUncommittedUniversalAceExtByRefPart(
			UniversalAceExtByRefPart part)  throws IOException;

	public void processIdList(UniversalIdList list, long time)
			throws IOException {
		if (time == Long.MAX_VALUE) {
			throw new IOException("commit time = Long.MAX_VALUE");
		}
		// Do all the commiting...
		for (UniversalAceIdentification id : list.getUncommittedIds()) {
			AceLog.getEditLog().fine("processUncommittedIds: " + id);
			for (UniversalAceIdentificationPart part : id.getVersions()) {
				if (part.getTime() == Long.MAX_VALUE) {
					processUncommittedUniversalAceIdentificationPart(part);
				}
			}
		}
	}

	protected abstract void processUncommittedUniversalAceIdentificationPart(
			UniversalAceIdentificationPart part)  throws IOException;

	private void processConceptAttributeChanges(long time, UniversalAceBean bean)
			throws IOException {
		if (bean.getConceptAttributes() != null) {
			UniversalAceConceptAttributes attributes = bean
					.getConceptAttributes();
			for (UniversalAceConceptAttributesPart part : attributes
					.getVersions()) {
				if (part.getTime() == Long.MAX_VALUE) {
					processUncommittedUniversalAceConceptAttributesPart(part);
				}
			}
		}
	}

	protected abstract void processUncommittedUniversalAceConceptAttributesPart(
			UniversalAceConceptAttributesPart part) throws IOException;

	private void processUncommittedConceptAttributes(long time,
			UniversalAceBean bean) throws IOException {
		if (bean.getUncommittedConceptAttributes() != null) {
			for (UniversalAceConceptAttributesPart part : bean
					.getConceptAttributes().getVersions()) {
				if (part.getTime() == Long.MAX_VALUE) {
					processUncommittedUniversalAceConceptAttributesPart(part);
				}
			}
		}
	}

	private void processImageChanges(long time, UniversalAceBean bean)
			throws IOException {
		for (UniversalAceImage image : bean.getImages()) {
			for (UniversalAceImagePart part : image.getVersions()) {
				if (part.getTime() == Long.MAX_VALUE) {
					processUncommittedUniversalAceImagePart(part);
				}
			}
		}
	}

	protected abstract void processUncommittedUniversalAceImagePart(
			UniversalAceImagePart part) throws IOException;

	private void processUncommittedImages(long time, UniversalAceBean bean)
			throws IOException {
		for (UniversalAceImage image : bean.getUncommittedImages()) {
			for (UniversalAceImagePart part : image.getVersions()) {
				if (part.getTime() == Long.MAX_VALUE) {
					processUncommittedUniversalAceImagePart(part);
				}
			}
		}
	}

	private void processUncommittedRelationships(long time,
			UniversalAceBean bean) throws IOException {
		for (UniversalAceRelationship rel : bean.getUncommittedSourceRels()) {
			for (UniversalAceRelationshipPart part : rel.getVersions()) {
				if (part.getTime() == Long.MAX_VALUE) {
					processUncommittedUniversalAceRelationshipPart(part);
				}
			}
		}
	}

	protected abstract void processUncommittedUniversalAceRelationshipPart(
			UniversalAceRelationshipPart part) throws IOException;

	private void processRelationshipChanges(long time, UniversalAceBean bean)
			throws IOException {
		for (UniversalAceRelationship rel : bean.getSourceRels()) {
			for (UniversalAceRelationshipPart part : rel.getVersions()) {
				if (part.getTime() == Long.MAX_VALUE) {
					processUncommittedUniversalAceRelationshipPart(part);
				}
			}
		}
	}

	private void processDescriptionChanges(long time, UniversalAceBean bean)
			throws IOException {
		for (UniversalAceDescription desc : bean.getDescriptions()) {
			if (desc.getVersions().size() == 0) {
				AceLog.getAppLog().warning("Description has no parts: " + desc);
			} else {
				processDescription(time, desc);
			}
		}
	}

	private void processUncommittedDescriptions(long time, UniversalAceBean bean)
			throws IOException {
		for (UniversalAceDescription desc : bean.getUncommittedDescriptions()) {
			processDescription(time, desc);
		}
	}

	private void processDescription(long time, UniversalAceDescription desc)
			throws IOException {
		for (UniversalAceDescriptionPart part : desc.getVersions()) {
			if (part.getTime() == Long.MAX_VALUE) {
				processUncommittedUniversalAceDescriptionPart(part);
			}
		}
	}

	protected abstract void processUncommittedUniversalAceDescriptionPart(
			UniversalAceDescriptionPart part)  throws IOException;

	private void processUncommittedIds(long time, UniversalAceBean bean)
			throws IOException {
		for (UniversalAceIdentification id : bean.getUncommittedIds()) {
			AceLog.getEditLog().fine("processUncommittedIds: " + id);
			for (UniversalAceIdentificationPart part : id.getVersions()) {
				if (part.getTime() == Long.MAX_VALUE) {
					processUncommittedUniversalAceIdentificationPart(part);
				}
			}
		}
	}

}
