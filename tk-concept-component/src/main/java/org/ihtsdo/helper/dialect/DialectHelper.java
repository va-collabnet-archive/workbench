/**
 * Copyright (c) 2012 International Health Terminology Standards Development
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
package org.ihtsdo.helper.dialect;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.ContradictionException;
import org.ihtsdo.tk.api.TerminologySnapshotDI;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.api.description.DescriptionVersionBI;
import org.ihtsdo.tk.api.refex.RefexChronicleBI;
import org.ihtsdo.tk.api.refex.type_string.RefexStringVersionBI;
import org.ihtsdo.tk.binding.snomed.Language;
import org.ihtsdo.tk.spec.ConceptSpec;
import org.ihtsdo.tk.spec.DescriptionSpec;
import org.ihtsdo.tk.spec.SpecFactory;
import org.ihtsdo.tk.uuid.UuidT5Generator;

// TODO: Auto-generated Javadoc
/**
 * The Class DialectHelper. Loads the lists of dialect variant words from a file into refsets.
 *
 */
public class DialectHelper {

    /** The variant map. */
    private static Map<Integer, Map<String, String>> variantMap = null;
    
    /** The variant set map. */
    private static Map<Integer, Set<String>> variantSetMap = null;
    
    /** The initialization lock. */
    private static Lock initLock = new ReentrantLock();

    /**
     * Imports the dialect variant words into the refset for the dialect, a child of language refset, if the refset does not contain members.
     *
     * @param dialectOrLanguageNid the dialect or language nid
     * @throws UnsupportedDialectOrLanguage the unsupported dialect or language
     * @throws IOException signals that an I/O exception has occurred.
     */
    private static void lazyInit(int dialectOrLanguageNid)
            throws UnsupportedDialectOrLanguage, IOException {
        if (variantMap == null) {
            initLock.lock();
            try {
                if (variantMap == null) {
                    HashMap<Integer, Map<String, String>> initialVariantMap =
                            new HashMap<Integer, Map<String, String>>();
                    variantSetMap = new HashMap<Integer, Set<String>>();
                    ViewCoordinate vc = Ts.get().getMetadataViewCoordinate();
                    TerminologySnapshotDI ts = Ts.get().getSnapshot(vc);

                    ConceptVersionBI enVariantTextRefsetC =
                            Language.EN_VARIANT_TEXT.get(Ts.get().getMetadataViewCoordinate());
                    Collection<? extends RefexChronicleBI<?>> enVariants =
                            enVariantTextRefsetC.getRefexes();
                    Set<String> variantSet = new HashSet<String>();
                    for (RefexChronicleBI<?> refex : enVariants) {
                        if(RefexStringVersionBI.class.isAssignableFrom(refex.getClass())){
                            RefexStringVersionBI variantText =
                                (RefexStringVersionBI) refex.getVersion(vc);
                            if (variantText != null) {
                                variantSet.add(variantText.getString1());
                            }
                        }
                    }
                    variantSetMap.put(Language.EN.get(vc).getNid(), variantSet);

                    addDialect(Language.EN_AU, vc, Language.EN_AU_TEXT_VARIANTS,
                            ts, initialVariantMap);
                    addDialect(Language.EN_CA, vc, Language.EN_CA_TEXT_VARIANTS,
                            ts, initialVariantMap);
                    addDialect(Language.EN_NZ, vc, Language.EN_NZ_TEXT_VARIANTS,
                            ts, initialVariantMap);
                    addDialect(Language.EN_UK, vc, Language.EN_UK_TEXT_VARIANTS,
                            ts, initialVariantMap);
                    addDialect(Language.EN_US, vc, Language.EN_US_TEXT_VARIANTS,
                            ts, initialVariantMap);
                    DialectHelper.variantMap = initialVariantMap;
                }
            } catch (ContradictionException ex) {
                throw new IOException(ex);
            } finally {
                initLock.unlock();
            }
        }
        if (!variantMap.containsKey(dialectOrLanguageNid)
                && !variantSetMap.containsKey(dialectOrLanguageNid)) {
            throw new UnsupportedDialectOrLanguage("nid: " + dialectOrLanguageNid);
        }
    }

    /**
     * Adds the dialect.
     *
     * @param dialectSpec the dialect spec
     * @param vc the vc
     * @param varientsSpec the varients spec
     * @param ts the ts
     * @param initialVariantMap the initial variant map
     * @throws ContradictionException the contradiction exception
     * @throws IOException signals that an I/O exception has occurred.
     */
    private static void addDialect(ConceptSpec dialectSpec,
            ViewCoordinate vc,
            ConceptSpec varientsSpec,
            TerminologySnapshotDI ts,
            HashMap<Integer, Map<String, String>> initialVariantMap) throws ContradictionException, IOException {
        ConceptVersionBI dialectC = dialectSpec.get(vc);
        ConceptVersionBI variantTextRefsetC = varientsSpec.get(vc);

        Collection<? extends RefexChronicleBI<?>> dialectVarients =
                variantTextRefsetC.getActiveRefsetMembers();
        Map<String, String> variantDialectMap = new HashMap<String, String>();
        for (RefexChronicleBI<?> refex : dialectVarients) {
            if(RefexStringVersionBI.class.isAssignableFrom(refex.getClass())){
                RefexStringVersionBI dialectText = (RefexStringVersionBI) refex.getVersion(vc);
                if (dialectText != null) {
                    RefexStringVersionBI variantText = (RefexStringVersionBI) ts.getComponentVersion(dialectText.getReferencedComponentNid());
                    variantDialectMap.put(variantText.getString1(), dialectText.getString1());
                }
            }
        }
        initialVariantMap.put(dialectC.getNid(), variantDialectMap);
    }

    /**
     * Checks if is missing desc for dialect.
     *
     * @param desc the desc
     * @param dialectNid the dialect nid
     * @param vc the vc
     * @return <code>true</code>, if is missing desc for dialect
     * @throws IOException signals that an I/O exception has occurred.
     * @throws ContradictionException the contradiction exception
     * @throws UnsupportedDialectOrLanguage the unsupported dialect or language
     */
    public static boolean isMissingDescForDialect(DescriptionVersionBI desc,
            int dialectNid, ViewCoordinate vc) throws IOException,
            ContradictionException, UnsupportedDialectOrLanguage {
        lazyInit(dialectNid);
        if (isTextForDialect(desc.getText(), dialectNid)) {
            return false;
        }
        String dialectText = makeTextForDialect(desc.getText(), dialectNid);
        ConceptVersionBI concept = Ts.get().getConceptVersion(vc,
                desc.getConceptNid());
        for (DescriptionVersionBI d : concept.getDescriptionsActive()) {
            if (d.getText().toLowerCase().equals(dialectText.toLowerCase())) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks for dialect variants.
     *
     * @param text the text
     * @param languageNid the language nid
     * @return <code>true</code>, if successful
     * @throws UnsupportedDialectOrLanguage the unsupported dialect or language
     * @throws IOException signals that an I/O exception has occurred.
     */
    public static boolean hasDialectVariants(String text, int languageNid)
            throws UnsupportedDialectOrLanguage, IOException {
        lazyInit(languageNid);
        String[] tokens = text.split("\\s+");
        Set<String> dialectVariants = variantSetMap.get(languageNid);
        for (String token : tokens) {
            if (dialectVariants.contains(token.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if is text for dialect.
     *
     * @param text the text
     * @param dialectNid the dialect nid
     * @return <code>true</code>, if is text for dialect
     * @throws UnsupportedDialectOrLanguage the unsupported dialect or language
     * @throws IOException signals that an I/O exception has occurred.
     */
    public static boolean isTextForDialect(String text, int dialectNid)
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

    /**
     * Make text for dialect.
     *
     * @param text the text
     * @param dialectNid the dialect nid
     * @return the string
     * @throws UnsupportedDialectOrLanguage the unsupported dialect or language
     * @throws IOException signals that an I/O exception has occurred.
     */
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
                        tokens[i] = Character.toUpperCase(tokens[i].charAt(0))
                                + tokens[i].substring(1);
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
    }

    /**
     * Gets the description spec for dialect.
     *
     * @param desc the desc
     * @param dialectSpec the dialect spec
     * @param vc the vc
     * @return the description spec for dialect
     * @throws UnsupportedDialectOrLanguage the unsupported dialect or language
     * @throws IOException signals that an I/O exception has occurred.
     */
    public static DescriptionSpec getDescriptionSpecForDialect(
            DescriptionVersionBI desc,
            ConceptSpec dialectSpec, ViewCoordinate vc)
            throws UnsupportedDialectOrLanguage, IOException {
        return getDescriptionSpecForDialect(
                desc,
                dialectSpec.get(vc).getNid(), vc);
    }

    /**
     * Gets the description spec for dialect.
     *
     * @param desc the desc
     * @param dialectNid the dialect nid
     * @param vc the vc
     * @return the description spec for dialect
     * @throws UnsupportedDialectOrLanguage the unsupported dialect or language
     * @throws IOException signals that an I/O exception has occurred.
     */
    public static DescriptionSpec getDescriptionSpecForDialect(
            DescriptionVersionBI desc,
            int dialectNid, ViewCoordinate vc)
            throws UnsupportedDialectOrLanguage, IOException {
        try {
            lazyInit(dialectNid);
            String variantText = makeTextForDialect(desc.getText(), dialectNid);

            UUID descUuid = UuidT5Generator.getDescUuid(desc.getText(),
                    Ts.get().getConcept(dialectNid).getPrimUuid(),
                    Ts.get().getConcept(desc.getConceptNid()).getPrimUuid());

            DescriptionSpec ds = new DescriptionSpec(new UUID[]{descUuid},
                    SpecFactory.get(Ts.get().getConcept(desc.getConceptNid()), vc),
                    SpecFactory.get(Ts.get().getConcept(desc.getTypeNid()), vc),
                    variantText);
            ds.setLangText(desc.getLang());
            return ds;
        } catch (NoSuchAlgorithmException ex) {
            throw new IOException(ex);
        } catch (UnsupportedEncodingException ex) {
            throw new IOException(ex);
        }
    }
}
