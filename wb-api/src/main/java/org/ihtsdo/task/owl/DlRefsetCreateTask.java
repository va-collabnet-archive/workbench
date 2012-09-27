/*
 * Copyright 2012 International Health Terminology Standards Development Organisation.
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
package org.ihtsdo.task.owl;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.dwfa.ace.api.Terms;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;
import org.dwfa.util.id.Type5UuidFactory;
import org.ihtsdo.helper.descriptionlogic.DescriptionLogic;
import org.ihtsdo.lang.LANG_CODE;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.ContradictionException;
import org.ihtsdo.tk.api.TerminologyBuilderBI;
import org.ihtsdo.tk.api.TerminologyStoreDI;
import org.ihtsdo.tk.api.blueprint.ConceptCB;
import org.ihtsdo.tk.api.blueprint.DescriptionCAB;
import org.ihtsdo.tk.api.blueprint.InvalidCAB;
import org.ihtsdo.tk.api.blueprint.RelationshipCAB;
import org.ihtsdo.tk.api.concept.ConceptChronicleBI;
import org.ihtsdo.tk.api.coordinate.EditCoordinate;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.binding.snomed.Snomed;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRfx;
import org.ihtsdo.tk.dto.concept.component.relationship.TkRelationshipType;

/**
 *
 * @author marc
 */
@BeanList(specs = {
    @Spec(directory = "tasks/owl", type = BeanType.TASK_BEAN)})
public class DlRefsetCreateTask extends AbstractTask implements ActionListener {

    private static final long serialVersionUID = 1L;
    private static final int dataVersion = 1;
    private boolean continueThisAction;

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
    }

    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();
    }

    /**
     * AbstractTask method
     * @param process
     * @param worker
     * @return
     * @throws TaskFailedException
     * @throws ContradictionException 
     */
    @Override
    public Condition evaluate(I_EncodeBusinessProcess process, I_Work worker)
            throws TaskFailedException, ContradictionException {
        try {
            createRefsetConcepts();
        } catch (IOException ex) {
            Logger.getLogger(DlRefsetCreateTask.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InvalidCAB ex) {
            Logger.getLogger(DlRefsetCreateTask.class.getName()).log(Level.SEVERE, null, ex);
        } catch (TerminologyException ex) {
            Logger.getLogger(DlRefsetCreateTask.class.getName()).log(Level.SEVERE, null, ex);
        }
        return Condition.CONTINUE;
    }

    /**
     * AbstractTask method complete
     * @param process
     * @param worker
     * @throws TaskFailedException
     */
    @Override
    public void complete(I_EncodeBusinessProcess process, I_Work worker)
            throws TaskFailedException {
        // Nothing to do
    }

    /**
     * AbstractTask method getConditions()
     * @return
     */
    @Override
    public Collection<Condition> getConditions() {
        return CONTINUE_CONDITION;
    }

    /**
     * ActionListener method actionPerformed sets an internal flag to stop processing
     * @param e
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        continueThisAction = false;
    }
    private final String REFSET_ID_NAMESPACE_UUID_TYPE1 = "d0b3c9c0-e395-11df-bccf-0800200c9a66";

    private void createRefsetConcepts()
            throws IOException, InvalidCAB, ContradictionException, TerminologyException {
        TerminologyStoreDI ts = Ts.get();

        // 
        UUID isaUuid = ArchitectonicAuxiliary.Concept.IS_A_REL.getPrimoridalUid();
        UUID desFsnTypeUuid =
                ts.getConcept(SnomedMetadataRfx.getDES_FULL_SPECIFIED_NAME_NID()).getPrimUuid();
        UUID desPtTypeUuid = ts.getConcept(SnomedMetadataRfx.getDESC_PREFERRED_NID()).getPrimUuid();

        //
        UUID auxPathUuid = ArchitectonicAuxiliary.Concept.ARCHITECTONIC_BRANCH.getPrimoridalUid();
        int authorNid = ts.getNidForUuids(ArchitectonicAuxiliary.Concept.USER.getUids());
        int editPathNid = ts.getNidForUuids(auxPathUuid);
        int moduleNid = Snomed.CORE_MODULE.getLenient().getNid();
        EditCoordinate ec = new EditCoordinate(authorNid, moduleNid, editPathNid);
        ViewCoordinate vc = Terms.get().getActiveAceFrameConfig().getViewCoordinate();
        TerminologyBuilderBI tsSnapshot = ts.getTerminologyBuilder(ec, vc);

        // "refset"
        UUID refsetParentUuid = UUID.fromString("3e0cd740-2cc6-3d68-ace7-bad2eb2621da");

        // Description Logic Refset
        // "c7942fa6-98a6-50a7-a4fd-24e5574c2a5d"
        UUID dlRefsetUuid = (DescriptionLogic.DESCRIPTION_LOGIC_REFSET.getUuids())[0];

        ConceptCB cb = new ConceptCB("Description logic refset",
                "Description logic refset",
                LANG_CODE.EN_CA,
                isaUuid,
                refsetParentUuid);
        cb.setComponentUuid(dlRefsetUuid);
        ConceptChronicleBI ccbi = tsSnapshot.constructIfNotCurrent(cb);
        ts.addUncommitted(ccbi);

        // Disjoint Sets
        UUID disjointSetsRefsetUuid = (DescriptionLogic.DISJOINT_SETS_REFSET.getUuids())[0];
        cb = new ConceptCB("Disjoint sets refset",
                "Disjoint sets refset",
                LANG_CODE.EN_CA,
                isaUuid,
                dlRefsetUuid);
        cb.setComponentUuid(disjointSetsRefsetUuid);
        ts.addUncommitted(tsSnapshot.constructIfNotCurrent(cb));

        // Negation
        UUID negationRefsetUuid = (DescriptionLogic.NEGATION_REFSET.getUuids())[0];
        cb = new ConceptCB("Negation refset",
                "Negation refset",
                LANG_CODE.EN_CA,
                isaUuid,
                dlRefsetUuid);
        cb.setComponentUuid(negationRefsetUuid);
        ts.addUncommitted(tsSnapshot.constructIfNotCurrent(cb));

        // Union Sets Refset
        UUID unionSetsRefsetUuid = (DescriptionLogic.UNION_SETS_REFSET.getUuids())[0];
        cb = new ConceptCB("Union sets refset",
                "Union sets refset",
                LANG_CODE.EN_CA,
                isaUuid,
                dlRefsetUuid);
        cb.setComponentUuid(unionSetsRefsetUuid);
        ts.addUncommitted(tsSnapshot.constructIfNotCurrent(cb));

        // ConDOR reasoner
        UUID condorUuid = (DescriptionLogic.CONDOR_REASONER.getUuids())[0];
        cb = new ConceptCB("ConDOR Reasoner",
                "ConDOR",
                LANG_CODE.EN_CA,
                isaUuid,
                UUID.fromString("f7495b58-6630-3499-a44e-2052b5fcf06c")); /* parent == user */
        cb.setComponentUuid(condorUuid);
        ts.addUncommitted(tsSnapshot.constructIfNotCurrent(cb));

        //        RelationshipCAB rc = new RelationshipCAB(dlRefsetUuid, // sourceUuid
//                isaUuid, // typeUuid
//                refsetParentUuid, // UUID targetUuid
//                0, // group
//                TkRelationshipType.STATED_HIERARCHY);
//        rc.setComponentUuid(dlRefsetUuid);
//        tsSnapshot.construct(rc);
//
//        DescriptionCAB dcfsn = new DescriptionCAB(isaUuid, // conceptUuid
//                desFsnTypeUuid, // typeUuid
//                LANG_CODE.EN,
//                "Description logic refset",
//                false); // initialCaseSignificant
//        // dcfsn.setComponentUuid(primordialUuid);
//        tsSnapshot.construct(dcfsn);
//
//        DescriptionCAB dcpt = new DescriptionCAB(isaUuid, // conceptUuid
//                desPtTypeUuid, // typeUuid
//                LANG_CODE.EN,
//                "Description logic refset",
//                false); // initialCaseSignificant
//        tsSnapshot.construct(dcpt);

    }

    private UUID uuidFrom(String s)
            throws NoSuchAlgorithmException, UnsupportedEncodingException {
        return Type5UuidFactory.get(REFSET_ID_NAMESPACE_UUID_TYPE1 + s);
    }
}
