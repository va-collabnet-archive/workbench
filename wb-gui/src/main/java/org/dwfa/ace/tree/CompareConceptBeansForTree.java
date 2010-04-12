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
package org.dwfa.ace.tree;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.api.ebr.I_ExtendByRef;
import org.dwfa.ace.api.ebr.I_ExtendByRefVersion;
import org.dwfa.ace.log.AceLog;
import org.dwfa.tapi.TerminologyException;

public class CompareConceptBeansForTree implements Comparator<I_GetConceptDataForTree> {

    private I_ConfigAceFrame aceConfig;

    public CompareConceptBeansForTree(I_ConfigAceFrame aceConfig) {
        super();
        this.aceConfig = aceConfig;
    }

    public int compare(I_GetConceptDataForTree cb1, I_GetConceptDataForTree cb2) {
        try {
            if (cb1 == cb2) {
                return 0;
            }
            if (cb1 == null) {
                return 1;
            }
            if (cb2 == null) {
                return -1;
            }
            if (this.aceConfig.getSortTaxonomyUsingRefset()) {
                if (AceLog.getAppLog().isLoggable(Level.FINE)) {
                    AceLog.getAppLog().fine("Sorting using refset");
                }
                return compareRefset(cb1, cb2);
            } else {
                return compareDescriptions(cb1, cb2);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (TerminologyException e) {
            throw new RuntimeException(e);
        }
    }

    private int compareRefset(I_GetConceptDataForTree cb1, I_GetConceptDataForTree cb2) throws IOException, TerminologyException {
        List<? extends I_ExtendByRef> c1Extensions = getExtensions(cb1);
        List<? extends I_ExtendByRef> c2Extensions = getExtensions(cb2);
        if (AceLog.getAppLog().isLoggable(Level.FINE)) {
            AceLog.getAppLog().fine("c1Extensions: " + c1Extensions.size() + " " + c1Extensions);
            AceLog.getAppLog().fine("c2Extensions: " + c2Extensions.size() + " " + c2Extensions);
        }

        if (c1Extensions.size() > 0) {
            if (AceLog.getAppLog().isLoggable(Level.FINE)) {
                AceLog.getAppLog().fine("A");
            }
            if (c2Extensions.size() > 0) {
                if (AceLog.getAppLog().isLoggable(Level.FINE)) {
                    AceLog.getAppLog().fine("C");
                }
                for (int refsetId : aceConfig.getRefsetsToSortTaxonomy().getListValues()) {
                    I_ExtendByRefVersion c1ExtTuple = getExtensionTuple(c1Extensions, refsetId);
                    I_ExtendByRefVersion c2ExtTuple = getExtensionTuple(c2Extensions, refsetId);
                    if (c1ExtTuple != c2ExtTuple) {
                        if (c1ExtTuple == null) {
                            return 1;
                        } else if (c2ExtTuple == null) {
                            return -1;
                        }
                        int comparison = c1ExtTuple.getMutablePart().compareTo(c2ExtTuple.getMutablePart());
                        if (comparison != 0) {
                            return comparison;
                        } else {
                            return compareDescriptions(cb1, cb2);
                        }

                    }
                }
            } else {
                for (int refsetId : aceConfig.getRefsetsToSortTaxonomy().getListValues()) {
                    I_ExtendByRefVersion c1ExtTuple = getExtensionTuple(c1Extensions, refsetId);
                    if (c1ExtTuple != null) {
                        if (AceLog.getAppLog().isLoggable(Level.FINE)) {
                            AceLog.getAppLog().fine("Found tuple for c1: " + c1ExtTuple);
                        }
                        return -1;
                    }
                }
            }
        } else {
            if (c2Extensions.size() > 0) {
                if (AceLog.getAppLog().isLoggable(Level.FINE)) {
                    AceLog.getAppLog().fine("B: " + aceConfig.getRefsetsToSortTaxonomy().getListValues());
                }
                for (int refsetId : aceConfig.getRefsetsToSortTaxonomy().getListValues()) {
                    if (AceLog.getAppLog().isLoggable(Level.FINE)) {
                        AceLog.getAppLog().fine("Testing: " + refsetId);
                    }
                    I_ExtendByRefVersion c2ExtTuple = getExtensionTuple(c2Extensions, refsetId);
                    if (AceLog.getAppLog().isLoggable(Level.FINE)) {
                        AceLog.getAppLog().fine("c2ExtTuple: " + c2ExtTuple);
                    }
                    if (c2ExtTuple != null) {
                        if (AceLog.getAppLog().isLoggable(Level.FINE)) {
                            AceLog.getAppLog().fine("Found tuple for c2: " + c2ExtTuple);
                        }
                        return 1;
                    }
                }
            }
        }
        if (AceLog.getAppLog().isLoggable(Level.FINE)) {
            AceLog.getAppLog().fine("no refsets, using description sort. ");
        }
        return compareDescriptions(cb1, cb2);
    }

    private I_ExtendByRefVersion getExtensionTuple(List<? extends I_ExtendByRef> c1Extensions, int refsetId) throws IOException, TerminologyException {
        for (I_ExtendByRef ext : c1Extensions) {
            if (AceLog.getAppLog().isLoggable(Level.FINE)) {
                AceLog.getAppLog().fine("Getting tuples for: " + c1Extensions);
                AceLog.getAppLog().fine("Extension: " + ext);
                AceLog.getAppLog().fine("refset id: " + ext.getRefsetId());
            }
            if (ext.getRefsetId() == refsetId) {
                if (AceLog.getAppLog().isLoggable(Level.FINE)) {
                    AceLog.getAppLog().fine("Matched refset id: " + refsetId);
                }
                List<I_ExtendByRefVersion> returnTuples = new ArrayList<I_ExtendByRefVersion>();
                ext.addTuples(this.aceConfig.getAllowedStatus(), 
                		this.aceConfig.getViewPositionSetReadOnly(),
                    returnTuples, 
                    aceConfig.getPrecedence(), aceConfig.getConflictResolutionStrategy());
                if (returnTuples.size() > 0) {
                    return returnTuples.get(0);
                }
                if (AceLog.getAppLog().isLoggable(Level.FINE)) {
                    AceLog.getAppLog().fine("No tuple for match: " + ext.getMutableParts());
                }
                return null;
            }
        }
        return null;
    }

    private List<? extends I_ExtendByRef> getExtensions(I_GetConceptDataForTree cb1) throws IOException {
        // May need to compare based on the relationship between the parent node
        // and this node
        // or based on the node and this node. Need to get extensions on the rel
        // and on the concept.
        List<I_ExtendByRef> extensions = (List<I_ExtendByRef>) Terms.get().getAllExtensionsForComponent(cb1.getConceptId());
        extensions.addAll((Collection<I_ExtendByRef>) Terms.get().getAllExtensionsForComponent(cb1.getRelId()));
        return extensions;
    }

    private int compareDescriptions(I_GetConceptDataForTree cb1, I_GetConceptDataForTree cb2) throws IOException {
        I_DescriptionTuple cb1dt = cb1.getDescTuple(aceConfig);
        I_DescriptionTuple cb2dt = cb2.getDescTuple(aceConfig);

        if (cb1dt == cb2dt) {
            return cb1.getConceptId() - cb2.getConceptId();
        }
        if (cb1dt == null || cb1dt.getText() == null) {
            return 1;
        }
        if (cb2dt == null || cb2dt.getText() == null) {
            return -1;
        }
        int comparison = cb1dt.getText().toLowerCase().compareTo(cb2dt.getText().toLowerCase());
        if (comparison == 0) {
            comparison = cb1dt.getText().compareTo(cb2dt.getText());
        }
        if (comparison == 0) {
            comparison = cb1.getConceptId() - cb2.getConceptId();
        }
        return comparison;
    }

}
