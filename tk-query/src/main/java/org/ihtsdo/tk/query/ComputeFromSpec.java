/*
 * Copyright 2013 International Health Terminology Standards Development Organisation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ihtsdo.tk.query;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.dwfa.bpa.process.Condition;
import org.dwfa.tapi.ComputationCanceled;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.helper.time.TimeHelper;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.NidBitSetBI;
import org.ihtsdo.tk.api.TerminologyStoreDI;
import org.ihtsdo.tk.api.concept.ConceptChronicleBI;
import org.ihtsdo.tk.api.coordinate.EditCoordinate;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.api.cs.ChangeSetPolicy;
import org.ihtsdo.tk.api.refex.RefexChronicleBI;
import org.ihtsdo.tk.query.RefsetComputer.ComputeType;
import org.ihtsdo.tk.query.persistance.PersistanceEngine;

/**
 * The class ComputeFromSpec provides methods for computing and persisting a
 * refset from a specification. Use
 * <code>RefsetComputer</code> to compute results without persisting.
 *
 */
public class ComputeFromSpec {

    /**
     * Computes a refset from a refset spec and persists results. Works with
     * existing business process tasks for generating a spec and computing a
     * refset.
     *
     * @param query the <code>RefsetSpecQuery</code> that contains the refset spec
     * @param viewCoordinate the <code>ViewCoordinate</code> specifying active/inactive versions
     * @param editCoordinate the <code>EditCoordinate</code> to use when writing results
     * @param refsetNid the nid of the concept representing the refset
     * @return a continue <code>Condition</code> 
     * @throws Exception indicates an exception has occurred
     */
    public static Condition computeRefset(RefsetSpecQuery query, ViewCoordinate viewCoordinate,
            EditCoordinate editCoordinate, int refsetNid,  ChangeSetPolicy csPolicy)
            throws Exception {
        Logger logger = Logger.getLogger(ComputeFromSpec.class.getName());
        TerminologyStoreDI ts = Ts.get();
        ConceptChronicleBI refsetConcept = ts.getConceptForNid(refsetNid);
        Collection<? extends RefexChronicleBI<?>> members = refsetConcept.getRefsetMembers();
        NidBitSetBI currentMemberSet = ts.getEmptyNidSet();
        for (RefexChronicleBI refex : members) {
            Collection versions = refex.getVersions(viewCoordinate);
            if (versions != null || !versions.isEmpty()) {
                currentMemberSet.setMember(refex.getReferencedComponentNid());
            }
        }

        logger.log(Level.INFO, ">>>>>>>>>> Computing RefsetSpecQuery: " + query);

        List<String> dangleWarnings = RefsetSpecFactory.removeDangles(query);

        for (String warning : dangleWarnings) {
            logger.log(Level.INFO, warning + "\nClause removed from computation: ");
        }

        RefsetSpec specHelper = new RefsetSpec(refsetConcept, true, viewCoordinate);


        RefsetComputer refsetComputer;
        Ts.get().suspendChangeNotifications();

        try {
            NidBitSetBI possibleIds;
            ComputeType computeType = null;
            if (specHelper.isConceptComputeType()) {
                logger.log(Level.INFO, ">>>>>>>>>> Computing possible concepts for concept spec: " + query);
                possibleIds = query.getPossibleConceptsInterruptable(null);
                computeType = ComputeType.CONCEPT;
            } else if (specHelper.isDescriptionComputeType()) {
                logger.log(Level.INFO, ">>>>>>>>>> Computing possible concepts for description spec: " + query);
                possibleIds = query.getPossibleDescriptionsInterruptable(null);
                computeType = ComputeType.DESCRIPTION;
            } else {
                throw new Exception("Relationship compute type not supported.");
            }

            // add the current members to the list of possible concepts to check (in case some need to be retired)
            possibleIds.or(currentMemberSet);
            logger.log(Level.INFO, ">>>>>>>>>> Search space (concept count): " + possibleIds.cardinality());
            refsetComputer = new RefsetComputer(query, viewCoordinate, possibleIds, computeType);
            long startTime = System.currentTimeMillis();
            logger.log(Level.INFO, ">>>>>>>>> Iterating concepts in parallel.");
            ts.iterateConceptDataInParallel(refsetComputer);
            

            if (!refsetComputer.continueWork()) {
                throw new ComputationCanceled("Computation cancelled");
            }
            specHelper.setLastComputeTime(System.currentTimeMillis(), editCoordinate);
            logger.log(Level.INFO, ">>>>>>>>> Finished computing spec.");
            
            logger.log(Level.INFO, ">>>>>>>>> Computing marked parents.");
            NidBitSetBI memberNids = refsetComputer.getMemberNids();
            MarkedParentComputer markedParentComputer = new MarkedParentComputer(memberNids, viewCoordinate);
            ts.iterateConceptDataInParallel(markedParentComputer);
            logger.log(Level.INFO, ">>>>>>>>> Finished computing marked parents.");
            logger.log(Level.INFO, ">>>>>>>>> Persisting results.");
            PersistanceEngine persister = new PersistanceEngine(viewCoordinate, editCoordinate, refsetNid, true, csPolicy);
            persister.persistRefsetAndMarkedParents(memberNids, markedParentComputer.getMarkedParentNids());
            
            long endTime = System.currentTimeMillis();
            long elapsed = endTime - startTime;
            String elapsedStr = TimeHelper.getElapsedTimeString(elapsed);
            System.out.println("Total computation time: " + elapsedStr);
            
            if (!refsetComputer.continueWork()) {
                throw new ComputationCanceled("Computation cancelled");
            }

            logger.log(Level.INFO, ">>>>>>>>> Finished");
            

        } catch (ComputationCanceled e) {
        } catch (InterruptedException e) {
        } catch (ExecutionException e) {

            if (getRootCause(e) instanceof TerminologyException) {
                throw new TerminologyException(e.getMessage());
            } else if (getRootCause(e) instanceof IOException) {
                throw new IOException(e.getMessage());
            } else if (getRootCause(e) instanceof ComputationCanceled) {
                // Nothing to do
            } else if (getRootCause(e) instanceof InterruptedException) {
                // Nothing to do
            } else {
                throw new TerminologyException(e);
            }
        } finally {
            ts.resumeChangeNotifications();
        }
        return Condition.CONTINUE;
    }
    /**
     * Gets the root cause of an exception.
     * @param e the exception
     * @return a <code>Throwable</code>
     */
    private static Throwable getRootCause(Exception e) {
        Throwable prevCause = e;
        Throwable rootCause = e.getCause();

        while (rootCause != null) {
            prevCause = rootCause;
            rootCause = rootCause.getCause();
        }

        return prevCause;
    }
}
