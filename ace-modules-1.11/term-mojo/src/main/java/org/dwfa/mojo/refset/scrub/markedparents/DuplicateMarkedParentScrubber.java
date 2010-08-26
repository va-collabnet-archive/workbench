/**
 * Copyright (c) 2009 International Health Terminology Standards Development
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
package org.dwfa.mojo.refset.scrub.markedparents;

import java.io.File;
import java.util.HashMap;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;

import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPart;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartConcept;
import org.dwfa.ace.api.ebr.I_ThinExtByRefVersioned;
import org.dwfa.ace.refset.MemberRefsetChangesetWriter;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.mojo.ConceptDescriptor;
import org.dwfa.mojo.refset.scrub.ConceptExtFinder;
import org.dwfa.mojo.refset.scrub.ConceptExtHandler;

/**
 * This scrubber removes duplicate "marked parents" changing their status to
 * "retired".
 */
public final class DuplicateMarkedParentScrubber implements ConceptExtHandler {

    /**
     * Specify the path that changes will be written to
     * 
     * @parameter
     * @required
     */
    private ConceptDescriptor writeToPath;
    
    /**
     * The directory where new CMRSCS change set file(s) will be created
     * 
     * @parameter
     * @required
     */
    private File changeSetOutputDirectory;
    
    private MemberRefsetChangesetWriter changesetWriter;
    
    private final I_TermFactory termFactory;

    private final int retiredStatusId;

    private HashMap<Integer, String> conceptDescCache = new HashMap<Integer, String>();
    
    public DuplicateMarkedParentScrubber() throws Exception {
        termFactory = LocalVersionedTerminology.get();
        retiredStatusId = termFactory.uuidToNative(ArchitectonicAuxiliary.Concept.RETIRED.getUids().iterator().next());
    }

    public void process(final ConceptExtFinder finder) {
        try {
            // This initialisation cannot be done in the class constructor because the mojo parameters
            // are not set until after the object is initialised
            UUID pathUuid = writeToPath.getVerifiedConcept().getUids().iterator().next();
            changesetWriter = new MemberRefsetChangesetWriter(changeSetOutputDirectory, termFactory, pathUuid);

            for (Object aFinder : finder) {
                processExtension((I_ThinExtByRefVersioned) aFinder);
            }
            termFactory.commit();
        } catch (Exception e) {
            throw new RuntimeException("Unable to complete the scrub.", e);
        } finally {
            if (changesetWriter != null) {
                try {
                    changesetWriter.close();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    private void processExtension(final I_ThinExtByRefVersioned member) throws Exception {
        // sort by version, smallest to largest.
        SortedSet<I_ThinExtByRefPart> sortedVersions = new TreeSet<I_ThinExtByRefPart>(new LatestVersionComparator());
        sortedVersions.addAll(member.getVersions());

        // Get the latest version.
        I_ThinExtByRefPartConcept latestPart = (I_ThinExtByRefPartConcept) sortedVersions.last();
        
        changesetWriter.addToRefset(member.getMemberId(), member.getComponentId(), latestPart.getC1id(), member.getRefsetId(), retiredStatusId);
        
        // System.out.printf("Scrubbed duplicate ext: refset='%1$s', component='%2$s', concept='%3$s'\n", 
        //    getConceptDesc(member.getRefsetId()), getConceptDesc(member.getComponentId()), latestPart.getC1id());
    }

    @SuppressWarnings("unused")
    private String getConceptDesc(int nid) {
        if (!conceptDescCache.containsKey(nid)) {
            try {
                conceptDescCache.put(nid, termFactory.getConcept(nid).getInitialText());
            } catch (Exception e) {
                conceptDescCache.put(nid, Integer.toString(nid));
            }
        } 
        
        return conceptDescCache.get(nid);
    }
}
