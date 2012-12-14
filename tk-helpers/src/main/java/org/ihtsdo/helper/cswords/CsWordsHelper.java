/*
 * Copyright 2011 International Health Terminology Standards Development Organisation.
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
package org.ihtsdo.helper.cswords;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.dwfa.ace.api.ebr.I_ExtendByRefPartCidString;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.ContradictionException;
import org.ihtsdo.tk.api.TerminologySnapshotDI;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.api.refex.RefexChronicleBI;
import org.ihtsdo.tk.api.refex.type_cnid_str.RefexCnidStrVersionBI;
import org.ihtsdo.tk.binding.snomed.CaseSensitive;

/**
 *
 * @author AKF
 */
public class CsWordsHelper {

    private static Map<Integer, Set<String>> csWordSetMap = null;
    private static Lock initLock = new ReentrantLock();

    public static void lazyInit()
            throws IOException {
        if (csWordSetMap == null) {
            initLock.lock();
            try {
                if (csWordSetMap == null) {
                    ViewCoordinate vc = Ts.get().getMetadataVC();
                    TerminologySnapshotDI ts = Ts.get().getSnapshot(vc);
                    HashMap csWordSetMap = new HashMap<Integer, Set<String>>();
                    ConceptVersionBI csWordsRefsetC =
                            CaseSensitive.CS_WORDS_REFSET.get(vc);
                    Collection<? extends RefexChronicleBI<?>> csWords =
                            csWordsRefsetC.getRefexes();

                    Set<String> csWordSet = new HashSet<String>();

                    int icSigNid = CaseSensitive.IC_SIGNIFICANT.getLenient().getNid();
                    int maybeSigNid = CaseSensitive.MAYBE_IC_SIGNIFICANT.getLenient().getNid();
                    Set<String> maybeCsWordSet = new HashSet<String>();
                    for (RefexChronicleBI<?> refex : csWords) {

                        boolean IsExtendRefPartCidString = refex.getVersion(vc) instanceof I_ExtendByRefPartCidString;

                        if (refex.getVersion(vc) instanceof I_ExtendByRefPartCidString) {
                            I_ExtendByRefPartCidString member = (I_ExtendByRefPartCidString) refex
                                    .getVersion(vc);
                            if (member != null) {
                                int typeNid = member.getC1id();
                                if (typeNid == icSigNid) {
                                    csWordSet.add(member.getStringValue());
                                } else {
                                    maybeCsWordSet.add(member.getStringValue());
                                }
                            }
                        } else {
                            // try {
                            if (refex.getVersion(vc) instanceof RefexCnidStrVersionBI) {
                                RefexCnidStrVersionBI member = 
                                        (RefexCnidStrVersionBI) refex.getVersion(vc);
                                if (member != null) {
                                    int typeNid = member.getCnid1();
                                    if (typeNid == icSigNid) {
                                        csWordSet.add(member.getStr1());
                                    } else {
                                        maybeCsWordSet.add(member.getStr1());
                                    }
                                }
                            }
                        }
                    }
                    csWordSetMap.put(icSigNid, csWordSet);
                    csWordSetMap.put(maybeSigNid, maybeCsWordSet);
                    CsWordsHelper.csWordSetMap = csWordSetMap;
                }
            } catch (ContradictionException ex) {
                throw new IOException(ex);
            } finally {
                initLock.unlock();
            }
        }
    }

    public static boolean isIcTypeSignificant(String text, int icsTypeNid)
            throws IOException {
        lazyInit();
        String[] tokens = text.split(" ");
        Set<String> csWords = csWordSetMap.get(icsTypeNid);
        if (csWords != null) {
            for (String token : tokens) {
                if (csWords.contains(token)) {
                    return true;
                }
            }
        }
        return false;
    }
}
