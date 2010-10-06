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
package org.dwfa.ace.task.search;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.List;
import java.util.logging.Level;

import org.dwfa.ace.api.I_AmTermComponent;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_DescriptionVersioned;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_RelTuple;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.log.AceLog;
import org.dwfa.ace.task.AceTaskUtil;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.jini.TermEntry;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;

@BeanList(specs = { @Spec(directory = "tasks/ide/search", type = BeanType.TASK_BEAN),
                   @Spec(directory = "search", type = BeanType.TASK_BEAN) })
public class RelSubsumptionMatch extends AbstractSearchTest {

    private static final long serialVersionUID = 1;

    private static final int dataVersion = 2;

    /**
     * Rel type concept for the term component to test.
     */
    private TermEntry relTypeTerm = new TermEntry(ArchitectonicAuxiliary.Concept.ACTIVE.getUids());

    /**
     * Rel restriction concept for the term component to test.
     */
    private TermEntry relRestrictionTerm = new TermEntry(
        ArchitectonicAuxiliary.Concept.ARCHITECTONIC_ROOT_CONCEPT.getUids());

    private Boolean applySubsumption = true;

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeObject(this.relTypeTerm);
        out.writeObject(this.relRestrictionTerm);
        out.writeBoolean(applySubsumption);
    }

    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion <= dataVersion) {
            this.relTypeTerm = (TermEntry) in.readObject();
            this.relRestrictionTerm = (TermEntry) in.readObject();
            if (objDataVersion >= 2) {
                applySubsumption = in.readBoolean();
            } else {
                applySubsumption = true;
            }
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }
    }

    @Override
    public boolean test(I_AmTermComponent component, I_ConfigAceFrame frameConfig) throws TaskFailedException {
        try {
            I_GetConceptData relTypeToMatch = null;
            if (relTypeTerm != null) {
                relTypeToMatch = AceTaskUtil.getConceptFromObject(relTypeTerm);
            }
            I_GetConceptData relRestrictionToMatch = null;
            if (relRestrictionTerm != null) {
                relRestrictionToMatch = AceTaskUtil.getConceptFromObject(relRestrictionTerm);
            }

            I_GetConceptData testConcept;
            if (I_GetConceptData.class.isAssignableFrom(component.getClass())) {
                testConcept = (I_GetConceptData) component;
            } else if (I_DescriptionVersioned.class.isAssignableFrom(component.getClass())) {
                I_DescriptionVersioned desc = (I_DescriptionVersioned) component;
                testConcept = LocalVersionedTerminology.get().getConcept(desc.getConceptId());
            } else {
                AceLog.getAppLog().info("Can't handle component: " + component);
                return applyInversion(false);
            }

            if (AceLog.getAppLog().isLoggable(Level.FINE)) {
                AceLog.getAppLog().fine("### testing rel subsumption: " + testConcept);
            }

            List<I_RelTuple> tuples = testConcept.getSourceRelTuples(frameConfig.getAllowedStatus(), null,
                frameConfig.getViewPositionSet(), false);

            for (I_RelTuple tuple : tuples) {
                I_GetConceptData relType = LocalVersionedTerminology.get().getConcept(tuple.getRelTypeId());
                I_GetConceptData relRestriction = LocalVersionedTerminology.get().getConcept(tuple.getC2Id());
                if (AceLog.getAppLog().isLoggable(Level.FINE)) {
                    AceLog.getAppLog().fine(
                        "     relType: " + relType + " relRestriction: " + relRestriction + " relTypeToMatch: "
                            + relTypeToMatch + " relRestrictionToMatch: " + relRestrictionToMatch);
                }
                if (relTypeToMatch != null) {
                    if (applySubsumption) {
                        if (relTypeToMatch.isParentOfOrEqualTo(relType, frameConfig.getAllowedStatus(),
                            frameConfig.getDestRelTypes(), frameConfig.getViewPositionSet(), false)) {
                            if (AceLog.getAppLog().isLoggable(Level.FINE)) {
                                AceLog.getAppLog().fine("  matched type: " + relType);
                            }
                            if (relRestrictionToMatch != null) {
                                if (relRestrictionToMatch.isParentOfOrEqualTo(relRestriction,
                                    frameConfig.getAllowedStatus(), frameConfig.getDestRelTypes(),
                                    frameConfig.getViewPositionSet(), false)) {
                                    if (AceLog.getAppLog().isLoggable(Level.FINE)) {
                                        AceLog.getAppLog().fine("  matched restriction: " + relRestriction);
                                        AceLog.getAppLog().fine("Rel subsumption OK1: " + testConcept);
                                    }
                                    return applyInversion(true);
                                }
                            } else {
                                if (AceLog.getAppLog().isLoggable(Level.FINE)) {
                                    AceLog.getAppLog().fine("Rel subsumption OK2: " + testConcept);
                                }
                                return applyInversion(true);
                            }
                        }
                    } else if (relTypeToMatch.equals(relType)) {
                        if (relRestrictionToMatch != null) {
                            if (relRestrictionToMatch.equals(relRestriction)) {
                                return applyInversion(true);
                            } else {
                                return applyInversion(false);
                            }
                        }
                        return applyInversion(true);
                    }

                } else if (relRestrictionToMatch != null) {
                    if (applySubsumption) {
                        if (relRestrictionToMatch.isParentOfOrEqualTo(relRestriction, frameConfig.getAllowedStatus(),
                            frameConfig.getDestRelTypes(), frameConfig.getViewPositionSet(), false)) {
                            if (AceLog.getAppLog().isLoggable(Level.FINE)) {
                                AceLog.getAppLog().fine("  matched restriction: " + relRestriction);
                                AceLog.getAppLog().fine("Rel subsumption OK3: " + testConcept);
                            }
                            return applyInversion(true);
                        }
                    } else {
                        if (relRestrictionToMatch.equals(relRestriction)) {
                            return applyInversion(true);
                        } else {
                            return applyInversion(false);
                        }
                    }
                } else {
                    return applyInversion(true);
                }
            }
            return applyInversion(false);
        } catch (TerminologyException e) {
            throw new TaskFailedException(e);
        } catch (IOException e) {
            throw new TaskFailedException(e);
        }
    }

    public TermEntry getRelTypeTerm() {
        return relTypeTerm;
    }

    public void setRelTypeTerm(TermEntry relTypeTerm) {
        this.relTypeTerm = relTypeTerm;
    }

    public TermEntry getRelRestrictionTerm() {
        return relRestrictionTerm;
    }

    public void setRelRestrictionTerm(TermEntry relRestrictionTerm) {
        this.relRestrictionTerm = relRestrictionTerm;
    }

    public Boolean getApplySubsumption() {
        return applySubsumption;
    }

    public void setApplySubsumption(Boolean applySubsumption) {
        this.applySubsumption = applySubsumption;
    }

}
