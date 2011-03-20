/*
 * Copyright 2001-2005 The Apache Software Foundation.
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
package org.ihtsdo.bdb.mojo;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.cement.RefsetAuxiliary;
import org.ihtsdo.tk.spec.ConceptSpec;
import org.dwfa.util.io.FileIO;
import org.ihtsdo.concept.Concept;
import org.ihtsdo.concept.component.attributes.ConceptAttributesBinder;
import org.ihtsdo.concept.component.description.DescriptionBinder;
import org.ihtsdo.concept.component.refset.RefsetMemberBinder;
import org.ihtsdo.concept.component.refset.RefsetMemberFactory;
import org.ihtsdo.concept.component.relationship.RelationshipBinder;
import org.ihtsdo.db.bdb.Bdb;
import org.ihtsdo.db.bdb.BdbCommitManager;
import org.ihtsdo.db.bdb.computer.ReferenceConcepts;
import org.ihtsdo.db.bdb.id.NidCNidMapBdb;
import org.ihtsdo.etypes.EConcept;
import org.ihtsdo.lucene.LuceneManager;
import org.ihtsdo.thread.NamedThreadFactory;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.blueprint.RefexCUB;
import org.ihtsdo.tk.api.TerminologyConstructorBI;
import org.ihtsdo.tk.api.blueprint.RefexCUB.RefexProperty;
import org.ihtsdo.tk.api.concept.ConceptChronicleBI;
import org.ihtsdo.tk.api.coordinate.EditCoordinate;
import org.ihtsdo.tk.api.description.DescriptionChronicleBI;
import org.ihtsdo.tk.api.description.DescriptionVersionBI;
import org.ihtsdo.tk.api.refex.RefexChronicleBI;
import org.ihtsdo.tk.dto.concept.component.refset.TkRefsetType;
import org.ihtsdo.tk.spec.PathSpec;

/**
 * Goal which loads an EConcept.jbin file into a bdb.
 * 
 * @goal load-econcepts-multi
 * 
 * @phase process-sources
 */
public class LoadBdbMulti extends AbstractMojo {

    /**
     * concepts file names.
     * 
     * @parameter default-value={"eConcepts.jbin"}
     * @required
     */
    private String[] conceptsFileNames;
    /**
     * rsta file names.
     * 
     * @parameter
     */
    private String[] rstaFileNames;
    /**
     * Generated resources directory.
     * 
     * @parameter expression="${project.build.directory}/generated-resources"
     * @required
     */
    private String generatedResources;
    /**
     * Berkeley directory.
     * 
     * @parameter expression="${project.build.directory}/berkeley-db"
     * @required
     */
    private File berkeleyDir;
    /**
     * 
     * @parameter default-value=true
     */
    private boolean moveToReadOnly;
    /**
     * Paths to add to initial database.
     * 
     * @parameter
     */
    private PathSpec[] initialPaths;
    AtomicInteger conceptsRead = new AtomicInteger();
    AtomicInteger conceptsProcessed = new AtomicInteger();
    private ThreadGroup loadBdbMultiDbThreadGroup = new ThreadGroup("LoadBdbMulti threads");
    ExecutorService executors = Executors.newCachedThreadPool(
            new NamedThreadFactory(loadBdbMultiDbThreadGroup, "converter "));
    LinkedBlockingQueue<I_ProcessEConcept> converters = new LinkedBlockingQueue<I_ProcessEConcept>();
    private int runtimeConverterSize = Runtime.getRuntime().availableProcessors() * 2;

    @Override
    public void execute() throws MojoExecutionException {
        executeMojo(conceptsFileNames, generatedResources, berkeleyDir);

    }

    void executeMojo(String[] conceptsFileNames, String generatedResources,
            File berkeleyDir) throws MojoExecutionException {
        try {
            runtimeConverterSize = 1;
            for (int i = 0; i < runtimeConverterSize; i++) {
                converters.put(new ConvertConcept());
            }

            long startTime = System.currentTimeMillis();
            FileIO.recursiveDelete(berkeleyDir);
            Bdb.setup(berkeleyDir.getAbsolutePath());
            if (initialPaths != null) {
               getLog().info("initialPaths: " + Arrays.asList(initialPaths));
            } else {
               getLog().warn("initialPaths: are NULL");
            }
 
            for (String fname : conceptsFileNames) {
                File conceptsFile = new File(generatedResources, fname);
                getLog().info("Starting load from: " + conceptsFile.getAbsolutePath());

                FileInputStream fis = new FileInputStream(conceptsFile);
                BufferedInputStream bis = new BufferedInputStream(fis);
                DataInputStream in = new DataInputStream(bis);

                try {
                    System.out.print(conceptsRead + "-");
                    while (true) {
                        EConcept eConcept = new EConcept(in);
                        int read = conceptsRead.incrementAndGet();
                        if (read % 100 == 0) {
                            if (read % 8000 == 0) {
                                System.out.println('.');
                                System.out.print(read + "-");
                            } else {
                                System.out.print('.');
                            }
                        }

                        I_ProcessEConcept conceptConverter = converters.take();
                        conceptConverter.setEConcept(eConcept);
                        executors.execute(conceptConverter);
                    }
                } catch (EOFException e) {
                    in.close();
                }
                System.out.println('.');
                getLog().info("Processed concept count: " + conceptsRead);
            }

            // See if any exceptions in the last converters;
            while (converters.isEmpty() == false) {
                I_ProcessEConcept conceptConverter = converters.take();
                conceptConverter.setEConcept(null);
            }

            while (conceptsProcessed.get() < conceptsRead.get()) {
                Thread.sleep(1000);
            }

            if (rstaFileNames != null) {
                for (String rstaName : rstaFileNames) {
                    getLog().info("Processing: " + rstaName);
                    File rstaFile = new File(generatedResources, rstaName);
                    FileInputStream fis = new FileInputStream(rstaFile);
                    BufferedInputStream bis = new BufferedInputStream(fis);
                    DataInputStream in = new DataInputStream(bis);
                    try {
                        while (true) {
                            EConcept eConcept = new EConcept(in);
                            getLog().info("Adding: "
                                    + eConcept.getRefsetMembers().size()
                                    + " annotations");
                            Bdb.addAsAnnotations(eConcept.getRefsetMembers());


                        }
                    } catch (EOFException e) {
                        in.close();
                    }
                }
            }

            getLog().info("finished load, adding paths.");
            if (initialPaths != null) {

                for (PathSpec spec : initialPaths) {

                    ConceptChronicleBI path =
                            Ts.get().getConcept(spec.getPathConcept().getUuids());
                    validateSpec(path, spec.getPathConcept());

                    ConceptChronicleBI origin =
                            Ts.get().getConcept(spec.getOriginConcept().getUuids());
                    validateSpec(origin, spec.getOriginConcept());

                    getLog().info("Adding path: " + spec.getPathConcept().getDescription() +
                            " with origin: " + spec.getOriginConcept().getDescription());

                    RefexCUB newPathSpec =
                            new RefexCUB(TkRefsetType.CID,
                            ReferenceConcepts.PATH.getNid(),
                            ReferenceConcepts.REFSET_PATHS.getNid());
                    newPathSpec.with(RefexProperty.CNID1, path.getNid());
                    newPathSpec.with(RefexProperty.STATUS_NID, ReferenceConcepts.CURRENT.getNid());
                    newPathSpec.setMemberContentUuid();

                    RefexCUB newOriginSpec =
                            new RefexCUB(TkRefsetType.CID_INT,
                            path.getNid(),
                            ReferenceConcepts.REFSET_PATH_ORIGINS.getNid());
                    newOriginSpec.with(RefexProperty.CNID1, origin.getNid());
                    newOriginSpec.with(RefexProperty.INTEGER1, Integer.MAX_VALUE);
                    newOriginSpec.with(RefexProperty.STATUS_NID, ReferenceConcepts.CURRENT.getNid());
                    newOriginSpec.setMemberContentUuid();

                    int authorNid = Ts.get().getNidForUuids(ArchitectonicAuxiliary.Concept.USER.getUids());
                    int pathNid = Ts.get().getNidForUuids(ArchitectonicAuxiliary.Concept.ARCHITECTONIC_BRANCH.getUids());
                    EditCoordinate ec = new EditCoordinate(authorNid, pathNid);

                    RefsetMemberFactory.createNoTx(newPathSpec, ec, startTime);
                    RefsetMemberFactory.createNoTx(newOriginSpec, ec, startTime);
                    getLog().info("Added path: " + spec.getPathConcept().getDescription() +
                            " with origin: " + spec.getOriginConcept().getDescription());



                }
            }

            getLog().info("added paths, start sync");
            getLog().info("Concept count: " + Bdb.getConceptDb().getCount());
            getLog().info(
                    "Concept attributes encountered: " + ConceptAttributesBinder.encountered
                    + " written: " + ConceptAttributesBinder.written);
            getLog().info(
                    "Descriptions encountered: " + DescriptionBinder.encountered + " written: "
                    + DescriptionBinder.written);
            getLog().info(
                    "Relationships encountered: " + RelationshipBinder.encountered + " written: "
                    + RelationshipBinder.written);
            getLog().info(
                    "Refexes encountered: " + RefsetMemberBinder.encountered + " written: "
                    + RefsetMemberBinder.written);

            getLog().info("Loading spelling variants.");
            loadVariants();
            getLog().info("Starting db sync.");
            Bdb.sync();
            getLog().info("Finished db sync, starting generate lucene index.");
            createLuceneDescriptionIndex();
            BdbCommitManager.commit();
            getLog().info("Finished create index, starting close.");
            Bdb.close();
            getLog().info("db closed");
            getLog().info("elapsed time: " + (System.currentTimeMillis() - startTime));

            if (moveToReadOnly) {
                FileIO.recursiveDelete(new File(berkeleyDir, "read-only"));
                File dirToMove = new File(berkeleyDir, "mutable");
                dirToMove.renameTo(new File(berkeleyDir, "read-only"));
            }
        } catch (Exception ex) {
            throw new MojoExecutionException(ex.getLocalizedMessage(), ex);
        } catch (Throwable ex) {
            throw new MojoExecutionException(ex.getLocalizedMessage(), ex);
        }

    }

    private void validateSpec(ConceptChronicleBI toValidate, ConceptSpec spec) throws IOException {
        boolean validated = false;
        for (DescriptionChronicleBI desc : toValidate.getDescs()) {
            for (DescriptionVersionBI descV : desc.getVersions()) {
                if (descV.getText().equals(
                        spec.getDescription())) {
                    validated = true;
                    break;
                }
            }
            if (validated) {
                break;
            }
        }
        if (!validated) {
            throw new IOException("Unable to validate spec: " + spec);
        }
    }

    public void loadVariants() throws Exception {
        List<File> dialectFiles = FileIO.recursiveGetFiles(
                new File(generatedResources, "spelling-variants"),
                "variants_", ".txt", false);
        if (dialectFiles != null && dialectFiles.size() > 0) {
            I_ConfigAceFrame config = DefaultConfig.newProfile();

            int authorNid = Ts.get().getNidForUuids(ArchitectonicAuxiliary.Concept.USER.getUids());
            int pathNid = Ts.get().getNidForUuids(ArchitectonicAuxiliary.Concept.ARCHITECTONIC_BRANCH.getUids());
            EditCoordinate ec = new EditCoordinate(authorNid, pathNid);
            TerminologyConstructorBI amender = Ts.get().getTerminologyConstructor(ec, config.getViewCoordinate());

            for (File df : dialectFiles) {
                getLog().info("processing dialectFile: " + df.getName());
                InputStreamReader isr =
                        new InputStreamReader(new FileInputStream(df),
                        "UTF-8");
                Concept enTextWithVariantsRefexColl = null;
                Concept dialectVariantsRefexColl = null;
                if (df.getName().toLowerCase().contains("us.txt")) {
                    enTextWithVariantsRefexColl = Bdb.getConcept(
                            Bdb.uuidsToNid(RefsetAuxiliary.Concept.EN_TEXT_WITH_DIALECT_VARIANTS.getUids()));
                    dialectVariantsRefexColl = Bdb.getConcept(
                            Bdb.uuidsToNid(RefsetAuxiliary.Concept.EN_US_TEXT_VARIANTS.getUids()));
                } else if (df.getName().toLowerCase().contains("uk.txt")) {
                    enTextWithVariantsRefexColl = Bdb.getConcept(
                            Bdb.uuidsToNid(RefsetAuxiliary.Concept.EN_TEXT_WITH_DIALECT_VARIANTS.getUids()));
                    dialectVariantsRefexColl = Bdb.getConcept(
                            Bdb.uuidsToNid(RefsetAuxiliary.Concept.EN_UK_TEXT_VARIANTS.getUids()));
                } else if (df.getName().toLowerCase().contains("ca.txt")) {
                    enTextWithVariantsRefexColl = Bdb.getConcept(
                            Bdb.uuidsToNid(RefsetAuxiliary.Concept.EN_TEXT_WITH_DIALECT_VARIANTS.getUids()));
                    dialectVariantsRefexColl = Bdb.getConcept(
                            Bdb.uuidsToNid(RefsetAuxiliary.Concept.EN_CA_TEXT_VARIANTS.getUids()));
                } else if (df.getName().toLowerCase().contains("au.txt")) {
                    enTextWithVariantsRefexColl = Bdb.getConcept(
                            Bdb.uuidsToNid(RefsetAuxiliary.Concept.EN_TEXT_WITH_DIALECT_VARIANTS.getUids()));
                    dialectVariantsRefexColl = Bdb.getConcept(
                            Bdb.uuidsToNid(RefsetAuxiliary.Concept.EN_AU_TEXT_VARIANTS.getUids()));
                }
                if (enTextWithVariantsRefexColl == null) {
                    break;
                }
                BufferedReader br = new BufferedReader(isr);
                try {
                    String line = br.readLine();
                    String[] parts = line.split("\\|");
                    int wordIndex = 0;
                    int variantIndex = 1;
                    if (parts[1].equalsIgnoreCase("text")) {
                        wordIndex = 1;
                        variantIndex = 0;
                    }
                    line = br.readLine();
                    while (line != null && line.length() > 3) {
                        parts = line.split("\\|");
                        String word = parts[wordIndex];
                        String variant = parts[variantIndex];

                        RefexCUB textRefexSpec = new RefexCUB(TkRefsetType.STR,
                                enTextWithVariantsRefexColl.getNid(), enTextWithVariantsRefexColl.getNid());
                        textRefexSpec.with(RefexProperty.STRING1, word);
                        textRefexSpec.with(RefexProperty.STATUS_NID, ReferenceConcepts.CURRENT.getNid());
                        textRefexSpec.setMemberContentUuid();

                        RefexChronicleBI<?> textRefex = amender.constructIfNotCurrent(textRefexSpec);

                        RefexCUB variantRefexSpec = new RefexCUB(TkRefsetType.STR,
                                textRefex.getNid(), dialectVariantsRefexColl.getNid());

                        variantRefexSpec.with(RefexProperty.STRING1, variant);
                        variantRefexSpec.with(RefexProperty.STATUS_NID, ReferenceConcepts.CURRENT.getNid());
                        variantRefexSpec.setMemberContentUuid();
                        amender.constructIfNotCurrent(variantRefexSpec);

                        line = br.readLine();
                    }
                } catch (EOFException ex) {
                    // nothing to do...
                } finally {
                    br.close();
                }
                BdbCommitManager.addUncommitted(enTextWithVariantsRefexColl);
                BdbCommitManager.addUncommitted(dialectVariantsRefexColl);
            }
        } else {
           getLog().warn("No dialect files found in " + 
                   new File(generatedResources, "spelling-variants").getAbsolutePath());
        }
    }

    private interface I_ProcessEConcept extends Runnable {

        public void setEConcept(EConcept eConcept) throws Throwable;
    }

    private class ConvertConcept implements I_ProcessEConcept {

        Throwable exception = null;
        EConcept eConcept = null;
        Concept newConcept = null;
        NidCNidMapBdb nidCnidMap;

        @Override
        public void run() {
            if (nidCnidMap == null) {
                nidCnidMap = Bdb.getNidCNidMap();
            }
            try {
                newConcept = Concept.get(eConcept);
                Bdb.getConceptDb().writeConcept(newConcept);
                Collection<Integer> nids = newConcept.getAllNids();
                assert nidCnidMap.getCNid(newConcept.getNid()) == newConcept.getNid();
                for (int nid : nids) {
                    assert nidCnidMap.getCNid(nid) == newConcept.getNid();
                }
                conceptsProcessed.incrementAndGet();
            } catch (Throwable e) {
                exception = e;
            }
            try {
                converters.put(this);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        /*
         * (non-Javadoc)
         * 
         * @see
         * org.ihtsdo.db.bdb.I_ProcessEConcept#setEConcept(org.ihtsdo.etypes
         * .EConcept)
         */
        @Override
        public void setEConcept(EConcept eConcept) throws Throwable {
            if (exception != null) {
                throw exception;
            }
            this.eConcept = eConcept;
        }
    }

    public void createLuceneDescriptionIndex() throws Exception {
        LuceneManager.setDbRootDir(berkeleyDir);
        LuceneManager.createLuceneDescriptionIndex();
    }
}
