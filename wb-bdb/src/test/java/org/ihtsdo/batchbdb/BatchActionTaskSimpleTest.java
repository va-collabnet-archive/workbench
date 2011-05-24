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
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.task.profile.NewDefaultProfile;

import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.I_ConceptualizeLocally;
import org.dwfa.tapi.impl.LocalFixedTerminology;
import org.dwfa.tapi.impl.MemoryTermServer;
import org.ihtsdo.batch.BatchActionEventReporter;
import org.ihtsdo.batch.BatchActionProcessor;
import org.ihtsdo.batch.BatchActionTask;
import org.ihtsdo.batch.BatchActionTaskSimple;
import org.ihtsdo.concept.Concept;
import org.ihtsdo.db.bdb.Bdb;
import org.ihtsdo.etypes.EConcept;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.concept.ConceptChronicleBI;
import org.ihtsdo.tk.api.coordinate.EditCoordinate;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
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
public class BatchActionTaskSimpleTest {

    String dbTarget;
    DataOutputStream eConceptDOS;

    @Ignore
    @Test
    public void batchActionTaskTest() {
        try {
            // :!!!: yank vc comment if not needed to create second type of exception.
            // ViewCoordinate vc = Ts.get().getMetadataVC();
            I_ConfigAceFrame profile = NewDefaultProfile.newProfile("", "", "", "", "");
            int pathNid = ArchitectonicAuxiliary.Concept.ARCHITECTONIC_BRANCH.localize().getNid();
            int userNid = ArchitectonicAuxiliary.Concept.USER.localize().getNid();
            EditCoordinate ec = new EditCoordinate(userNid, pathNid);
            ViewCoordinate vc = profile.getViewCoordinate();

            // SETUP CONCEPT LIST
            List<ConceptChronicleBI> concepts = new ArrayList<ConceptChronicleBI>();

            // 'characteristic type'
            UUID u1 = UUID.fromString("f88e2a66-3a5b-3358-92f0-5b3f5e82b270");
            ConceptChronicleBI c1 = Ts.get().getConcept(u1);

            // 'definition type'
            UUID u2 = UUID.fromString("26a6e5fd-2f6e-3c53-a4f0-532dbb49b9e1");
            ConceptChronicleBI c2 = Ts.get().getConcept(u2);

            concepts.add(c1);
            concepts.add(c2);

            // SETUP BatchActionTaskList
            List<BatchActionTask> batl = new ArrayList<BatchActionTask>();
            batl.add(new BatchActionTaskSimple());

            // EXERCISE BATCH ACTION TEST
            BatchActionProcessor bap = new BatchActionProcessor(concepts, batl, ec, vc);
            Ts.get().iterateConceptDataInParallel(bap);

            Logger.getLogger(BatchActionTaskSimpleTest.class.getName()).log(Level.INFO, BatchActionEventReporter.createReportTSV());

        } catch (Exception e) {
            try {
                Bdb.close();
            } catch (InterruptedException ex) {
                Logger.getLogger(BatchActionTaskSimpleTest.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ExecutionException ex) {
                Logger.getLogger(BatchActionTaskSimpleTest.class.getName()).log(Level.SEVERE, null, ex);
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
            File directory = new File(dbTarget);
            directory.mkdirs();

            File eConceptsFile = new File(dbTarget, "eConcepts.jbin");
            eConceptsFile.getParentFile().mkdirs();
            BufferedOutputStream eConceptsBos = new BufferedOutputStream(new FileOutputStream(
                    eConceptsFile));
            eConceptDOS = new DataOutputStream(eConceptsBos);
            for (I_ConceptualizeLocally localConcept : mts.getConcepts()) {
                EConcept eC = new EConcept(localConcept, mts);
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
            Assert.fail(e.toString());
            Bdb.close();
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
            Logger.getLogger(BatchActionTaskSimpleTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ExecutionException ex) {
            Logger.getLogger(BatchActionTaskSimpleTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
