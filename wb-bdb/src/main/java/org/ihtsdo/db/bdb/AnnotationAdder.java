/*
 * Copyright 2010 International Health Terminology Standards Development Organisation.
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
package org.ihtsdo.db.bdb;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import org.dwfa.ace.api.IdentifierSet;
import org.dwfa.ace.log.AceLog;
import org.ihtsdo.concept.Concept;
import org.ihtsdo.concept.I_FetchConceptFromCursor;
import org.ihtsdo.concept.I_ProcessUnfetchedConceptData;
import org.ihtsdo.concept.ParallelConceptIterator;
import org.ihtsdo.concept.component.refset.RefsetMemberFactory;
import org.ihtsdo.tk.api.ComponentChroncileBI;
import org.ihtsdo.tk.api.NidBitSetBI;
import org.ihtsdo.tk.dto.concept.component.refset.TkRefsetAbstractMember;

/**
 *
 * @author maestro
 */
public class AnnotationAdder implements I_ProcessUnfetchedConceptData {

    static class TkRmComparator implements Comparator<TkRefsetAbstractMember<?>> {

        @Override
        public int compare(TkRefsetAbstractMember<?> t, TkRefsetAbstractMember<?> t1) {
            return t.primordialUuid.compareTo(t1.primordialUuid);
        }
    }
    NidBitSetBI conceptNids = new IdentifierSet();
    ConcurrentHashMap<Integer, ConcurrentSkipListSet<TkRefsetAbstractMember<?>>> membersForConcept =
            new ConcurrentHashMap<Integer, ConcurrentSkipListSet<TkRefsetAbstractMember<?>>>();

    AnnotationAdder(List<TkRefsetAbstractMember<?>> members) {

        TkRmComparator comparator = new TkRmComparator();
        int errors = 0;
        for (TkRefsetAbstractMember<?> member : members) {
            int cNid = Bdb.getConceptNid(Bdb.uuidToNid(member.getComponentUuid()));
            if (cNid + Integer.MIN_VALUE >= 0) {
                conceptNids.setMember(cNid);
                ConcurrentSkipListSet<TkRefsetAbstractMember<?>> set =
                        new ConcurrentSkipListSet<TkRefsetAbstractMember<?>>(comparator);
                membersForConcept.putIfAbsent(cNid, set);
                membersForConcept.get(cNid).add(member);
            } else {
                AceLog.getAppLog().warning("No concept for: " + member);
                errors++;
            }
        }
        if (errors > 0) {
                AceLog.getAppLog().warning(errors + " processing errors.");
        }
    }

    @Override
    public void processUnfetchedConceptData(int cNid, I_FetchConceptFromCursor fcfc) throws Exception {
        if (conceptNids.isMember(cNid)) {
            Concept c = fcfc.fetch();
            ConcurrentSkipListSet<TkRefsetAbstractMember<?>> set =
                    membersForConcept.get(cNid);
            for (TkRefsetAbstractMember<?> member : set) {
                ComponentChroncileBI<?> component = c.getComponent(Bdb.uuidToNid(member.getComponentUuid()));
                component.addAnnotation(RefsetMemberFactory.create(member, cNid));
            }
            membersForConcept.remove(cNid);
            BdbCommitManager.addUncommittedNoChecks(c);
        }
    }

    @Override
    public void setParallelConceptIterators(List<ParallelConceptIterator> pcis) {
        // nothing to do...;
    }

    @Override
    public NidBitSetBI getNidSet() throws IOException {
        return conceptNids;
    }

    @Override
    public boolean continueWork() {
        return true;
    }
}
