package org.ihtsdo.batchbdb;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.management.RuntimeErrorException;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.task.profile.NewDefaultProfile;

import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.tapi.I_ConceptualizeLocally;
import org.dwfa.tapi.impl.LocalFixedTerminology;
import org.dwfa.tapi.impl.MemoryTermServer;
import org.ihtsdo.batch.BatchActionEventReporter;
import org.ihtsdo.batch.BatchActionProcessor;
import org.ihtsdo.batch.BatchActionTask;
import org.ihtsdo.batch.BatchActionTaskParentAddNew;
import org.ihtsdo.batch.BatchActionTaskParentReplace;
import org.ihtsdo.batch.BatchActionTaskParentRetire;
import org.ihtsdo.batch.BatchActionTaskSimple;
import org.ihtsdo.concept.Concept;
import org.ihtsdo.db.bdb.Bdb;
import org.ihtsdo.etypes.EConcept;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.PathBI;
import org.ihtsdo.tk.api.concept.ConceptChronicleBI;
import org.ihtsdo.tk.api.coordinate.EditCoordinate;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.dto.concept.component.refset.TkRefsetAbstractMember;
import org.ihtsdo.tk.dto.concept.component.refset.cid.TkRefsetCidMember;
import org.ihtsdo.tk.example.binding.TermAux;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Based on CementLoadTest.java as the initial example.
 * 
 * @author marc
 *
 */
public class BatchActionTaskTest {

    String dbTarget;
    DataOutputStream eConceptDOS;

    @Ignore
    @Test
    public void batchActionTaskTest() {
        try {
            I_ConfigAceFrame profile = NewDefaultProfile.newProfile("", "", "", "", "");
            int pathNid = ArchitectonicAuxiliary.Concept.ARCHITECTONIC_BRANCH.localize().getNid();
            int userNid = ArchitectonicAuxiliary.Concept.USER.localize().getNid();
            EditCoordinate ec = new EditCoordinate(userNid, pathNid);
            ViewCoordinate vc = profile.getViewCoordinate();
            I_TermFactory terms = Terms.get();
            PathBI editPath = Terms.get().getPath(pathNid); // :!!!: THROWS EXCEPTION
            profile.addEditingPath(editPath);

            UUID isaUuid = TermAux.IS_A.getUuids()[0];

            // 'description type'
            UUID parentMoveFromUuid = UUID.fromString("2d3ecefb-46f5-3dc2-a0ea-43e2c62db1aa");

            // 'definition type'
            UUID parentMoveToUuid = UUID.fromString("26a6e5fd-2f6e-3c53-a4f0-532dbb49b9e1");

            // SETUP CONCEPT LIST
            List<ConceptChronicleBI> concepts = new ArrayList<ConceptChronicleBI>();

            // 'defining'
            UUID parentToAddUuid = UUID.fromString("a4c6bf72-8fb6-11db-b606-0800200c9a66");

            // 'characteristic type' -- add 'defining' parent
            UUID cUuid1 = UUID.fromString("f88e2a66-3a5b-3358-92f0-5b3f5e82b270");
            ConceptChronicleBI c1 = Ts.get().getConcept(cUuid1);
            concepts.add(c1);

            // 'definition type' -- add 'defining' parent
            UUID cUuid2 = UUID.fromString("26a6e5fd-2f6e-3c53-a4f0-532dbb49b9e1");
            ConceptChronicleBI c2 = Ts.get().getConcept(cUuid2);
            concepts.add(c2);

            // 'inferred'
            UUID parentToDelUuid = UUID.fromString("d8fb4fb0-18c3-3352-9431-4919193f85bc");

            // 'subsumed' -- remove the 'inferred' parent, leave the 'stated' parent
            UUID cUuid3 = UUID.fromString("05997863-1f18-3c14-8b3b-059dd2ba28d8");
            ConceptChronicleBI c3 = Ts.get().getConcept(cUuid3);
            concepts.add(c3);

            // 'entry term' -- move from 'description type' parent to 'definition type' parent
            UUID cUuid4 = UUID.fromString("8b3058dd-b236-300a-9a00-dda3a88caa71");
            ConceptChronicleBI c4 = Ts.get().getConcept(cUuid4);
            concepts.add(c4);

            // SETUP BatchActionTaskList
            List<BatchActionTask> batl = new ArrayList<BatchActionTask>();
            batl.add(new BatchActionTaskParentAddNew(isaUuid, parentToAddUuid));
            batl.add(new BatchActionTaskParentRetire(isaUuid, parentToDelUuid));
            batl.add(new BatchActionTaskParentReplace(isaUuid, parentMoveFromUuid, isaUuid, parentMoveToUuid));
            batl.add(new BatchActionTaskSimple());

            // EXERCISE BATCH ACTION TEST
            BatchActionProcessor bap = new BatchActionProcessor(concepts, batl, ec, vc);
            Ts.get().iterateConceptDataInParallel(bap);

            System.out.print(BatchActionEventReporter.createReportTSV());

            // Assert.assertTrue(TF);
            // Assert.assertEquals(A, B);

        } catch (Exception e) {
            try {
                Bdb.close();
            } catch (InterruptedException ex) {
                Logger.getLogger(BatchActionTaskTest.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ExecutionException ex) {
                Logger.getLogger(BatchActionTaskTest.class.getName()).log(Level.SEVERE, null, ex);
            }
            throw new RuntimeException(e);
        } 

    }

    @Before
    public void setUp() throws Exception {
        try {
            dbTarget = "target/" + UUID.randomUUID();
            MemoryTermServer mts = new MemoryTermServer();
            LocalFixedTerminology.setStore(mts);
            mts.setGenerateIds(true);
            ArchitectonicAuxiliary aa = new ArchitectonicAuxiliary();
            aa.addToMemoryTermServer(mts);
            RefsetAuxiliary ra = new RefsetAuxiliary();
            ra.addToMemoryTermServer(mts);
            File directory = new File(dbTarget);
            directory.mkdirs();

            File eConceptsFile = new File(dbTarget, "eConcepts.jbin");
            eConceptsFile.getParentFile().mkdirs();
            BufferedOutputStream eConceptsBos = new BufferedOutputStream(new FileOutputStream(
                    eConceptsFile));
            eConceptDOS = new DataOutputStream(eConceptsBos);
            for (I_ConceptualizeLocally localConcept : mts.getConcepts()) {
                EConcept eC = new EConcept(localConcept, mts);
                if (RefsetAuxiliary.Concept.REFSET_PATHS.getUids().contains(eC.getPrimordialUuid())) {
                    // Add the workbench auxiliary path...
                    TkRefsetCidMember member = new TkRefsetCidMember();
                    member.primordialUuid = UUID.fromString("9353a710-a1c0-11df-981c-0800200c9a66");
                    member.componentUuid = ArchitectonicAuxiliary.Concept.PATH.getPrimoridalUid();
                    member.c1Uuid = ArchitectonicAuxiliary.Concept.ARCHITECTONIC_BRANCH.getPrimoridalUid();
                    member.setRefsetUuid(eC.primordialUuid);
                    member.statusUuid = eC.conceptAttributes.statusUuid;
                    member.authorUuid = eC.conceptAttributes.authorUuid;
                    member.pathUuid = eC.conceptAttributes.pathUuid;
                    member.time = eC.conceptAttributes.time;
                    List<TkRefsetAbstractMember<?>> memberList = new ArrayList<TkRefsetAbstractMember<?>>();
                    memberList.add(member);
                    eC.setRefsetMembers(memberList);
                }
                eC.writeExternal(eConceptDOS);
            }
            eConceptDOS.close();

            Bdb.setup(dbTarget); // Also sets up Terms and Ts
            FileInputStream fis = new FileInputStream(eConceptsFile);
            BufferedInputStream bis = new BufferedInputStream(fis);
            DataInputStream in = new DataInputStream(bis);

            try {
                while (true) {
                    EConcept eConcept = new EConcept(in);
                    Concept newConcept = Concept.get(eConcept);
                    Bdb.getConceptDb().writeConcept(newConcept);
                }
            } catch (EOFException e) {
                in.close();
            }
            System.out.println("finished load, start sync");
            Bdb.sync();
        } catch (Exception e) {
            
            Bdb.close();
            throw new RuntimeException(e);
        }
    }

    @After
    public void tearDown() {
        try {
            // BEGIN TEAR DOWN
            // ... delete created diretory/files
            System.out.println("Finished BatchActionTest");
            Bdb.close();
        } catch (InterruptedException ex) {
            Logger.getLogger(BatchActionTaskTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ExecutionException ex) {
            Logger.getLogger(BatchActionTaskTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
