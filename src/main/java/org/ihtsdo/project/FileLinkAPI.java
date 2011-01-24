package org.ihtsdo.project;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.dwfa.ace.api.I_ConfigAceDb;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.TerminologyException;

public class FileLinkAPI {
	I_ConfigAceFrame config;
	I_TermFactory tf;

	public FileLinkAPI(I_ConfigAceFrame config) {
		super();
		this.config = config;
		this.tf = Terms.get();
	}
	
	public Set<I_GetConceptData> getCategories(I_GetConceptData concept) throws IOException, TerminologyException {
		Set<I_GetConceptData> children = new HashSet<I_GetConceptData>();
		
		I_IntSet isa = tf.newIntSet();
		isa.add(ArchitectonicAuxiliary.Concept.IS_A_REL.localize().getNid());
		
		
		children.addAll(concept.getDestRelOrigins(config.getAllowedStatus(), isa, config.getViewPositionSetReadOnly(), config.getPrecedence(), config.getConflictResolutionStrategy()));
		
		Set<I_GetConceptData> childrenClone = new HashSet<I_GetConceptData>();
		childrenClone.addAll(children);
		
		for (I_GetConceptData loopChild : childrenClone) {
			children.addAll(getCategories(loopChild));
		}
		
		return children;
	}

	public void putLinkInConfig(FileLink link) throws IOException {
		I_ConfigAceDb userConfig = config.getDbConfig();

		List<FileLink> links = getAllLinks();

		Boolean found = false;
		for (FileLink loopLink : links) {
			if (loopLink.getFile().getPath().equals(link.getFile().getPath())) {
				found = true;
			}
		}
		
		if (!found) {
			links.add(link);
		}

		userConfig.setProperty("FileLinks", links);

	}

	public void removeLinkFromConfig(FileLink link) throws IOException {
		I_ConfigAceDb userConfig = config.getDbConfig();

		List<FileLink> links;

		links = (List<FileLink>) userConfig.getProperty("FileLinks");

		if (links == null) {
			links = new ArrayList<FileLink>();
		}

		List<FileLink> linksClone = new ArrayList<FileLink>();
		linksClone.addAll(links);

		for (FileLink loopLink : linksClone) {
			if (loopLink.getUuid().equals(link.getUuid())) {
				links.remove(loopLink);
			}
		}
		userConfig.setProperty("FileLinks", links);

	}

	public List<FileLink> getAllLinks() throws IOException {
		List<FileLink> returnLinks = new ArrayList<FileLink>();
		I_ConfigAceDb userConfig = config.getDbConfig();

		List<FileLink> links;

		links = (List<FileLink>) userConfig.getProperty("FileLinks");

		if (links == null) {
			links = new ArrayList<FileLink>();
		}

		for (FileLink loopLink : links) {
			returnLinks.add(loopLink);
		}

		return returnLinks;
	}

	public List<FileLink> getLinksForCategory(I_GetConceptData category) throws IOException {
		List<FileLink> returnLinks = new ArrayList<FileLink>();
		I_ConfigAceDb userConfig = config.getDbConfig();

		List<FileLink> links = getAllLinks();

		for (FileLink loopLink : links) {
			if (compareLists(loopLink.getCategoryUUIDs(),category.getUids()) > 0) {
				returnLinks.add(loopLink);
			}
		}

		return returnLinks;
	}

	/**
	 * Adds the folder as file links to config.
	 * Goes through all the non-hidden files in all the sub-folders, and adds them to the config
	 * using the passed category
	 * 
	 * @param folder the folder
	 * @param category the category
	 * 
	 * @return the list< file link>
	 * 
	 * @throws Exception the exception
	 */
	public List<FileLink> addFolderAsFileLinksToConfig(File folder, I_GetConceptData category) throws Exception {
		List<FileLink> returnLinks = new ArrayList<FileLink>();

		if (folder.isDirectory() && !folder.isHidden()) {
			String[] children = folder.list();
			for (int i=0; i<children.length; i++) {
				File loopFile = new File(folder, children[i]);

				if (loopFile.isFile() && !loopFile.isHidden()) {
					FileLink loopLink = new FileLink(loopFile, category);
					putLinkInConfig(loopLink);
					returnLinks.add(loopLink);
				}

				if (loopFile.isDirectory()) {
					returnLinks.addAll(addFolderAsFileLinksToConfig(loopFile, category));
				}
			}
		}

		return returnLinks;
	}

	/**
	 * Cleanup: cleans file links from the config that the file.exists() check fail
	 * 
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public void cleanup() throws IOException {
		I_ConfigAceDb userConfig = config.getDbConfig();

		List<FileLink> links;

		links = (List<FileLink>) userConfig.getProperty("FileLinks");

		if (links == null) {
			links = new ArrayList<FileLink>();
		}

		List<FileLink> linksClone = new ArrayList<FileLink>();
		linksClone.addAll(links);

		for (FileLink loopLink : linksClone) {
			if (!loopLink.getFile().exists()) {
				links.remove(loopLink);
			}
		}
		userConfig.setProperty("FileLinks", links);

	}

	private static int compareLists(List list1, List list2) {
		int counter = 0;

		for (Object object : list1) {

			if(list2.contains(object)&& object != null)
			{    
				counter++;
			}
		}
		return counter;
	}

}
