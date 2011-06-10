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
import org.ihtsdo.helper.dialect.UnsupportedDialectOrLanguage;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.ContraditionException;
import org.ihtsdo.tk.api.TerminologySnapshotDI;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.api.refex.RefexChronicleBI;
import org.ihtsdo.tk.api.refex.type_cnid.RefexCnidVersionBI;
import org.ihtsdo.tk.api.refex.type_str.RefexStrVersionBI;
import org.ihtsdo.tk.example.binding.CaseSensitive;

/**
 *
 * @author AKF
 */
public class CsWordsHelper {

    private static Map<Integer, Set<String>> csWordSetMap = null;
    private static Lock initLock = new ReentrantLock();

    private static void lazyInit()
            throws IOException {
        if (csWordSetMap == null) {
            initLock.lock();
            try {
                if (csWordSetMap == null) {
                    ViewCoordinate vc = Ts.get().getMetadataVC();
                    TerminologySnapshotDI ts = Ts.get().getSnapshot(vc);
                    csWordSetMap = new HashMap<Integer, Set<String>>();
                    ConceptVersionBI csWordsRefsetC =
                            CaseSensitive.CS_WORDS_REFSET.get(Ts.get().getMetadataVC());
                    Collection<? extends RefexChronicleBI<?>> csWords =
                            csWordsRefsetC.getRefexes();

                    Set<String> csWordSet = new HashSet<String>();
                    Set<String> maybeCsWordSet = new HashSet<String>();
                    for (RefexChronicleBI<?> refex : csWords) {
                        
                        RefexStrVersionBI sv =
                                (RefexStrVersionBI) refex.getVersion(vc);
                        RefexCnidVersionBI cv = 
                                (RefexCnidVersionBI) refex.getVersion(vc);
                        int typeNid = cv.getCnid1();
                        if(typeNid == CaseSensitive.IC_SIGNIFICANT.getLenient().getNid()){
                            csWordSet.add(sv.getStr1());
                        }else{
                            maybeCsWordSet.add(sv.getStr1());
                        }
                    }
                    csWordSetMap.put(CaseSensitive.IC_SIGNIFICANT.getLenient().getNid(), csWordSet);
                    csWordSetMap.put(CaseSensitive.MAYBE_IC_SIGNIFICANT.getLenient().getNid(), maybeCsWordSet);
                }
            } catch (ContraditionException ex) {
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
        for (String token : tokens) {
            if (csWords.contains(text)) {
                return true;
            }
        }
        return false;
    }

    /*public static boolean isMissingDescForDialect(DescriptionVersionBI desc,
    int dialectNid, ViewCoordinate vc) throws IOException,
    ContraditionException, UnsupportedDialectOrLanguage {
    lazyInit();
    if (isTextForDialect(desc.getText(), dialectNid)) {
    return false;
    }
    String dialectText = makeTextForDialect(desc.getText(), dialectNid);
    ConceptVersionBI concept = Ts.get().getConceptVersion(vc,
    desc.getConceptNid());
    for (DescriptionVersionBI d : concept.getDescsActive()) {
    if (d.getText().toLowerCase().equals(dialectText.toLowerCase())) {
    return false;
    }
    }
    return true;
    }*/ 
    

    /*public static boolean isTextForDialect(String text, int dialectNid)
    throws UnsupportedDialectOrLanguage, IOException {
    lazyInit(dialectNid);
    String[] tokens = text.split("\\s+");
    Map<String, String> dialectVariants = variantMap.get(dialectNid);
    for (String token : tokens) {
    if (dialectVariants.containsKey(token.toLowerCase())) {
    return false;
    }
    }
    return true;
    }
    
    public static String makeTextForDialect(String text, int dialectNid)
    throws UnsupportedDialectOrLanguage, IOException {
    lazyInit(dialectNid);
    String[] tokens = text.split("\\s+");
    Map<String, String> dialectVariants = variantMap.get(dialectNid);
    for (int i = 0; i < tokens.length; i++) {
    if (dialectVariants.containsKey(tokens[i].toLowerCase())) {
    boolean upperCase = Character.isUpperCase(tokens[i].charAt(0));
    tokens[i] = dialectVariants.get(tokens[i].toLowerCase());
    if (upperCase) {
    if (Character.isLowerCase(tokens[i].charAt(0))) {
    tokens[i] = Character.toUpperCase(tokens[i].charAt(0)) +
    tokens[i].substring(1);
    }
    }
    }
    }
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < tokens.length; i++) {
    sb.append(tokens[i]);
    if (i < tokens.length - 1) {
    sb.append(' ');
    }
    }
    return sb.toString();
    }*/
}


