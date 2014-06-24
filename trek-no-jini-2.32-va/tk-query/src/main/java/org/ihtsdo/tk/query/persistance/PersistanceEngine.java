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
package org.ihtsdo.tk.query.persistance;

import java.io.IOException;
import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import org.dwfa.cement.RefsetAuxiliary;
import org.ihtsdo.helper.time.TimeHelper;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.ConceptFetcherBI;
import org.ihtsdo.tk.api.NidBitSetBI;
import org.ihtsdo.tk.api.ProcessUnfetchedConceptDataBI;
import org.ihtsdo.tk.api.TerminologyStoreDI;
import org.ihtsdo.tk.api.concept.ConceptChronicleBI;
import org.ihtsdo.tk.api.coordinate.EditCoordinate;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.api.cs.ChangeSetPolicy;
import org.ihtsdo.tk.api.refex.RefexChronicleBI;
import org.ihtsdo.tk.query.RefsetSpec;
import org.ihtsdo.tk.query.helper.MarkedParentRefsetHelper;
import org.ihtsdo.tk.query.helper.RefsetHelper;

/**
 *
 * @author aimeefurber
 */
public class PersistanceEngine {

    private int refsetNid;
    private int memberCount = 0;
    private int markedParentCount = 0;
    private AtomicInteger newMemberCount = new AtomicInteger();
    private AtomicInteger retiredMemberCount = new AtomicInteger();
    private AtomicInteger newMarkedParentCount = new AtomicInteger();
    private AtomicInteger retiredMarkedParentCount = new AtomicInteger();
    private ViewCoordinate viewCoordinate;
    private MarkedParentRefsetHelper markedParentRefsetHelper;
    private ConceptChronicleBI refsetConcept;
    private ConceptChronicleBI markedParentRefsetConcept;
    private TerminologyStoreDI ts;
    protected NidBitSetBI originalRetiredRefsetRefCompNids;
    protected NidBitSetBI originalActiveRefsetRefCompNids;
    protected NidBitSetBI originalActiveMarkedParentRefCompNids;
    NidBitSetBI resultSet;
    private int normalMemberNid;
    private RefsetHelper memberRefsetHelper;
    private AtomicBoolean continueWorking = new AtomicBoolean(true);
    private long startTime;
    private boolean commit = false;
    private ChangeSetPolicy csPolicy;

    public PersistanceEngine(ViewCoordinate viewCoordinate, EditCoordinate editCoordinate,
            int memberRefsetNid, boolean commit, ChangeSetPolicy csPolicy) throws IOException, Exception {
        ts = Ts.get();
        this.viewCoordinate = viewCoordinate;
        this.commit = commit;
        this.refsetNid = memberRefsetNid;
        this.refsetConcept = ts.getConcept(refsetNid);
        this.originalRetiredRefsetRefCompNids = ts.getEmptyNidSet();
        this.originalActiveRefsetRefCompNids = ts.getEmptyNidSet();
        this.originalActiveMarkedParentRefCompNids = ts.getEmptyNidSet();
        for (RefexChronicleBI r : refsetConcept.getRefsetMembers()) {
            if (r.getVersions(viewCoordinate).isEmpty()) {
                originalRetiredRefsetRefCompNids.setMember(r.getReferencedComponentNid());
            } else {
                originalActiveRefsetRefCompNids.setMember(r.getReferencedComponentNid());
            }
        }
        memberRefsetHelper = new RefsetHelper(viewCoordinate, editCoordinate);
        markedParentRefsetHelper = new MarkedParentRefsetHelper(viewCoordinate, editCoordinate, refsetNid);
        markedParentRefsetConcept = ts.getConcept(markedParentRefsetHelper.getParentRefsetNid());
        Collection<? extends RefexChronicleBI<?>> refsetMembers = markedParentRefsetConcept.getRefsetMembers();
        for (RefexChronicleBI r : refsetMembers) {
            if (!r.getVersions(viewCoordinate).isEmpty()) {
                originalActiveMarkedParentRefCompNids.setMember(r.getReferencedComponentNid());
            }
        }
        normalMemberNid = ts.getNidForUuids(RefsetAuxiliary.Concept.NORMAL_MEMBER.getUids());
        this.csPolicy = csPolicy;
    }

    public void persistRefsetMembers(NidBitSetBI newMembers) throws Exception {
        startTime = System.currentTimeMillis();
        memberCount = newMembers.cardinality();
        RefsetMemberWriter refsetMemberWriter = new RefsetMemberWriter(newMembers);
        Ts.get().iterateConceptDataInParallel(refsetMemberWriter);
        Ts.get().addUncommitted(refsetConcept);
        if (commit) {
            Ts.get().commit(refsetConcept, csPolicy);
        }
        long elapsed = System.currentTimeMillis() - startTime;
        String elapsedStr = TimeHelper.getElapsedTimeString(elapsed);

        System.out.println("Refset member writting complete. Time: " + elapsedStr
                + "; Members: " + memberCount + " New: "
                + newMemberCount.get() + " Ret: " + retiredMemberCount.get());
    }

    public void persistMarkedParents(NidBitSetBI newMarkedParents) throws Exception {
        startTime = System.currentTimeMillis();
        markedParentCount = newMarkedParents.cardinality();
        RefsetMarkedParentWriter refsetMarkedParentWriter = new RefsetMarkedParentWriter(newMarkedParents);
        Ts.get().iterateConceptDataInParallel(refsetMarkedParentWriter);
        Ts.get().addUncommitted(markedParentRefsetConcept);
        if (commit) {
            Ts.get().commit(markedParentRefsetConcept, csPolicy);
        }
        long endTime = System.currentTimeMillis();
        long elapsed = endTime - startTime;
        String elapsedStr = TimeHelper.getElapsedTimeString(elapsed);
        System.out.println("Marked parent writting complete. Elapsed: " + elapsedStr
                + ";  Members: " + markedParentCount + " New: " + newMarkedParentCount.get() + " Ret: "
                + retiredMarkedParentCount.get());

    }

    public void persistRefsetAndMarkedParents(NidBitSetBI newMembers, NidBitSetBI newMarkedParents) throws Exception {
        startTime = System.currentTimeMillis();
        Writer writer = new Writer(newMembers, newMarkedParents);
        Ts.get().iterateConceptDataInParallel(writer);
        Ts.get().addUncommitted(refsetConcept);
        Ts.get().addUncommitted(markedParentRefsetConcept);
        if (commit) {
            RefsetSpec helper = new RefsetSpec(refsetConcept, true, viewCoordinate);
            if(helper.getComputeConcept().isUncommitted()){
                Ts.get().commit(helper.getComputeConcept(), csPolicy);
            }
            if(helper.getEditConcept().isUncommitted()){
                Ts.get().commit(helper.getEditConcept(), csPolicy);
            }
            Ts.get().commit(refsetConcept, csPolicy);
            Ts.get().commit(markedParentRefsetConcept, csPolicy);
        }
        long endTime = System.currentTimeMillis();
        long elapsed = endTime - startTime;
        String elapsedStr = TimeHelper.getElapsedTimeString(elapsed);
        System.out.println("Refset member writting complete. Time: " + elapsedStr
                + "; Members: " + memberCount + " New: "
                + newMemberCount.get() + " Ret: " + retiredMemberCount.get());
        System.out.println("Marked parent writting complete. Elapsed: " + elapsedStr
                + ";  Members: " + markedParentCount + " New: " + newMarkedParentCount.get() + " Ret: "
                + retiredMarkedParentCount.get());
    }

    private class RefsetMemberWriter implements ProcessUnfetchedConceptDataBI {

        NidBitSetBI newMembers;

        public RefsetMemberWriter(NidBitSetBI newMembers) {
            this.newMembers = newMembers;
        }

        @Override
        public void processUnfetchedConceptData(int conceptNid, ConceptFetcherBI conceptFetcher) throws Exception {
            process(conceptNid, newMembers);
        }

        protected void process(int conceptNid, NidBitSetBI newMembers) throws Exception {
            if (originalActiveRefsetRefCompNids.isMember(conceptNid) || newMembers.isMember(conceptNid)) {
                if (!originalActiveRefsetRefCompNids.isMember(conceptNid) && newMembers.isMember(conceptNid)) { //add
                    newMemberCount.incrementAndGet();
                    memberRefsetHelper.newRefsetExtension(refsetNid, conceptNid,
                            normalMemberNid);
                } else if (originalActiveRefsetRefCompNids.isMember(conceptNid) && !newMembers.isMember(conceptNid)) { //retire
                    retiredMemberCount.incrementAndGet();
                    memberRefsetHelper.retireRefsetExtension(refsetNid, conceptNid,
                            normalMemberNid);
                }
            }
        }

        @Override
        public NidBitSetBI getNidSet() throws IOException {
            NidBitSetBI allMembers = Ts.get().getEmptyNidSet();
            allMembers.or(newMembers);
            allMembers.or(originalActiveRefsetRefCompNids);
            return allMembers;
        }

        @Override
        public boolean continueWork() {
            return continueWorking.get();
        }
    }

    private class RefsetMarkedParentWriter implements ProcessUnfetchedConceptDataBI {

        NidBitSetBI newMarkedParents;

        public RefsetMarkedParentWriter(NidBitSetBI newMarkedParents) {
            this.newMarkedParents = newMarkedParents;
        }

        @Override
        public void processUnfetchedConceptData(int conceptNid, ConceptFetcherBI conceptFetcher) throws Exception {
            process(conceptNid, newMarkedParents);
        }

        protected void process(int conceptNid, NidBitSetBI newMarkedParents) throws Exception {
            if (originalActiveMarkedParentRefCompNids.isMember(conceptNid) || newMarkedParents.isMember(conceptNid)) {
                if (!originalActiveMarkedParentRefCompNids.isMember(conceptNid) && newMarkedParents.isMember(conceptNid)) { //add
                    newMarkedParentCount.incrementAndGet();
                    markedParentRefsetHelper.addParentMembers(refsetNid, conceptNid,
                            normalMemberNid);
                } else if (originalActiveMarkedParentRefCompNids.isMember(conceptNid) && !newMarkedParents.isMember(conceptNid)) {
                    retiredMarkedParentCount.incrementAndGet();
                    markedParentRefsetHelper.removeParentMembers(refsetNid, conceptNid,
                            normalMemberNid);
                }
            }
        }

        @Override
        public NidBitSetBI getNidSet() throws IOException {
            NidBitSetBI allMembers = Ts.get().getEmptyNidSet();
            allMembers.or(newMarkedParents);
            allMembers.or(originalActiveMarkedParentRefCompNids);
            return allMembers;
        }

        @Override
        public boolean continueWork() {
            return continueWorking.get();
        }
    }

    private class Writer implements ProcessUnfetchedConceptDataBI {

        NidBitSetBI newMarkedParents;
        NidBitSetBI newMembers;
        RefsetMemberWriter refsetMemberWriter;
        RefsetMarkedParentWriter refsetMarkedParentWriter;

        public Writer(NidBitSetBI newMembers, NidBitSetBI newMarkedParents) {
            this.newMarkedParents = newMarkedParents;
            this.newMembers = newMembers;
            refsetMemberWriter = new RefsetMemberWriter(newMembers);
            refsetMarkedParentWriter = new RefsetMarkedParentWriter(newMarkedParents);
        }

        @Override
        public void processUnfetchedConceptData(int conceptNid, ConceptFetcherBI conceptFetcher) throws Exception {
            refsetMemberWriter.process(conceptNid, newMembers);
            refsetMarkedParentWriter.process(conceptNid, newMarkedParents);
        }

        @Override
        public NidBitSetBI getNidSet() throws IOException {
            NidBitSetBI allMembers = ts.getEmptyNidSet();
            allMembers.or(newMarkedParents);
            allMembers.or(originalActiveMarkedParentRefCompNids);
            allMembers.or(newMembers);
            allMembers.or(originalActiveRefsetRefCompNids);
            return allMembers;
        }

        @Override
        public boolean continueWork() {
            return continueWorking.get();
        }
    }

    public void setCommit(boolean commit) {
        this.commit = commit;
    }
}
