package org.dwfa.ace.task.path;

import java.beans.IntrospectionException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_Path;
import org.dwfa.ace.api.I_Position;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.task.ProcessAttachmentKeys;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.jini.TermEntry;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;
import org.dwfa.util.id.Type5UuidFactory;

@BeanList(specs = { @Spec(directory = "tasks/ace/path", type = BeanType.TASK_BEAN) })
public class NewEditPathForUser extends AbstractTask {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static final int dataVersion = 1;

	private TermEntry parentPathTermEntry = new TermEntry(
			ArchitectonicAuxiliary.Concept.DEVELOPMENT.getUids());

	private String userPropName = ProcessAttachmentKeys.USERNAME
			.getAttachmentKey();

	private String originTime = "latest";

	private String profilePropName = ProcessAttachmentKeys.WORKING_PROFILE
			.getAttachmentKey();

	private void writeObject(ObjectOutputStream out) throws IOException {
		out.writeInt(dataVersion);
		out.writeObject(parentPathTermEntry);
		out.writeObject(originTime);
		out.writeObject(profilePropName);
		out.writeObject(userPropName);
	}

	private void readObject(ObjectInputStream in) throws IOException,
			ClassNotFoundException {
		int objDataVersion = in.readInt();
		if (objDataVersion == dataVersion) {
			parentPathTermEntry = (TermEntry) in.readObject();
			originTime = (String) in.readObject();
			profilePropName = (String) in.readObject();
			userPropName = (String) in.readObject();
		} else {
			throw new IOException("Can't handle dataversion: " + objDataVersion);
		}

	}

	public void complete(I_EncodeBusinessProcess process, I_Work worker)
			throws TaskFailedException {
		// Nothing to do...

	}

	public Condition evaluate(I_EncodeBusinessProcess process, I_Work worker)
			throws TaskFailedException {
		try {
			String username = (String) process.readProperty(userPropName);
			I_TermFactory tf = LocalVersionedTerminology.get();
			I_ConfigAceFrame activeProfile = tf.getActiveAceFrameConfig();

			UUID type5ConceptId = Type5UuidFactory.get(
					parentPathTermEntry.ids[0], username);

			I_GetConceptData newPathConcept = tf.newConcept(type5ConceptId,
					false, activeProfile);

			String descText = username + " development editing path";
			tf
					.newDescription(
							Type5UuidFactory.get(parentPathTermEntry.ids[0],
									descText),
							newPathConcept,
							"en",
							descText,
							ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE
									.localize(), activeProfile);

			descText = username + " dev path";
			tf.newDescription(Type5UuidFactory.get(parentPathTermEntry.ids[0],
					descText), newPathConcept, "en", descText,
					ArchitectonicAuxiliary.Concept.PREFERRED_DESCRIPTION_TYPE
							.localize(), activeProfile);

			descText = username;
			tf.newDescription(Type5UuidFactory.get(parentPathTermEntry.ids[0],
					descText + "username"), newPathConcept, "en", descText,
					ArchitectonicAuxiliary.Concept.USER_NAME.localize(),
					activeProfile);

			descText = username;
			tf.newDescription(Type5UuidFactory.get(parentPathTermEntry.ids[0],
					descText + "inbox"), newPathConcept, "en", descText,
					ArchitectonicAuxiliary.Concept.USER_INBOX.localize(),
					activeProfile);

			I_GetConceptData relType = tf
					.getConcept(ArchitectonicAuxiliary.Concept.IS_A_REL
							.getUids());
			I_GetConceptData relDestination = tf
					.getConcept(parentPathTermEntry.ids);
			I_GetConceptData relCharacteristic = tf
					.getConcept(ArchitectonicAuxiliary.Concept.DEFINING_CHARACTERISTIC
							.getUids());
			I_GetConceptData relRefinability = tf
					.getConcept(ArchitectonicAuxiliary.Concept.NOT_REFINABLE
							.getUids());
			I_GetConceptData relStatus = tf
					.getConcept(ArchitectonicAuxiliary.Concept.CURRENT
							.getUids());

			UUID relId = Type5UuidFactory.get(parentPathTermEntry.ids[0],
					"relid");
			tf.newRelationship(relId, newPathConcept, relType, relDestination,
					relCharacteristic, relRefinability, relStatus, 0,
					activeProfile);

			tf.commit();

			Set<I_Position> origins = new HashSet<I_Position>();

			I_Path parentPath = tf.getPath(parentPathTermEntry.ids);
			origins.add(tf.newPosition(parentPath, tf
					.convertToThinVersion(originTime)));

			I_Path editPath = tf.newPath(origins, newPathConcept);
			I_ConfigAceFrame profile = (I_ConfigAceFrame) process
					.readProperty(profilePropName);
			profile.getEditingPathSet().clear();
			profile.addEditingPath(editPath);
			profile.getViewPositionSet().clear();
			profile
					.addViewPosition(tf
							.newPosition(editPath, Integer.MAX_VALUE));
			tf.commit();

			return Condition.CONTINUE;
		} catch (IllegalArgumentException e) {
			throw new TaskFailedException(e);
		} catch (IllegalAccessException e) {
			throw new TaskFailedException(e);
		} catch (InvocationTargetException e) {
			throw new TaskFailedException(e);
		} catch (IntrospectionException e) {
			throw new TaskFailedException(e);
		} catch (IOException e) {
			throw new TaskFailedException(e);
		} catch (TerminologyException e) {
			throw new TaskFailedException(e);
		} catch (Exception e) {
			throw new TaskFailedException(e);
		}
	}

	public Collection<Condition> getConditions() {
		return CONTINUE_CONDITION;
	}

	public int[] getDataContainerIds() {
		return new int[] {};
	}

	public String getUserPropName() {
		return userPropName;
	}

	public void setUserPropName(String profilePropName) {
		this.userPropName = profilePropName;
	}

	public String getOriginTime() {
		return originTime;
	}

	public void setOriginTime(String originTime) {
		this.originTime = originTime;
	}

	public TermEntry getParentPathTermEntry() {
		return parentPathTermEntry;
	}

	public void setParentPathTermEntry(TermEntry parentPath) {
		this.parentPathTermEntry = parentPath;
	}

	public String getProfilePropName() {
		return profilePropName;
	}

	public void setProfilePropName(String profilePropName) {
		this.profilePropName = profilePropName;
	}

}
