/**
 * Copyright (c) 2010 International Health Terminology Standards Development
 * Organisation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ihtsdo.issue.manager;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.swing.JDialog;
import javax.swing.JOptionPane;

import org.apache.axis.encoding.Base64;
import org.apache.lucene.document.Document;
import org.apache.lucene.queryParser.ParseException;
import org.dwfa.ace.api.I_AmTermComponent;
import org.dwfa.ace.api.I_ConfigAceDb;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_DescriptionVersioned;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_HelpRefsets;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.RefsetPropertyMap;
import org.dwfa.ace.api.RefsetPropertyMap.REFSET_PROPERTY;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.api.ebr.I_ExtendByRef;
import org.dwfa.ace.api.ebr.I_ExtendByRefPart;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartStr;
import org.dwfa.ace.api.ebr.I_ExtendByRefVersion;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.etypes.EConcept.REFSET_TYPES;
import org.ihtsdo.issue.IssueRepoRegistration;
import org.ihtsdo.issue.issuerepository.IssueRepository;
import org.ihtsdo.lucene.SearchResult;
import org.ihtsdo.tk.api.PathBI;
import org.ihtsdo.tk.api.PositionBI;
import org.ihtsdo.tk.api.Precedence;



/**
 * The Class IssueRepositoryDAO.
 */
public class IssueRepositoryDAO {

	//I_TerminologyProjects CRUD **********************************

	/**
	 * Gets the all issue repository.
	 * 
	 * @param config the config
	 * 
	 * @return the all issue repository
	 * 
	 * @throws Exception the exception
	 */
	public static List<IssueRepository> getAllIssueRepository(I_ConfigAceFrame config) throws Exception {
		I_TermFactory termFactory = Terms.get();
		List<IssueRepository> issueRepos = new ArrayList<IssueRepository>();
		try {
			I_GetConceptData issueReposRoot = termFactory.getConcept(
					new UUID[] {RefsetAuxiliary.Concept.ISSUE_REPOSITORY.getUids().iterator().next()});
			I_IntSet allowedDestRelTypes =  termFactory.newIntSet();
			allowedDestRelTypes.add(termFactory.uuidToNative(ArchitectonicAuxiliary.Concept.IS_A_REL.getUids()));
			I_IntSet allowedStatuses =  termFactory.newIntSet();
			allowedStatuses.add(termFactory.uuidToNative(ArchitectonicAuxiliary.Concept.RETIRED.getUids()));
			allowedStatuses.addAll(config.getAllowedStatus().getSetValues());
			Set<? extends I_GetConceptData> children = issueReposRoot.getDestRelOrigins(allowedStatuses,
					allowedDestRelTypes, config.getViewPositionSetReadOnly(), Precedence.TIME, 
					config.getConflictResolutionStrategy());
			for (I_GetConceptData child : children) {
				if (child.getConceptAttributeTuples(allowedStatuses, config.getViewPositionSetReadOnly(), 
						Precedence.TIME, 
						config.getConflictResolutionStrategy()).iterator().next().getStatusId() !=
							ArchitectonicAuxiliary.Concept.RETIRED.localize().getNid()) {
					issueRepos.add(getIssueRepository(child));
				}
			}

		} catch (TerminologyException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return issueRepos;
	}

	/**
	 * Adds the issue repo to metahier.
	 * 
	 * @param issueRepoWithMetadata the issue repo with metadata
	 * @param config the config
	 * 
	 * @return the i_ get concept data
	 */
	public static I_GetConceptData addIssueRepoToMetahier(IssueRepository issueRepoWithMetadata, I_ConfigAceFrame config) {
		I_GetConceptData newConcept = null;
		I_TermFactory termFactory = Terms.get();
		I_HelpRefsets refsetHelper;
		refsetHelper = termFactory.getRefsetHelper(config);
		refsetHelper.setAutocommitActive(true);

		//		try {
		//			termFactory.setActiveAceFrameConfig(config);
		//		} catch (TerminologyException e1) {
		//			e1.printStackTrace();
		//		} catch (IOException e1) {
		//			e1.printStackTrace();
		//		}

		try {
			if (isConceptDuplicate(issueRepoWithMetadata.getName())) {
				JOptionPane.showMessageDialog(new JDialog(), "Duplicated repository name", "Error", JOptionPane.ERROR_MESSAGE);
				throw new Exception("Repository name allready exists.");
			}
			I_GetConceptData issueReposRoot = termFactory.getConcept(
					RefsetAuxiliary.Concept.ISSUE_REPOSITORY.getUids());

			I_GetConceptData issueReposRefset = termFactory.getConcept(
					RefsetAuxiliary.Concept.ISSUE_REPOSITORY_METADATA_REFSET.getUids());

			I_GetConceptData fsnType = termFactory.getConcept(
					ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.getUids());

			I_GetConceptData preferredType = termFactory.getConcept(
					ArchitectonicAuxiliary.Concept.PREFERRED_DESCRIPTION_TYPE.getUids());

			newConcept = termFactory.newConcept(UUID.randomUUID(), false, config);

			termFactory.newDescription(UUID.randomUUID(), newConcept, "en", issueRepoWithMetadata.getName() + " (issue repository)",
					fsnType, config);

			termFactory.newDescription(UUID.randomUUID(), newConcept, "en", issueRepoWithMetadata.getName(),
					preferredType, config);

			termFactory.newRelationship(UUID.randomUUID(), newConcept, 
					termFactory.getConcept(ArchitectonicAuxiliary.Concept.IS_A_REL.getUids()), 
					issueReposRoot, 
					termFactory.getConcept(ArchitectonicAuxiliary.Concept.DEFINING_CHARACTERISTIC.getUids()), 
					termFactory.getConcept(ArchitectonicAuxiliary.Concept.NOT_REFINABLE.getUids()),
					termFactory.getConcept(ArchitectonicAuxiliary.Concept.CURRENT.getUids()), 
					0, config);

			String metadata = serialize(issueRepoWithMetadata);

			termFactory.addUncommitted(newConcept);
			termFactory.addUncommitted(issueReposRefset);
			termFactory.commit();

			promote(newConcept, config);

			termFactory.addUncommitted(newConcept);
			termFactory.addUncommitted(issueReposRefset);
			termFactory.commit();

			refsetHelper.newRefsetExtension(issueReposRefset.getConceptNid(), newConcept.getConceptNid(), 
					REFSET_TYPES.STR, 
					new RefsetPropertyMap().with(REFSET_PROPERTY.STRING_VALUE, metadata), config); 

			for (I_ExtendByRef extension : termFactory.getRefsetExtensionMembers(issueReposRefset.getConceptNid())) {
				if (extension.getComponentNid() == newConcept.getConceptNid() &&
						extension.getMutableParts().iterator().next().getTime() == Long.MAX_VALUE) {
					termFactory.addUncommittedNoChecks(extension);
					termFactory.commit();
					promote(extension, config);
					termFactory.addUncommittedNoChecks(extension);
					termFactory.commit();
				}
			}

		} catch (TerminologyException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	return newConcept;
}


/**
 * Gets the issue repository.
 * 
 * @param issueRepoConcept the issue repo concept
 * 
 * @return the issue repository
 * 
 * @throws Exception the exception
 */
public static IssueRepository getIssueRepository(I_GetConceptData issueRepoConcept) throws Exception {

	IssueRepository deserializedIssueRepoWithMetadata = null;
	I_TermFactory termFactory = Terms.get();
	I_ConfigAceFrame config = termFactory.getActiveAceFrameConfig();
	I_HelpRefsets refsetHelper;
	refsetHelper = termFactory.getRefsetHelper(config);
	refsetHelper.setAutocommitActive(true);

	try {
		I_GetConceptData issueReposRefset = termFactory.getConcept(
				new UUID[] {RefsetAuxiliary.Concept.ISSUE_REPOSITORY_METADATA_REFSET.getUids().iterator().next()});
		I_IntSet allowedDestRelTypes =  termFactory.newIntSet();
		allowedDestRelTypes.add(termFactory.uuidToNative(ArchitectonicAuxiliary.Concept.IS_A_REL.getUids()));
		I_IntSet descriptionTypes =  termFactory.newIntSet();
		descriptionTypes.add(termFactory.uuidToNative(ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.getUids()));

		//List<I_ExtendByRef> extensions = new ArrayList<I_ExtendByRef>();

		List<I_ExtendByRefPart> currentExtensionParts = 
			refsetHelper.getAllCurrentRefsetExtensions(issueReposRefset.getConceptNid(), 
					issueRepoConcept.getConceptNid());

		for (I_ExtendByRefPart loopPart : currentExtensionParts) {
			I_ExtendByRefPartStr strPart = (I_ExtendByRefPartStr) loopPart;
			String metadata = strPart.getStringValue();
			deserializedIssueRepoWithMetadata = (IssueRepository) deserialize(metadata);
		}


		//			extensions.addAll(issueRepoConcept.getExtensions());
		//			for (I_ExtendByRef extension : extensions) {
		//				if (extension.getRefsetId() == issueReposRefset.getConceptNid()) {
		//					List<? extends I_ExtendByRefVersion> extTuples = extension.getTuples(config.getConflictResolutionStrategy());
		//					if (!extTuples.isEmpty()) {
		//						I_ExtendByRefPartStr part = (I_ExtendByRefPartStr) extTuples.iterator().next().getMutablePart();
		//						String metadata = part.getStringValue();
		//						deserializedIssueRepoWithMetadata = (IssueRepository) deserialize(metadata);
		//					} else {
		//					}
		//				}
		//			}
		if (deserializedIssueRepoWithMetadata == null) {
			throw new Exception("No object found");
		}
		deserializedIssueRepoWithMetadata.setConceptId(issueRepoConcept.getConceptNid());
		deserializedIssueRepoWithMetadata.setUuid(issueRepoConcept.getUids().get(0));
		deserializedIssueRepoWithMetadata.setId(issueRepoConcept.getConceptNid());
	} catch (TerminologyException e) {
		e.printStackTrace();
	} catch (IOException e) {
		e.printStackTrace();
	}
	return deserializedIssueRepoWithMetadata;
}

/**
 * Gets the repository registration.
 * 
 * @param issueRepositoryUUId the issue repository uu id
 * @param config the config
 * 
 * @return the repository registration
 * 
 * @throws Exception the exception
 */
public static IssueRepoRegistration getRepositoryRegistration(UUID issueRepositoryUUId, I_ConfigAceFrame config) throws Exception {

	I_ConfigAceDb dbConfig=config.getDbConfig();
	HashMap<UUID,IssueRepoRegistration>  repos;

	repos=(HashMap<UUID,IssueRepoRegistration>)dbConfig.getProperty(IssueRepoRegistration.ISSUE_REPO_PROPERTY_NAME);
	if (repos==null){
		return null;
	}
	if (repos.containsKey(issueRepositoryUUId))
		return repos.get(issueRepositoryUUId);

	return null;
}

/**
 * Update repository metadata.
 * 
 * @param issueRepoWithMetadata the issue repo with metadata
 * @param config the config
 * 
 * @return the issue repository
 */
public static IssueRepository updateRepositoryMetadata(IssueRepository issueRepoWithMetadata, I_ConfigAceFrame config) {
	I_TermFactory termFactory = Terms.get();
	IssueRepository issueRepo = null;

	try {
		I_GetConceptData issueReposRefset = termFactory.getConcept(
				new UUID[] {RefsetAuxiliary.Concept.ISSUE_REPOSITORY_METADATA_REFSET.getUids().iterator().next()});

		I_GetConceptData issueRepoConcept = termFactory.getConcept(new UUID[]{issueRepoWithMetadata.getUuid()});

		String metadata = serialize(issueRepoWithMetadata);

		List<I_ExtendByRef> extensions = new ArrayList<I_ExtendByRef>();

		extensions.addAll(issueRepoConcept.getExtensions());

		boolean foundPreviousExtension = false;
		for (I_ExtendByRef extension : extensions) {
			if (extension.getRefsetId() == issueReposRefset.getConceptNid()) {
				foundPreviousExtension = true;
				List<? extends I_ExtendByRefVersion> extTuples = extension.getTuples(config.getConflictResolutionStrategy());
				I_ExtendByRefPartStr part = (I_ExtendByRefPartStr) 
				extTuples.iterator().next().getMutablePart().makeAnalog(
						config.getEditingPathSet().iterator().next().getConceptNid(),
						ArchitectonicAuxiliary.Concept.CURRENT.localize().getNid(),
						Long.MAX_VALUE);
				part.setStringValue(metadata);
				extension.addVersion(part);
				termFactory.addUncommitted(extension);
				promote(extension, config);
			}
		}

		if (!foundPreviousExtension) {
			termFactory.getRefsetHelper(config).newRefsetExtension(issueReposRefset.getConceptNid(), issueRepoConcept.getConceptNid(), REFSET_TYPES.STR, 
					new RefsetPropertyMap().with(REFSET_PROPERTY.STRING_VALUE, metadata), config);
			termFactory.addUncommitted(issueRepoConcept);
			termFactory.addUncommitted(issueReposRefset);
			for (I_ExtendByRef extension : termFactory.getRefsetExtensionMembers(issueReposRefset.getConceptNid())) {
				if (extension.getComponentId() == issueRepoConcept.getConceptNid() &&
						extension.getMutableParts().iterator().next().getVersion() == Integer.MAX_VALUE) {
					termFactory.addUncommittedNoChecks(extension);
					promote(extension, config);
					termFactory.addUncommittedNoChecks(extension);
				}
			}
		}

		termFactory.commit();
		issueRepo = getIssueRepository(issueRepoConcept);

	} catch (TerminologyException e) {
		e.printStackTrace();
	} catch (IOException e) {
		e.printStackTrace();
	} catch (Exception e) {
		e.printStackTrace();
	}

	return issueRepo;
}

/**
 * Retire issue repository.
 * 
 * @param conceptToRetire the concept to retire
 * @param config the config
 * 
 * @return the i_ get concept data
 */
public static I_GetConceptData retireIssueRepository(I_GetConceptData conceptToRetire, I_ConfigAceFrame config) {
	I_TermFactory termFactory = Terms.get();
	try {
		Collection<? extends I_ExtendByRef> extensions = termFactory.getAllExtensionsForComponent(
				conceptToRetire.getConceptNid());
		for (I_ExtendByRef extension : extensions) {
			if (extension.getRefsetId() == conceptToRetire.getConceptNid()) {
				I_ExtendByRefPart lastPart = getLastExtensionPart(extension);
				for (PathBI editPath : config.getEditingPathSetReadOnly()) {
					I_ExtendByRefPartStr part = (I_ExtendByRefPartStr) 
					lastPart.makeAnalog(
							ArchitectonicAuxiliary.Concept.RETIRED.localize().getNid(),
							editPath.getConceptNid(),
							Long.MAX_VALUE);
					extension.addVersion(part);
				}
				termFactory.addUncommittedNoChecks(conceptToRetire);
				termFactory.addUncommittedNoChecks(extension);
				termFactory.commit();
				promote(extension, config);
				termFactory.addUncommittedNoChecks(conceptToRetire);
				termFactory.addUncommittedNoChecks(extension);
				termFactory.commit();
			}
		}
	} catch (TerminologyException e) {
		e.printStackTrace();
	} catch (IOException e) {
		e.printStackTrace();
	} catch (Exception e) {
		e.printStackTrace();
	}
	return conceptToRetire;
}

public static I_ExtendByRefPart getLastExtensionPart(I_ExtendByRef extension) throws TerminologyException, IOException {
	int lastVersion = Integer.MIN_VALUE;
	I_ConfigAceFrame config = Terms.get().getActiveAceFrameConfig();
	I_IntSet allowedStatus = Terms.get().newIntSet();
	allowedStatus.addAll(config.getAllowedStatus().getSetValues());
	allowedStatus.add(ArchitectonicAuxiliary.Concept.INACTIVE.localize().getNid());
	allowedStatus.add(ArchitectonicAuxiliary.Concept.RETIRED.localize().getNid());
	I_ExtendByRefPart lastPart = null;
	for (I_ExtendByRefVersion loopTuple : extension.getTuples(
			allowedStatus, config.getViewPositionSetReadOnly(), Precedence.TIME,
			config.getConflictResolutionStrategy())) {
		if (loopTuple.getVersion() > lastVersion) {
			lastVersion = loopTuple.getVersion();
			lastPart = loopTuple.getMutablePart();
		}
	}

	if (lastPart == null) {
		throw new TerminologyException("No parts on this viewpositionset.");
	}

	return lastPart;
}

/**
 * Adds the repository to profile.
 * 
 * @param issueRepoRegistration the issue repo registration
 * 
 * @throws IOException Signals that an I/O exception has occurred.
 */
public static void addRepositoryToProfile(IssueRepoRegistration issueRepoRegistration) throws IOException {

	try {

		HashMap<UUID,IssueRepoRegistration> repos = null;
		if (!Terms.get().getActiveAceFrameConfig().getDbConfig().getProperties().containsKey(IssueRepoRegistration.ISSUE_REPO_PROPERTY_NAME)){
			repos=new HashMap<UUID,IssueRepoRegistration>();
			repos.put(issueRepoRegistration.getRepositoryUUId(),issueRepoRegistration);
			Terms.get().getActiveAceFrameConfig().getDbConfig().setProperty(IssueRepoRegistration.ISSUE_REPO_PROPERTY_NAME,repos);
		} else {
			repos = (HashMap<UUID, IssueRepoRegistration>) Terms.get().getActiveAceFrameConfig().getDbConfig().getProperty(IssueRepoRegistration.ISSUE_REPO_PROPERTY_NAME);
			repos.put(issueRepoRegistration.getRepositoryUUId(),issueRepoRegistration);
			Terms.get().getActiveAceFrameConfig().getDbConfig().setProperty(IssueRepoRegistration.ISSUE_REPO_PROPERTY_NAME,repos);
		}

	} catch (IOException e) {
		e.printStackTrace();
	} catch (TerminologyException e) {
		e.printStackTrace();
	}

}

/**
 * Adds the repository to profile.
 * 
 * @param issueRepoRegistration the issue repo registration
 * @param dbConfig the db config
 * 
 * @throws IOException Signals that an I/O exception has occurred.
 */
public static void addRepositoryToProfile(
		IssueRepoRegistration issueRepoRegistration, 
		I_ConfigAceDb dbConfig)
throws IOException {

	HashMap<UUID, IssueRepoRegistration> repos = null;
	if (!dbConfig.getProperties().containsKey(
			IssueRepoRegistration.ISSUE_REPO_PROPERTY_NAME)) {
		repos = new HashMap<UUID, IssueRepoRegistration>();
		repos.put(issueRepoRegistration.getRepositoryUUId(),
				issueRepoRegistration);
		dbConfig.setProperty(
				IssueRepoRegistration.ISSUE_REPO_PROPERTY_NAME, repos);
	} else {
		repos = (HashMap<UUID, IssueRepoRegistration>) dbConfig
		.getProperty(IssueRepoRegistration.ISSUE_REPO_PROPERTY_NAME);
		repos.put(issueRepoRegistration.getRepositoryUUId(),
				issueRepoRegistration);
		dbConfig.setProperty(
				IssueRepoRegistration.ISSUE_REPO_PROPERTY_NAME, repos);
	}

}

/**
 * Serialize.
 * 
 * @param object the object
 * 
 * @return the string
 */
private static String serialize(Object object) {
	String serializedForm = "";
	//BASE64Encoder encode = new BASE64Encoder();
	try {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(baos);
		oos.writeObject(object);
		serializedForm = Base64.encode(baos.toByteArray());
	} catch (IOException e) {
		e.printStackTrace();
		return null;
	}
	return serializedForm;
}

/**
 * Deserialize.
 * 
 * @param string the string
 * 
 * @return the object
 */
private static Object deserialize(String string) {
	Object object = null;
	//BASE64Decoder decode = new BASE64Decoder();
	try {
		ByteArrayInputStream bios = new ByteArrayInputStream(Base64.decode(string));
		ObjectInputStream ois = new ObjectInputStream(bios);
		object = ois.readObject();
	} catch (IOException e) {
		e.printStackTrace();
		return null;
	} catch (ClassNotFoundException e) {
		e.printStackTrace();
		return null;
	}
	return object;
}

public static void promote(I_AmTermComponent termComponent, I_ConfigAceFrame config) {
	PositionBI viewPosition = config.getViewPositionSetReadOnly().iterator().next();
	I_IntSet allowedStatusWithRetired = Terms.get().newIntSet();
	allowedStatusWithRetired.addAll(config.getAllowedStatus().getSetValues());
	try {
		allowedStatusWithRetired.add(ArchitectonicAuxiliary.Concept.RETIRED.localize().getNid());
		allowedStatusWithRetired.add(ArchitectonicAuxiliary.Concept.INACTIVE.localize().getNid());
		//			if (I_GetConceptData.class.isAssignableFrom(termComponent.getClass())) {
		//				Terms.get().addUncommittedNoChecks((I_GetConceptData)termComponent);
		//			}
		//			if (I_ExtendByRef.class.isAssignableFrom(termComponent.getClass())) {
		//				Terms.get().addUncommittedNoChecks((I_ExtendByRef)termComponent);
		//			}
		//			
		//			Terms.get().commit();

		termComponent.promote(viewPosition, config.getPromotionPathSetReadOnly(), 
				allowedStatusWithRetired, Precedence.TIME);

		//			if (I_GetConceptData.class.isAssignableFrom(termComponent.getClass())) {
		//				Terms.get().addUncommittedNoChecks((I_GetConceptData)termComponent);
		//			}
		//			if (I_ExtendByRef.class.isAssignableFrom(termComponent.getClass())) {
		//				Terms.get().addUncommittedNoChecks((I_ExtendByRef)termComponent);
		//			}
		//
		//			Terms.get().commit();
	} catch (Exception e) {
		e.printStackTrace();
	}


	//		for (I_Path path : config.getEditingPathSetReadOnly()) {
	//			try {
	//				I_Position viewPosition = Terms.get().newPosition(path, Integer.MAX_VALUE);
	//				termComponent.promote(viewPosition, config.getPromotionPathSetReadOnly(), 
	//						config.getAllowedStatus(), PRECEDENCE.TIME);
	//			} catch (TerminologyException e) {
	//				e.printStackTrace();
	//			} catch (IOException e) {
	//				e.printStackTrace();
	//			}
	//		}

}

private static boolean isConceptDuplicate(String descriptionText) {
	boolean result = false;
	I_TermFactory tf = Terms.get();
	try {
		I_ConfigAceFrame config = tf.getActiveAceFrameConfig();
		SearchResult results = tf.doLuceneSearch(descriptionText);
		for (int i = 0 ; i <results.topDocs.scoreDocs.length  ; i++) {
			try{
				Document doc = results.searcher.doc(results.topDocs.scoreDocs[i].doc);
				int cnid = Integer.parseInt(doc.get("cnid"));
				int dnid = Integer.parseInt(doc.get("dnid"));
				//System.out.println(doc);
				I_DescriptionVersioned<?> description = tf.getDescription(dnid);
				if (description.getTuples(config.getConflictResolutionStrategy()).iterator().next().getText().equals(description)) {
					result = true;
				}
			}catch(Exception e){
				//Do Nothing
			}
		}
	} catch (IOException e) {
		e.printStackTrace();
	} catch (ParseException e) {
		e.printStackTrace();
	} catch (TerminologyException e) {
		e.printStackTrace();
	}
	return result;
}

}
