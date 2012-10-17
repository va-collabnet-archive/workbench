/**
 * Copyright (c) 2012 International Health Terminology Standards Development
 * Organisation
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
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
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.ContradictionException;
import org.ihtsdo.tk.api.TerminologySnapshotDI;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.api.refex.RefexChronicleBI;
import org.ihtsdo.tk.api.refex.type_nid_string.RefexNidStringVersionBI;
import org.ihtsdo.tk.binding.snomed.CaseSensitive;

/**
 * The Class CsWordsHelper loads the list of case sensitive words from the Case
 * Sensitive Words Refset in to a map of the word and the associated sensitivity
 * (either "case sensitive" or "maybe case sensitive").
 */
public class CsWordsHelper {

    private static Map<Integer, Set<String>> csWordSetMap = null;
    private static Lock initLock = new ReentrantLock();

    /**
     * Imports the case sensitive word list from the Case Sensitive Words Refset
     * into a map of the word to the associated sensitivity (either "case
     * sensitive" or "maybe case sensitive"). Checks to see if the map is empty
     * before loading, and will only load the words if the map is found to be
     * empty.
     *
     * @throws IOException signals that an I/O exception has occurred
     */
    public static void lazyInit()
            throws IOException {
        if (csWordSetMap == null) {
            initLock.lock();
            try {
                if (csWordSetMap == null) {
                    ViewCoordinate vc = Ts.get().getMetadataViewCoordinate();
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
                        RefexNidStringVersionBI member =
                                (RefexNidStringVersionBI) refex.getVersion(vc);
                        if (member != null) {
                            int typeNid = member.getNid1();
                            if (typeNid == icSigNid) {
                                csWordSet.add(member.getString1());
                            } else {
                                maybeCsWordSet.add(member.getString1());
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

    /**
     * Checks if text is initial case significant (ICS) based on the ICS type
     * given.
     *
     * @param text the string representing the word to check
     * @param icsTypeNid the ICS type nid
     * @return <code>true</code>, if the given string is found for the specified
     * ICS type
     * @throws IOException signals that an I/O exception has occurred
     */
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
