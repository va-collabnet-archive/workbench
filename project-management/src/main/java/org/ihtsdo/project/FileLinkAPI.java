/*
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

/**
 * The Class FileLinkAPI.
 */
public class FileLinkAPI {

    /**
     * The config.
     */
    I_ConfigAceFrame config;
    /**
     * The tf.
     */
    I_TermFactory tf;

    /**
     * Instantiates a new file link api.
     *
     * @param config the config
     */
    public FileLinkAPI(I_ConfigAceFrame config) {
        super();
        this.config = config;
        this.tf = Terms.get();
    }

    /**
     * Gets the categories.
     *
     * @param concept the concept
     * @return the categories
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws TerminologyException the terminology exception
     */
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

    /**
     * Put link in config.
     *
     * @param link the link
     * @throws IOException Signals that an I/O exception has occurred.
     */
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

    /**
     * Removes the link from config.
     *
     * @param link the link
     * @throws IOException Signals that an I/O exception has occurred.
     */
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

    /**
     * Gets the all links.
     *
     * @return the all links
     * @throws IOException Signals that an I/O exception has occurred.
     */
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

    /**
     * Gets the links for category.
     *
     * @param category the category
     * @return the links for category
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public List<FileLink> getLinksForCategory(I_GetConceptData category) throws IOException {
        List<FileLink> returnLinks = new ArrayList<FileLink>();
        I_ConfigAceDb userConfig = config.getDbConfig();

        List<FileLink> links = getAllLinks();

        for (FileLink loopLink : links) {
            if (compareLists(loopLink.getCategoryUUIDs(), category.getUids()) > 0) {
                returnLinks.add(loopLink);
            }
        }

        return returnLinks;
    }

    /**
     * Adds the folder as file links to config. Goes through all the non-hidden
     * files in all the sub-folders, and adds them to the config using the
     * passed category
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
            for (int i = 0; i < children.length; i++) {
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
     * Cleanup: cleans file links from the config that the file.exists() check
     * fail
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

    /**
     * Compare lists.
     *
     * @param list1 the list1
     * @param list2 the list2
     * @return the int
     */
    private static int compareLists(List list1, List list2) {
        int counter = 0;

        for (Object object : list1) {

            if (list2.contains(object) && object != null) {
                counter++;
            }
        }
        return counter;
    }
}
