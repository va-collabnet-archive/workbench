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

//~--- non-JDK imports --------------------------------------------------------

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.log.AceLog;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.id.Type5UuidFactory;
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
import org.ihtsdo.helper.bdb.NullComponentFinder;
import org.ihtsdo.helper.bdb.UuidDupFinder;
import org.ihtsdo.helper.bdb.UuidDupReporter;
import org.ihtsdo.lang.LANG_CODE;
import org.ihtsdo.lucene.LuceneManager;
import org.ihtsdo.lucene.LuceneManager.LuceneSearchType;
import org.ihtsdo.thread.NamedThreadFactory;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.ContradictionException;
import org.ihtsdo.tk.api.TerminologyBuilderBI;
import org.ihtsdo.tk.api.TerminologyStoreDI;
import org.ihtsdo.tk.api.blueprint.ConceptCB;
import org.ihtsdo.tk.api.blueprint.DescriptionCAB;
import org.ihtsdo.tk.api.blueprint.InvalidCAB;
import org.ihtsdo.tk.api.blueprint.RefexCAB;
import org.ihtsdo.tk.api.blueprint.RefexCAB.RefexProperty;
import org.ihtsdo.tk.api.concept.ConceptChronicleBI;
import org.ihtsdo.tk.api.coordinate.EditCoordinate;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.api.description.DescriptionChronicleBI;
import org.ihtsdo.tk.api.description.DescriptionVersionBI;
import org.ihtsdo.tk.api.refex.RefexChronicleBI;
import org.ihtsdo.tk.binding.snomed.CaseSensitive;
import org.ihtsdo.tk.binding.snomed.Snomed;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRfx;
import org.ihtsdo.tk.dto.concept.component.TkRevision;
import org.ihtsdo.tk.dto.concept.component.description.TkDescription;
import org.ihtsdo.tk.dto.concept.component.refex.TK_REFEX_TYPE;
import org.ihtsdo.tk.dto.concept.component.relationship.TkRelationship;
import org.ihtsdo.tk.spec.ConceptSpec;
import org.ihtsdo.tk.spec.PathSpec;

//~--- JDK imports ------------------------------------------------------------

import java.io.*;

import java.security.NoSuchAlgorithmException;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import org.ihtsdo.tk.dto.concept.component.refex.TkRefexAbstractMember;

/**
 * Goal which loads an EConcept.jbin file into a bdb.
 *
 * @goal load-econcepts-multi
 *
 * @phase process-sources
 */
public class LoadBdbMulti extends AbstractMojo {
   AtomicInteger       conceptsRead              = new AtomicInteger();
   AtomicInteger       conceptsProcessed         = new AtomicInteger();
   private ThreadGroup loadBdbMultiDbThreadGroup =
      new ThreadGroup("LoadBdbMulti threads");
   ExecutorService executors = Executors.newCachedThreadPool(
                                   new NamedThreadFactory(
                                      loadBdbMultiDbThreadGroup, "converter "));
   LinkedBlockingQueue<I_ProcessEConcept> converters =
      new LinkedBlockingQueue<>();
   private int runtimeConverterSize =
      Runtime.getRuntime().availableProcessors() * 2;
   private int                               converterSize      = 1;
   ConcurrentSkipListSet<Object>             watchSet           =
      new ConcurrentSkipListSet<>();
   ConcurrentSkipListSet<ConceptChronicleBI> indexedAnnotations =
      new ConcurrentSkipListSet<>();

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
    * concepts to generate file names.
    *
    * @parameter
    */
   private String[] newConceptsFileNames;

   /**
    * Generated resources directory.
    *
    * @parameter expression="${project.build.directory}/generated-resources"
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

   /**
    * Annotation index concepts
    *
    * @parameter
    */
   private List<ConceptDescriptor> annotationIndexes;

   /**
    * Watch concepts that will be printed to log when encountered.
    *
    * @parameter
    */
   private String[] watchConceptUuids;

   /**
    * True if mojo should check for duplicate UUIDs in the database
    *
    * @parameter default-value=true
    */
   private boolean findDups;

   /**
    * True if mojo should check for null components in the database
    *
    * @parameter default-value=true
    */
   private boolean findNullComponents;

   public void createLuceneIndices() throws Exception {
      LuceneManager.setLuceneRootDir(berkeleyDir, LuceneSearchType.DESCRIPTION);
      LuceneManager.createLuceneIndex(LuceneSearchType.DESCRIPTION);
   }

   @Override
   public void execute() throws MojoExecutionException {
      executeMojo(conceptsFileNames, generatedResources, berkeleyDir);
   }

   void executeMojo(String[] conceptsFileNames, String generatedResources,
                    File berkeleyDir)
           throws MojoExecutionException {
      if (watchConceptUuids != null) {
         for (String uuidStr : watchConceptUuids) {
            watchSet.add(UUID.fromString(uuidStr));
         }
      }

      try {
         for (int i = 0; i < converterSize; i++) {
            converters.put(new ConvertConcept());
         }

         long startTime = System.currentTimeMillis();

         FileIO.recursiveDelete(new File(berkeleyDir, "mutable"));
         FileIO.recursiveDelete(new File(berkeleyDir, "read-only"));
         Bdb.selectJeProperties(berkeleyDir, berkeleyDir);
         Bdb.setup(berkeleyDir.getAbsolutePath());

         if (initialPaths != null) {
            getLog().info("initialPaths: " + Arrays.asList(initialPaths));
         } else {
            getLog().warn("initialPaths: are NULL");
         }

         // 1st pass to load the uuid sets
         for (String fname : conceptsFileNames) {
            File conceptsFile = new File(generatedResources, fname);

            getLog().info("Starting identifier load from: "
                          + conceptsFile.getAbsolutePath());

            FileInputStream     fis = new FileInputStream(conceptsFile);
            BufferedInputStream bis = new BufferedInputStream(fis);
            DataInputStream     in  = new DataInputStream(bis);

            try {
               System.out.print(conceptsRead + "-");

               while (true) {
                  EConcept eConcept = new EConcept(in);
                  int      read     = conceptsRead.incrementAndGet();

                  if (read % 100 == 0) {
                     if (read % 8000 == 0) {
                        System.out.println('.');
                        System.out.print(read + "-");
                     } else {
                        System.out.print('.');
                     }
                  }

                  if (eConcept.getConceptAttributes() != null) {
                     Ts.get().getNidForUuids(
                         eConcept.getConceptAttributes().getUuids());

                     if (eConcept.getDescriptions() != null) {
                        for (TkDescription desc : eConcept.getDescriptions()) {
                           Ts.get().getNidForUuids(desc.getUuids());
                        }
                     }

                     if (eConcept.getRelationships() != null) {
                        for (TkRelationship rel : eConcept.getRelationships()) {
                           Ts.get().getNidForUuids(rel.getUuids());
                        }
                     }
                  }
               }
            } catch (EOFException e) {
               in.close();
            }
         }

         conceptsRead.set(0);

         // 2nd pass to load the eConcepts...
         for (String fname : conceptsFileNames) {
            File conceptsFile = new File(generatedResources, fname);

            getLog().info("Starting eConcept load from: "
                          + conceptsFile.getAbsolutePath());

            FileInputStream     fis = new FileInputStream(conceptsFile);
            BufferedInputStream bis = new BufferedInputStream(fis);
            DataInputStream     in  = new DataInputStream(bis);

            try {
               System.out.print(conceptsRead + "-");

               while (true) {
                  EConcept eConcept = new EConcept(in);
                  int      read     = conceptsRead.incrementAndGet();

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

            for (ConceptChronicleBI concept : indexedAnnotations) {
               Ts.get().addUncommittedNoChecks(concept);
            }

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

          Concept.resolveUnresolvedAnnotations(null);
          if (Concept.getUnresolvedAnnotations() != null) {
              for (TkRefexAbstractMember member : Concept.getUnresolvedAnnotations()) {
                  Ts.get().addUncommittedNoChecks(Ts.get().getConceptForNid(Ts.get().getNidForUuids(member.getComponentUuid())));
              }
              Ts.get().commit();
          }
         
         UuidDupFinder dupFinder = null;

         if (findDups) {
            getLog().info("Testing for dup UUIDs.");
            Concept.disableComponentsCRHM();
            dupFinder = new UuidDupFinder();
            Bdb.getConceptDb().iterateConceptDataInParallel(dupFinder);
            System.out.println();

            if (dupFinder.getDupUuids().isEmpty()) {
               getLog().info("No dup UUIDs found.");
            } else {
               dupFinder.writeDupFile();
               getLog().warn("\n\nDuplicate UUIDs found: "
                             + dupFinder.getDupUuids().size() + "\n"
                             + dupFinder.getDupUuids() + "\n");

               UuidDupReporter reporter =
                  new UuidDupReporter(dupFinder.getDupUuids());

               Bdb.getConceptDb().iterateConceptDataInParallel(reporter);
               reporter.reportDupClasses();
            }

            Concept.enableComponentsCRHM();
         }

         if (annotationIndexes != null) {
            for (ConceptDescriptor cd : annotationIndexes) {
               Concept c =
                  (Concept) Ts.get().getConcept(UUID.fromString(cd.getUuid()));

               c.setAnnotationIndex(true);
               Ts.get().addUncommitted(c);
               Ts.get().commit();
               getLog().info("Setting concept to annotation index: "
                             + cd.getDescription());
            }
         }

         if (rstaFileNames != null) {
            for (String rstaName : rstaFileNames) {
               getLog().info("Processing: " + rstaName);

               File                rstaFile = new File(generatedResources,
                                                 rstaName);
               FileInputStream     fis      = new FileInputStream(rstaFile);
               BufferedInputStream bis      = new BufferedInputStream(fis);
               DataInputStream     in       = new DataInputStream(bis);

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
               boolean hasConcept = spec.testPathConcept();

               if (hasConcept) {
                  int nid =
                     Ts.get().getNidForUuids(spec.getPathConcept().getUuids());
                  int cNid = Bdb.getConceptNid(nid);

                  if (cNid == Integer.MAX_VALUE) {
                     hasConcept = false;
                  }
               }

               if (!hasConcept) {
                  ConceptCB            conceptBp =
                     spec.makePathConceptBluePrint();
                  ViewCoordinate       vc        =
                     Ts.get().getMetadataViewCoordinate();
                  EditCoordinate       ec        =
                     Ts.get().getMetadataEditCoordinate();
                  TerminologyBuilderBI builder   =
                     Ts.get().getTerminologyBuilder(ec, vc);
                  ConceptChronicleBI concept = builder.construct(conceptBp);

                  BdbCommitManager.addUncommitted(concept);
               }

               ConceptChronicleBI path =
                  Ts.get().getConcept(spec.getPathConcept().getUuids());

               validateSpec(path, spec.getPathConcept());

               ConceptChronicleBI origin =
                  Ts.get().getConcept(spec.getOriginConcept().getUuids());

               validateSpec(origin, spec.getOriginConcept());
               getLog().info("Adding path: "
                             + spec.getPathConcept().getDescription()
                             + " with origin: "
                             + spec.getOriginConcept().getDescription());

               RefexCAB newPathSpec =
                  new RefexCAB(TK_REFEX_TYPE.CID,
                               ReferenceConcepts.PATH.getNid(),
                               ReferenceConcepts.REFSET_PATHS.getNid());

               newPathSpec.with(RefexProperty.CNID1, path.getNid());
               newPathSpec.with(RefexProperty.STATUS_NID,
                                SnomedMetadataRfx.getSTATUS_CURRENT_NID());
               newPathSpec.setMemberContentUuid();

               RefexCAB newOriginSpec =
                  new RefexCAB(TK_REFEX_TYPE.CID_INT, path.getNid(),
                               ReferenceConcepts.REFSET_PATH_ORIGINS.getNid());

               newOriginSpec.with(RefexProperty.CNID1, origin.getNid());
               newOriginSpec.with(RefexProperty.INTEGER1, Integer.MAX_VALUE);
               newOriginSpec.with(RefexProperty.STATUS_NID,
                                  SnomedMetadataRfx.getSTATUS_CURRENT_NID());
               newOriginSpec.setMemberContentUuid();

               int authorNid =
                  Ts.get().getNidForUuids(
                      ArchitectonicAuxiliary.Concept.USER.getUids());
               int pathNid =
                  Ts.get()
                     .getNidForUuids(ArchitectonicAuxiliary.Concept
                        .ARCHITECTONIC_BRANCH.getUids());
               EditCoordinate ec =
                  new EditCoordinate(
                      authorNid,
                      Ts.get().getNidForUuids(
                         TkRevision.unspecifiedModuleUuid), pathNid);

               RefsetMemberFactory.createNoTx(newPathSpec, ec, startTime);
               RefsetMemberFactory.createNoTx(newOriginSpec, ec, startTime);
               getLog().info("Added path: "
                             + spec.getPathConcept().getDescription()
                             + " with origin: "
                             + spec.getOriginConcept().getDescription());
            }
         }

         getLog().info("added paths, start sync");
         getLog().info("Concept count: " + Bdb.getConceptDb().getCount());
         getLog().info("Concept attributes encountered: "
                       + ConceptAttributesBinder.encountered + " written: "
                       + ConceptAttributesBinder.written);
         getLog().info("Descriptions encountered: "
                       + DescriptionBinder.encountered + " written: "
                       + DescriptionBinder.written);
         getLog().info("Relationships encountered: "
                       + RelationshipBinder.encountered + " written: "
                       + RelationshipBinder.written);
         getLog().info("Refexes encountered: " + RefsetMemberBinder.encountered
                       + " written: " + RefsetMemberBinder.written);
         getLog().info("Loading spelling variants.");
         loadVariants();
         getLog().info("Loading case sensitive words.");
         loadCaseSensitiveWords();
         getLog().info("Generating new concepts.");
         generateNewConcepts();
         getLog().info("Starting db sync.");
         Bdb.sync();
         getLog().info("Finished db sync, starting generate lucene index.");
         createLuceneIndices();
         BdbCommitManager.commit();
         getLog().info("Finished create index.");

         if (findDups) {
            Concept.disableComponentsCRHM();
            getLog().info("Testing for UUID dups.");
            dupFinder = new UuidDupFinder();
            Bdb.getConceptDb().iterateConceptDataInParallel(dupFinder);
            System.out.println();

            if (dupFinder.getDupUuids().isEmpty()) {
               getLog().info("No dup UUIDs found.");
            } else {
               dupFinder.writeDupFile();
               getLog().warn("\n\nDuplicate UUIDs found: "
                             + dupFinder.getDupUuids().size() + "\n"
                             + dupFinder.getDupUuids() + "\n");

               UuidDupReporter reporter =
                  new UuidDupReporter(dupFinder.getDupUuids());

               Bdb.getConceptDb().iterateConceptDataInParallel(reporter);
               reporter.reportDupClasses();
            }
         }

         if (findNullComponents) {
            getLog().info("Testing for Null Components Started.");
            Concept.disableComponentsCRHM();

            NullComponentFinder nullComponentFinder = new NullComponentFinder();

            Bdb.getConceptDb().iterateConceptDataInParallel(
                nullComponentFinder);
            System.out.println();

            if (nullComponentFinder.getNidsWithNullComponents().isEmpty()) {
               getLog().info("No Null component found.");
            } else {
               getLog().warn(
                   "\n\n Null Components found: "
                   + nullComponentFinder.getNidsWithNullComponents().size()
                   + "\n" + nullComponentFinder.getNidsWithNullComponents()
                   + "\n");
            }

            Concept.enableComponentsCRHM();
            getLog().info("Testing for Null Components Finished.");
         }
         
         Concept.enableComponentsCRHM();
         getLog().info("Starting close.");
         Bdb.close();
         getLog().info("db closed");
         getLog().info("elapsed time: "
                       + (System.currentTimeMillis() - startTime));

         if ((dupFinder != null) &&!dupFinder.getDupUuids().isEmpty()) {
            throw new Exception("Duplicate UUIDs found: "
                                + dupFinder.getDupUuids().size());
         }

         if (moveToReadOnly) {
            File mutableFile = new File(berkeleyDir, "mutable");

            FileIO.recursiveDelete(new File(berkeleyDir, "read-only"));

            File jePropFile = new File(mutableFile, "je.properties");

            jePropFile.delete();

            File dirToMove = mutableFile;

            dirToMove.renameTo(new File(berkeleyDir, "read-only"));
         }
      } catch (Exception ex) {
         throw new MojoExecutionException(ex.getLocalizedMessage(), ex);
      } catch (Throwable ex) {
         throw new MojoExecutionException(ex.getLocalizedMessage(), ex);
      }
   }

   // fsn, prefTem, parent(uuid), path, make annotation, author(uuid)(blank = user) //@afk todo: make parent to be conept spec
   private void generateNewConcepts()
           throws IOException, NoSuchAlgorithmException, TerminologyException,
                  InvalidCAB, ContradictionException {
      if (newConceptsFileNames != null) {
         for (String conceptFile : newConceptsFileNames) {
            int authorNid = Ts.get().getNidForUuids(
                                ArchitectonicAuxiliary.Concept.USER.getUids());
            TerminologyStoreDI store          = Ts.get();
            BufferedReader     conceptsReader =
               new BufferedReader(new FileReader(conceptFile));
            String line = conceptsReader.readLine();

            line = conceptsReader.readLine();

            while (line != null) {
               String[] parts          = line.split("\t");
               String   fsn            = parts[0];
               String   pref           = parts[1];
               String   parent         = parts[2];
               String   path           = parts[3];
               String   makeAnnotation = parts[4];
               String   author         = null;
               UUID     conceptUuid    = null;

               if (parts.length == 6) {
                  author    = parts[5];
                  authorNid = store.getNidForUuids(UUID.fromString(author));
               }

               if (parts.length == 7) {
                  String uuidString = parts[6];

                  conceptUuid = UUID.fromString(uuidString);
               }

               UUID parentUuid = UUID.fromString(parent);
               UUID pathUuid   =
                  Type5UuidFactory.get(Type5UuidFactory.PATH_ID_FROM_FS_DESC,
                                       path);
               EditCoordinate ec = new EditCoordinate(authorNid,
                                      Snomed.CORE_MODULE.getLenient().getNid(),
                                      store.getNidForUuids(pathUuid));
               TerminologyBuilderBI builder =
                  store.getTerminologyBuilder(
                      ec, Ts.get().getMetadataViewCoordinate());
               ConceptCB conceptBp = new ConceptCB(fsn, pref, LANG_CODE.EN,
                                        Snomed.IS_A.getLenient().getPrimUuid(),
                                        parentUuid);

               if (conceptUuid != null) {
                  conceptBp.setComponentUuid(conceptUuid);
               }

               List<DescriptionCAB> fsnCABs =
                  conceptBp.getFullySpecifiedNameCABs();
               List<DescriptionCAB> prefCABs = conceptBp.getPreferredNameCABs();

               for (DescriptionCAB f : fsnCABs) {
                  conceptBp.addFullySpecifiedName(f, LANG_CODE.EN);
               }

               for (DescriptionCAB p : prefCABs) {
                  conceptBp.addFullySpecifiedName(p, LANG_CODE.EN);
               }

               ConceptChronicleBI concept =
                  builder.constructIfNotCurrent(conceptBp);

               if (makeAnnotation.equalsIgnoreCase("true")) {
                  concept.setAnnotationStyleRefex(true);
               }

               BdbCommitManager.addUncommitted(concept);
               System.out.println("**CREATED NEW CONCEPT: " + concept + " "
                                  + concept.getPrimUuid());
               line = conceptsReader.readLine();
            }
         }
      }
   }

   public void loadCaseSensitiveWords() throws Exception {
      List<File> caseFiles =
         FileIO.recursiveGetFiles(new File(generatedResources,
            "case-sensitive"), "cs_words", ".txt", false);

      if ((caseFiles != null) && (caseFiles.size() > 0)) {
         I_ConfigAceFrame config    = DefaultConfig.newProfile();
         int              authorNid =
            Ts.get().getNidForUuids(
                ArchitectonicAuxiliary.Concept.USER.getUids());
         int pathNid =
            Ts.get().getNidForUuids(
                ArchitectonicAuxiliary.Concept.ARCHITECTONIC_BRANCH.getUids());
         EditCoordinate ec = new EditCoordinate(
                                 authorNid,
                                 Ts.get().getNidForUuids(
                                    TkRevision.unspecifiedModuleUuid), pathNid);
         TerminologyBuilderBI amender = Ts.get().getTerminologyBuilder(ec,
                                           config.getViewCoordinate());

         for (File cf : caseFiles) {
            getLog().info("processing dialectFile: " + cf.getName());

            InputStreamReader isr =
               new InputStreamReader(new FileInputStream(cf), "UTF-8");
            Concept caseSensitiveRefexColl = null;

            if (cf.getName().toLowerCase().contains("cs_words.txt")) {
               caseSensitiveRefexColl = Bdb.getConcept(
                  Bdb.uuidsToNid(
                     RefsetAuxiliary.Concept.CASE_SENSITIVE_WORDS.getUids()));
            }

            if (caseSensitiveRefexColl == null) {
               break;
            }

            BufferedReader br = new BufferedReader(isr);

            try {
               String              line      = br.readLine();
               String[]            parts     = line.split(" ");
               int                 wordIndex = 0;
               int                 caseIndex = 1;
               RefexChronicleBI<?> wordRefex = null;

               line = br.readLine();

               while ((line != null) && (line.length() > 1)) {
                  parts = line.split(" ");

                  String word     = parts[wordIndex];
                  int    caseType = Integer.parseInt(parts[caseIndex]);
                  int    icsTypeNid;

                  if (caseType == 1) {
                     icsTypeNid =
                        CaseSensitive.IC_SIGNIFICANT.getLenient().getNid();
                  } else {
                     icsTypeNid =
                        CaseSensitive.MAYBE_IC_SIGNIFICANT.getLenient()
                           .getNid();
                  }

                  RefexCAB wordRefexSpec = new RefexCAB(TK_REFEX_TYPE.CID_STR,
                                              caseSensitiveRefexColl.getNid(),
                                              caseSensitiveRefexColl.getNid());

                  wordRefexSpec.with(RefexProperty.STRING1, word);
                  wordRefexSpec.with(RefexProperty.CNID1, icsTypeNid);
                  wordRefexSpec.with(RefexProperty.STATUS_NID,
                                     SnomedMetadataRfx.getSTATUS_CURRENT_NID());
                  wordRefexSpec.setMemberContentUuid();
                  wordRefex = amender.constructIfNotCurrent(wordRefexSpec);
                  line      = br.readLine();
               }
            } catch (EOFException ex) {

               // nothing to do...
            } finally {
               br.close();
            }

            BdbCommitManager.addUncommitted(caseSensitiveRefexColl);
         }
      } else {
         getLog().warn("No dialect files found in "
                       + new File(generatedResources,
                                  "spelling-variants").getAbsolutePath());
      }
   }

   public void loadVariants() throws Exception {
      List<File> dialectFiles =
         FileIO.recursiveGetFiles(new File(generatedResources,
            "spelling-variants"), "variants_", ".txt", false);

      if ((dialectFiles != null) && (dialectFiles.size() > 0)) {
         I_ConfigAceFrame config    = DefaultConfig.newProfile();
         int              authorNid =
            Ts.get().getNidForUuids(
                ArchitectonicAuxiliary.Concept.USER.getUids());
         int pathNid =
            Ts.get().getNidForUuids(
                ArchitectonicAuxiliary.Concept.ARCHITECTONIC_BRANCH.getUids());
         EditCoordinate ec = new EditCoordinate(
                                 authorNid,
                                 Ts.get().getNidForUuids(
                                    TkRevision.unspecifiedModuleUuid), pathNid);
         TerminologyBuilderBI amender = Ts.get().getTerminologyBuilder(ec,
                                           config.getViewCoordinate());

         for (File df : dialectFiles) {
            getLog().info("processing dialectFile: " + df.getName());

            InputStreamReader isr =
               new InputStreamReader(new FileInputStream(df), "UTF-8");
            Concept enTextWithVariantsRefexColl = null;
            Concept dialectVariantsRefexColl    = null;

            if (df.getName().toLowerCase().contains("us.txt")) {
               enTextWithVariantsRefexColl =
                  Bdb.getConcept(Bdb
                     .uuidsToNid(RefsetAuxiliary.Concept
                        .EN_TEXT_WITH_DIALECT_VARIANTS.getUids()));
               dialectVariantsRefexColl = Bdb.getConcept(
                  Bdb.uuidsToNid(
                     RefsetAuxiliary.Concept.EN_US_TEXT_VARIANTS.getUids()));
            } else if (df.getName().toLowerCase().contains("uk.txt")) {
               enTextWithVariantsRefexColl =
                  Bdb.getConcept(Bdb
                     .uuidsToNid(RefsetAuxiliary.Concept
                        .EN_TEXT_WITH_DIALECT_VARIANTS.getUids()));
               dialectVariantsRefexColl = Bdb.getConcept(
                  Bdb.uuidsToNid(
                     RefsetAuxiliary.Concept.EN_UK_TEXT_VARIANTS.getUids()));
            } else if (df.getName().toLowerCase().contains("ca.txt")) {
               enTextWithVariantsRefexColl =
                  Bdb.getConcept(Bdb
                     .uuidsToNid(RefsetAuxiliary.Concept
                        .EN_TEXT_WITH_DIALECT_VARIANTS.getUids()));
               dialectVariantsRefexColl = Bdb.getConcept(
                  Bdb.uuidsToNid(
                     RefsetAuxiliary.Concept.EN_CA_TEXT_VARIANTS.getUids()));
            } else if (df.getName().toLowerCase().contains("au.txt")) {
               enTextWithVariantsRefexColl =
                  Bdb.getConcept(Bdb
                     .uuidsToNid(RefsetAuxiliary.Concept
                        .EN_TEXT_WITH_DIALECT_VARIANTS.getUids()));
               dialectVariantsRefexColl = Bdb.getConcept(
                  Bdb.uuidsToNid(
                     RefsetAuxiliary.Concept.EN_AU_TEXT_VARIANTS.getUids()));
            }

            if (enTextWithVariantsRefexColl == null) {
               break;
            }

            BufferedReader br = new BufferedReader(isr);

            try {
               String   line         = br.readLine();
               String[] parts        = line.split("\\|");
               int      wordIndex    = 0;
               int      variantIndex = 1;

               if (parts[1].equalsIgnoreCase("text")) {
                  wordIndex    = 1;
                  variantIndex = 0;
               }

               line = br.readLine();

               while ((line != null) && (line.length() > 3)) {
                  parts = line.split("\\|");

                  String   word          = parts[wordIndex];
                  String   variant       = parts[variantIndex];
                  RefexCAB textRefexSpec =
                     new RefexCAB(TK_REFEX_TYPE.STR,
                                  enTextWithVariantsRefexColl.getNid(),
                                  enTextWithVariantsRefexColl.getNid());

                  textRefexSpec.with(RefexProperty.STRING1, word);
                  textRefexSpec.with(RefexProperty.STATUS_NID,
                                     SnomedMetadataRfx.getSTATUS_CURRENT_NID());
                  textRefexSpec.setMemberContentUuid();

                  RefexChronicleBI<?> textRefex =
                     amender.constructIfNotCurrent(textRefexSpec);
                  RefexCAB variantRefexSpec =
                     new RefexCAB(TK_REFEX_TYPE.STR, textRefex.getNid(),
                                  dialectVariantsRefexColl.getNid());

                  variantRefexSpec.with(RefexProperty.STRING1, variant);
                  variantRefexSpec.with(
                      RefexProperty.STATUS_NID,
                      SnomedMetadataRfx.getSTATUS_CURRENT_NID());
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
         getLog().warn("No dialect files found in "
                       + new File(generatedResources,
                                  "spelling-variants").getAbsolutePath());
      }
   }

   private void validateSpec(ConceptChronicleBI toValidate, ConceptSpec spec)
           throws IOException {
      boolean validated = false;

      for (DescriptionChronicleBI desc : toValidate.getDescriptions()) {
         for (DescriptionVersionBI descV : desc.getVersions()) {
            if (descV.getText().equals(spec.getDescription())) {
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

   private interface I_ProcessEConcept extends Runnable {
      public void setEConcept(EConcept eConcept) throws Throwable;
   }


   private class ConvertConcept implements I_ProcessEConcept {
      Throwable     exception  = null;
      EConcept      eConcept   = null;
      Concept       newConcept = null;
      NidCNidMapBdb nidCnidMap;

      @Override
      public void run() {
         if (nidCnidMap == null) {
            nidCnidMap = Bdb.getNidCNidMap();
         }

         boolean watch = watchSet.contains(eConcept.getPrimordialUuid());

         if (watch) {
            AceLog.getAppLog().info("Watch found: " + eConcept);
            AceLog.getAppLog().info(
                "UUID set: " + eConcept.getConceptAttributes().getUuids());
         }

         try {
            newConcept = Concept.get(eConcept, indexedAnnotations);

            if (watch) {
               AceLog.getAppLog().info("New concept: "
                                       + newConcept.toLongString());
            }

            if (newConcept != null) {
               assert newConcept.readyToWrite();
               Bdb.getConceptDb().writeConcept(newConcept);

               Collection<Integer> nids = newConcept.getAllNids();

               assert nidCnidMap.getCNid(newConcept.getNid())
                      == newConcept.getNid();

               for (int nid : nids) {
                  assert nidCnidMap.getCNid(nid) == newConcept.getNid();
               }
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
}
