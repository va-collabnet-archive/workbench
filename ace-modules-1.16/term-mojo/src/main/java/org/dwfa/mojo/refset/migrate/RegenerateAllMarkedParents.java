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
package org.dwfa.mojo.refset.migrate;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_ProcessConcepts;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartConcept;
import org.dwfa.ace.api.ebr.I_ThinExtByRefTuple;
import org.dwfa.ace.api.ebr.I_ThinExtByRefVersioned;
import org.dwfa.ace.api.process.I_ProcessQueue;
import org.dwfa.ace.refset.MarkedParentRefsetHelper;
import org.dwfa.ace.task.profile.NewDefaultProfile;
import org.dwfa.ace.util.TupleVersionPart;
import org.dwfa.cement.RefsetAuxiliary;

/**
 *
 * @goal regenerate-all-marked-parents
 */
public class RegenerateAllMarkedParents extends AbstractMojo implements I_ProcessConcepts {
    /**
     * Class logger.
     */
    private Logger logger = Logger.getLogger(RegenerateAllMarkedParents.class.getName());

    /**
     * The number of threads to use.
     *
     * @parameter
     * @required
     */
    private int numberOfThreads = 1;

    /**
     * Da factory
     */
    protected I_TermFactory termFactory;

    /**
     * Map of refsets nids and there normal members nids
     */
    private HashMap<Integer, Set<Integer>> refsetNormalMemberList;

    /**
     * Thread pool for processing concepts.
     */
    private I_ProcessQueue workQueue;

    /**
     * Normal member native id.
     */
    private int normalMemeberNid;

    private int conceptCount = 0;


    public RegenerateAllMarkedParents() throws Exception {
        termFactory = LocalVersionedTerminology.get();
        if (termFactory == null) {
            throw new RuntimeException("The LocalVersionedTerminology is not available. Please check the database.");
        }
    }

    public void init() throws Exception {
        refsetNormalMemberList = new HashMap<Integer, Set<Integer>>();
        workQueue = LocalVersionedTerminology.get().newProcessQueue(numberOfThreads);
        normalMemeberNid = RefsetAuxiliary.Concept.NORMAL_MEMBER.localize().getNid();

        I_ConfigAceFrame config = getTermFactory().getActiveAceFrameConfig();
        if (config == null) {
            config = NewDefaultProfile.newProfile(null, null, null, null, null);
            getTermFactory().setActiveAceFrameConfig(config);
        }

        config.setViewPositions(null);
    }

    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            init();

            getTermFactory().iterateConcepts(this);

            for (Integer refsetNid : refsetNormalMemberList.keySet()) {
                workQueue.execute(new RunnableProcessConcept(refsetNid, refsetNormalMemberList.get(refsetNid)));
            }
            workQueue.awaitCompletion();

        } catch (Exception ex) {
            throw new MojoExecutionException("Unable to migrate specification refsets", ex);
        }
    }

    private I_TermFactory getTermFactory() {
        return termFactory;
    }

    @Override
    public void processConcept(I_GetConceptData concept) throws Exception {
        if(++conceptCount % 1000 == 0){
            logger.info("Concepts processed " + conceptCount);
        }

        for (I_ThinExtByRefVersioned thinExtByRefVersioned : getTermFactory().getAllExtensionsForComponent(concept.getNid())) {
            List<I_ThinExtByRefTuple> extensions = thinExtByRefVersioned.getTuples(null, null, true, false);

            for (I_ThinExtByRefTuple thinExtByRefTuple : extensions) {
                if (thinExtByRefTuple.getPart() instanceof I_ThinExtByRefPartConcept) {
                    I_ThinExtByRefPartConcept part = (I_ThinExtByRefPartConcept) thinExtByRefTuple.getPart();
                    if (part.getC1id() == normalMemeberNid) {
                        if (!refsetNormalMemberList.containsKey(thinExtByRefTuple.getRefsetId())) {
                            refsetNormalMemberList.put(thinExtByRefTuple.getRefsetId(), new HashSet<Integer>());
                        }

                        refsetNormalMemberList.get(thinExtByRefTuple.getRefsetId()).add(thinExtByRefTuple.getComponentId());
                    }
                }
            }
        }
    }

    /**
     * Class to use with Work Queue.
     */
    class RunnableProcessConcept implements Runnable {

        int refsetNid;
        Set<Integer> normalMemberNids;

        RunnableProcessConcept(int refsetNid, Set<Integer> normalMemberNids) throws Exception{
            this.refsetNid = refsetNid;
            this.normalMemberNids = normalMemberNids;
        }

        @Override
        public void run() {
            try {
                 I_GetConceptData refsetConcept = getTermFactory().getConcept(refsetNid);
                logger.info("Processing " + refsetConcept.getInitialText());
                I_ConfigAceFrame config = termFactory.getActiveAceFrameConfig();

                config.getEditingPathSet().clear();
                config.addEditingPath(getTermFactory().getPath(
                        TupleVersionPart.getLatestPart(refsetConcept.getConceptAttributes().getVersions()).getPathId()));

                new MarkedParentRefsetHelper(refsetNid, normalMemeberNid, false).addParentMembers(normalMemberNids.toArray(new Integer[] {}));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}