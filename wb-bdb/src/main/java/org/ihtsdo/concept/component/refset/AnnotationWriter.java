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
package org.ihtsdo.concept.component.refset;

import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicInteger;
import org.ihtsdo.concept.Concept;
import org.ihtsdo.tk.api.refex.RefexChronicleBI;

/**
 *
 * @author kec
 */
public class AnnotationWriter {

    public static AtomicInteger encountered = new AtomicInteger();
    public static AtomicInteger written = new AtomicInteger();
    RefsetMemberFactory factory = new RefsetMemberFactory();

    public AnnotationWriter() {
    }

	@SuppressWarnings("unchecked")
	public ConcurrentSkipListSet<RefsetMember<?,?>> entryToObject(TupleInput input, 
            int enclosingConceptNid) {
        int listSize = input.readShort();
        if (listSize == 0) {
            return null;
        }
        ConcurrentSkipListSet<RefsetMember<?,?>> newRefsetMemberList = 
                new ConcurrentSkipListSet<RefsetMember<?,?>>(
                    new Comparator<RefexChronicleBI<?>>() {

                @Override
                public int compare(RefexChronicleBI<?> t, RefexChronicleBI<?> t1) {
                    return t.getNid() - t1.getNid();
                }
                
            });

        for (int index = 0; index < listSize; index++) {
            int typeNid = input.readInt();
            input.mark(8);
            int nid = input.readInt();
            input.reset();
            RefsetMember<?, ?> refsetMember = (RefsetMember<?, ?>) Concept.componentsCRHM.get(nid);
            if (refsetMember == null) {
                try {
                    refsetMember = factory.create(nid, typeNid, enclosingConceptNid, input);
                    if (refsetMember.getTime() != Long.MIN_VALUE) {
                        RefsetMember<?, ?> oldMember = (RefsetMember<?, ?>) Concept.componentsCRHM.putIfAbsent(nid, refsetMember);
                        if (oldMember != null) {
                            refsetMember = oldMember;
                        }
                    }
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            } else {
                try {
                    refsetMember.merge(factory.create(nid, typeNid, enclosingConceptNid, input));
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
            if (refsetMember.getTime() != Long.MIN_VALUE) {
                newRefsetMemberList.add(refsetMember);
            }

        }
        return newRefsetMemberList;
    }

    public void objectToEntry(Collection<RefsetMember<?,?>> list,
            TupleOutput output, int maxReadOnlyStatusAtPositionId) {
        if (list == null) {
            output.writeShort(0); // List size
            return;
        }
        List<RefsetMember<?,?>> refsetMembersToWrite = new ArrayList<RefsetMember<?,?>>(list.size());
        for (RefexChronicleBI<?> refsetChronicle : list) {
            RefsetMember<?,?> refsetMember = (RefsetMember<?, ?>) refsetChronicle;
            encountered.incrementAndGet();
            assert refsetMember.getSapNid() != Integer.MAX_VALUE;
            if (refsetMember.primordialSapNid > maxReadOnlyStatusAtPositionId
                    && refsetMember.getTime() != Long.MIN_VALUE) {
                refsetMembersToWrite.add(refsetMember);
            } else {
                if (refsetMember.revisions != null) {
                    for (RefsetRevision<?, ?> r : refsetMember.revisions) {
                        if (r.getStatusAtPositionNid() > maxReadOnlyStatusAtPositionId
                                && r.getTime() != Long.MIN_VALUE) {
                            refsetMembersToWrite.add(refsetMember);
                            break;
                        }
                    }
                }
            }
        }
        output.writeShort(refsetMembersToWrite.size()); // List size
        for (RefsetMember<?, ?> refsetMember : refsetMembersToWrite) {
            written.incrementAndGet();
            output.writeInt(refsetMember.getTypeNid());
            refsetMember.writeComponentToBdb(output, maxReadOnlyStatusAtPositionId);
        }
    }
}
