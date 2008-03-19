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

@BeanList(specs = { @Spec(directory = "tasks/ace/search", type = BeanType.TASK_BEAN),
        @Spec(directory = "search", type = BeanType.TASK_BEAN) })
public class RelSubsumptionMatch extends AbstractSearchTest {

    private static final long serialVersionUID = 1;

    private static final int dataVersion = 1;

    /**
     * Rel type concept for the term component to test.
     */
    private TermEntry relTypeTerm = new TermEntry(ArchitectonicAuxiliary.Concept.ACTIVE.getUids());

    /**
     * Rel restriction concept for the term component to test.
     */
    private TermEntry relRestrictionTerm = new TermEntry(ArchitectonicAuxiliary.Concept.ARCHITECTONIC_ROOT_CONCEPT
            .getUids());
    
    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeObject(this.relTypeTerm);
        out.writeObject(this.relRestrictionTerm);
    }

    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion == 1) {
            this.relTypeTerm = (TermEntry) in.readObject();
            this.relRestrictionTerm = (TermEntry) in.readObject();
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }
    }

    @Override
    public boolean test(I_AmTermComponent component, I_ConfigAceFrame frameConfig) throws TaskFailedException {
        try {
            I_GetConceptData relTypeToMatch = AceTaskUtil.getConceptFromObject(relTypeTerm);
            I_GetConceptData relRestrictionToMatch = AceTaskUtil.getConceptFromObject(relRestrictionTerm);

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

            List<I_RelTuple> tuples = testConcept.getSourceRelTuples(frameConfig.getAllowedStatus(), null, frameConfig
                    .getViewPositionSet(), false);

            for (I_RelTuple tuple : tuples) {
                I_GetConceptData relType = LocalVersionedTerminology.get().getConcept(tuple.getRelTypeId());
                I_GetConceptData relRestriction = LocalVersionedTerminology.get().getConcept(tuple.getC2Id());
                if (AceLog.getAppLog().isLoggable(Level.FINE)) {
                    AceLog.getAppLog().fine("     relType: " + relType + 
                                            " relRestriction: " + relRestriction + 
                                            " relTypeToMatch: " + relTypeToMatch + 
                                            " relRestrictionToMatch: " + relRestrictionToMatch);
                }
                if (relTypeToMatch.isParentOfOrEqualTo(relType, frameConfig.getAllowedStatus(), frameConfig.getDestRelTypes(),
                                              frameConfig.getViewPositionSet(), false)) {
                    if (AceLog.getAppLog().isLoggable(Level.FINE)) {
                        AceLog.getAppLog().fine("  matched type: " + relType);
                    }
                    if (relRestrictionToMatch.isParentOfOrEqualTo(relRestriction, frameConfig.getAllowedStatus(), frameConfig
                            .getDestRelTypes(), frameConfig.getViewPositionSet(), false)) {
                        if (AceLog.getAppLog().isLoggable(Level.FINE)) {
                            AceLog.getAppLog().fine("  matched restriction: " + relRestriction);
                        }
                        AceLog.getAppLog().info("Rel subsumption OK: " + testConcept.getUids());
                        return applyInversion(true);
                    }
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

}
