package org.ihtsdo.db.bdb;

//~--- non-JDK imports --------------------------------------------------------

import org.dwfa.ace.ACE;
import org.dwfa.ace.TermComponentDataCheckSelectionListener;
import org.dwfa.ace.api.I_ConceptAttributeVersioned;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_DescriptionVersioned;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IterateIds;
import org.dwfa.ace.api.I_RelVersioned;
import org.dwfa.ace.api.I_RepresentIdSet;
import org.dwfa.ace.api.IdentifierSet;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.api.cs.ChangeSetPolicy;
import org.dwfa.ace.api.cs.ChangeSetWriterThreading;
import org.dwfa.ace.api.ebr.I_ExtendByRef;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartStr;
import org.dwfa.ace.config.AceFrame;
import org.dwfa.ace.log.AceLog;
import org.dwfa.ace.task.commit.AlertToDataConstraintFailure;
import org.dwfa.ace.task.commit.AlertToDataConstraintFailure.ALERT_TYPE;
import org.dwfa.ace.task.commit.I_TestDataConstraints;
import org.dwfa.app.DwfaEnv;
import org.dwfa.bpa.util.OpenFrames;
import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.svn.Svn;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.vodb.types.IntSet;

import org.ihtsdo.arena.contradiction.ContradictionEditorFrame;
import org.ihtsdo.concept.Concept;
import org.ihtsdo.concept.component.ConceptComponent;
import org.ihtsdo.concept.component.attributes.ConceptAttributes;
import org.ihtsdo.concept.component.attributes.ConceptAttributesRevision;
import org.ihtsdo.concept.component.description.Description;
import org.ihtsdo.concept.component.description.DescriptionRevision;
import org.ihtsdo.concept.component.refset.RefsetMember;
import org.ihtsdo.concept.component.refset.RefsetRevision;
import org.ihtsdo.concept.component.relationship.Relationship;
import org.ihtsdo.concept.component.relationship.RelationshipRevision;
import org.ihtsdo.cs.ChangeSetWriterHandler;
import org.ihtsdo.db.bdb.computer.kindof.KindOfComputer;
import org.ihtsdo.db.bdb.id.NidCNidMapBdb;
import org.ihtsdo.db.change.BdbCommitSequence;
import org.ihtsdo.db.change.LastChange;
import org.ihtsdo.lucene.LuceneManager;
import org.ihtsdo.lucene.LuceneManager.LuceneSearchType;
import org.ihtsdo.lucene.WfHxLuceneManager;
import org.ihtsdo.thread.NamedThreadFactory;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.ComponentBI;
import org.ihtsdo.tk.api.NidBitSetItrBI;
import org.ihtsdo.tk.api.NidSetBI;
import org.ihtsdo.tk.api.concept.ConceptChronicleBI;
import org.ihtsdo.tk.api.coordinate.IsaCoordinate;
import org.ihtsdo.workflow.WorkflowHistoryJavaBean;
import org.ihtsdo.workflow.refset.history.WorkflowHistoryRefsetReader;
import org.ihtsdo.workflow.refset.history.WorkflowHistoryRefsetWriter;
import org.ihtsdo.workflow.refset.utilities.WorkflowHelper;

//~--- JDK imports ------------------------------------------------------------

import java.awt.Frame;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.ObjectInputStream;

import java.lang.reflect.InvocationTargetException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

public class BdbCommitManager {
   private static final int                   PERMIT_COUNT             = 50;
   public static String                       pluginRoot               = "plugins";
   private static int                         wfHistoryRefsetId        = 0;
   private static final AtomicInteger         writerCount              = new AtomicInteger(0);
   private static boolean                     writeChangeSets          = true;
   private static Set<I_ExtendByRef>          uncommittedWfMemberIds   = new HashSet<I_ExtendByRef>();
   private static I_RepresentIdSet            uncommittedDescNids      = new IdentifierSet();
   private static I_RepresentIdSet            uncommittedCNidsNoChecks = new IdentifierSet();
   private static I_RepresentIdSet            uncommittedCNids         = new IdentifierSet();
   private static boolean                     performCreationTests     = true;
   private static boolean                     performCommitTests       = true;
   private static boolean                     performCommit            = false;
   private static Semaphore                   luceneWriterPermit       = new Semaphore(PERMIT_COUNT);
   private static AtomicReference<Concept>    lastUncommitted          = new AtomicReference<Concept>();
   private static long                        lastDoUpdate             = Long.MIN_VALUE;
   private static long                        lastCommit               = Bdb.gVersion.incrementAndGet();
   private static long                        lastCancel               = Integer.MIN_VALUE;
   private static Semaphore                   dbWriterPermit           = new Semaphore(PERMIT_COUNT);
   private static List<I_TestDataConstraints> creationTests            =
      new ArrayList<I_TestDataConstraints>();
   private static List<I_TestDataConstraints> commitTests              =
      new ArrayList<I_TestDataConstraints>();
   private static ThreadGroup                 commitManagerThreadGroup =
      new ThreadGroup("commit manager threads");
   private static ExecutorService             changeSetWriterService;
   private static ExecutorService             dbWriterService;
   private static ExecutorService             luceneWriterService;
   //J-
     private static ConcurrentHashMap<I_GetConceptData, 
                                      Collection<AlertToDataConstraintFailure>> dataCheckMap =
                            new ConcurrentHashMap<I_GetConceptData, Collection<AlertToDataConstraintFailure>>();
   //J+

   //~--- static initializers -------------------------------------------------

   static {
      reset();
   }

   //~--- methods -------------------------------------------------------------

   public static void addUncommitted(ConceptChronicleBI igcd) {
      if (igcd == null) {
         return;
      }

      try {
         KindOfComputer.updateIsaCachesUsingStatedView(igcd);
      } catch (Exception ex) {
         AceLog.getAppLog().alertAndLogException(ex);
      }

      Concept concept = (Concept) igcd;

      LastChange.touch(concept);
      dataCheckMap.remove(concept);

      if (concept.isUncommitted() == false) {
         if (Bdb.watchList.containsKey(concept.getNid())) {
            AceLog.getAppLog().info("--- Removing uncommitted concept: " + concept.getNid() + " --- ");
         }

         removeUncommitted(concept);

         try {
            dbWriterPermit.acquire();
         } catch (InterruptedException ex) {
            throw new RuntimeException(ex);
         }

         dbWriterService.execute(new SetNidsForCid(concept));
         dbWriterService.execute(new ConceptWriter(concept));

         return;
      }

      concept.modified();

      if (Bdb.watchList.containsKey(concept.getNid())) {
         AceLog.getAppLog().info("---@@@ Adding uncommitted concept: " + concept.getNid() + " ---@@@ ");
      }

      try {
         if (performCreationTests) {
            Set<AlertToDataConstraintFailure> warningsAndErrors = new HashSet<AlertToDataConstraintFailure>();

            dataCheckMap.put(concept, warningsAndErrors);
            DataCheckRunner.runDataChecks(concept, creationTests);
         }

         uncommittedCNids.setMember(concept.getNid());
         dbWriterPermit.acquire();
         dbWriterService.execute(new SetNidsForCid(concept));
         dbWriterService.execute(new ConceptWriter(concept));
      } catch (InterruptedException e) {
         throw new RuntimeException(e);
      }

      SwingUtilities.invokeLater(new UpdateFrames(concept));
   }

   public static void addUncommitted(I_ExtendByRef extension) {
      RefsetMember<?, ?> member = (RefsetMember<?, ?>) extension;

      addUncommitted(member.getEnclosingConcept());

      if ((wfHistoryRefsetId != Integer.MAX_VALUE) && (wfHistoryRefsetId == 0)) {
         if (wfHistoryRefsetId == 0) {
            if (Ts.get().hasUuid(RefsetAuxiliary.Concept.WORKFLOW_HISTORY.getUids().iterator().next())) {
               try {
                  wfHistoryRefsetId =
                     Terms.get().uuidToNative(RefsetAuxiliary.Concept.WORKFLOW_HISTORY.getUids());
               } catch (Exception e) {
                  AceLog.getAppLog().log(Level.WARNING,
                                         "Unable to access Workflow History Refset UUID with error: "
                                         + e.getMessage());
               }
            } else {
                wfHistoryRefsetId = Integer.MAX_VALUE;
            }
         }
      }

      if (wfHistoryRefsetId == extension.getRefsetId()) {
         addUncommittedWfMemberId(extension);
      }
   }

   public static void addUncommittedDescNid(int dNid) {
      uncommittedDescNids.setMember(dNid);
   }

   public static void addUncommittedNoChecks(I_ExtendByRef extension) {
      RefsetMember<?, ?> member = (RefsetMember<?, ?>) extension;

      addUncommittedNoChecks(member.getEnclosingConcept());
   }

   public static void addUncommittedNoChecks(I_GetConceptData concept) {
      Concept c = (Concept) concept;

      c.modified();
      LastChange.touch(c);

      try {
         KindOfComputer.updateIsaCachesUsingStatedView(c);
      } catch (Exception ex) {
         AceLog.getAppLog().alertAndLogException(ex);
      }

      if (Bdb.watchList.containsKey(concept.getNid())) {
         AceLog.getAppLog().info("---@@@ Adding uncommitted NO checks: " + concept.getNid() + " ---@@@ ");
      }

      c = null;

      if (concept.isUncommitted()) {
         uncommittedCNidsNoChecks.setMember(concept.getNid());
         c = lastUncommitted.getAndSet((Concept) concept);

         if (c == concept) {
            c = null;
         }
      } else {
         c = (Concept) concept;

         if (Bdb.watchList.containsKey(concept.getNid())) {
            AceLog.getAppLog().info("--- Removing uncommitted concept: " + concept.getNid() + " --- ");
         }

         removeUncommitted(c);
      }

      try {
         writeUncommitted(c);
      } catch (InterruptedException e) {
         throw new RuntimeException(e);
      }
   }

   private static void addUncommittedWfMemberId(I_ExtendByRef extension) {
      uncommittedWfMemberIds.add(extension);
   }

   public static void cancel() {
      lastCancel = Bdb.gVersion.incrementAndGet();

      synchronized (uncommittedCNids) {
         synchronized (uncommittedCNidsNoChecks) {
            synchronized (uncommittedWfMemberIds) {
               try {
                  NidBitSetItrBI uncommittedCNidsItr         = uncommittedCNids.iterator();
                  NidBitSetItrBI uncommittedCNidsNoChecksItr = uncommittedCNidsNoChecks.iterator();

                  while (uncommittedCNidsItr.next()) {
                     AceLog.getAppLog().info(
                         "Canceling on concept: "
                         + Ts.get().getComponent(uncommittedCNidsItr.nid()).toUserString() + " UUID: "
                         + Ts.get().getUuidsForNid(uncommittedCNidsItr.nid()).toString());
                  }

                  while (uncommittedCNidsNoChecksItr.next()) {
                     AceLog.getAppLog().info(
                         "Canceling on concept: "
                         + Ts.get().getComponent(uncommittedCNidsNoChecksItr.nid()).toUserString()
                         + " UUID: " + Ts.get().getUuidsForNid(uncommittedCNidsNoChecksItr.nid()).toString());
                  }

                  KindOfComputer.reset();
                  handleCanceledConcepts(uncommittedCNids);
                  handleCanceledConcepts(uncommittedCNidsNoChecks);
                  uncommittedCNidsNoChecks.clear();
                  uncommittedCNids.clear();
                  Bdb.getSapDb().commit(Long.MIN_VALUE);
                  DataCheckRunner.cancelAll();
                  dataCheckMap.clear();
               } catch (IOException e1) {
                  AceLog.getAppLog().alertAndLogException(e1);
               }
            }
         }
      }

      fireCancel();
      updateFrames();
   }

   public static boolean commit() {
      return commit(ChangeSetPolicy.MUTABLE_ONLY, ChangeSetWriterThreading.SINGLE_THREAD);
   }

   public static boolean commit(ChangeSetPolicy changeSetPolicy,
                                ChangeSetWriterThreading changeSetWriterThreading) {
      Svn.rwl.acquireUninterruptibly();

      boolean passedRelease = false;

      try {
         synchronized (uncommittedCNids) {
            synchronized (uncommittedCNidsNoChecks) {
               synchronized (uncommittedWfMemberIds) {
                  flushUncommitted();
                  performCommit = true;

                  int errorCount   = 0;
                  int warningCount = 0;

                  if (performCreationTests) {
                     NidBitSetItrBI uncommittedCNidItr = uncommittedCNids.iterator();

                     DataCheckRunner.cancelAll();
                     dataCheckMap.clear();

                     while (uncommittedCNidItr.next()) {
                        Set<AlertToDataConstraintFailure> warningsAndErrors =
                           new HashSet<AlertToDataConstraintFailure>();
                        Concept concept = Concept.get(uncommittedCNidItr.nid());

                        dataCheckMap.put(concept, warningsAndErrors);

                        DataCheckRunner checkRunner = DataCheckRunner.runDataChecks(concept, commitTests);

                        checkRunner.latch.await();
                        warningsAndErrors.addAll(checkRunner.get());

                        for (AlertToDataConstraintFailure alert : warningsAndErrors) {
                           if (alert.getAlertType().equals(ALERT_TYPE.ERROR)) {
                              errorCount++;
                           } else if (alert.getAlertType().equals(ALERT_TYPE.WARNING)) {
                              warningCount++;
                           }
                        }
                     }
                  }

                  if (errorCount + warningCount != 0) {
                     if (errorCount > 0) {
                        performCommit = false;
                        SwingUtilities.invokeLater(new Runnable() {
                           @Override
                           public void run() {
                              JOptionPane.showMessageDialog(new JFrame(),
                                                            "Please fix data errors prior to commit.",
                                                            "Data errors exist", JOptionPane.ERROR_MESSAGE);
                           }
                        });
                     } else {
                        if (SwingUtilities.isEventDispatchThread()) {
                           int selection = JOptionPane.showConfirmDialog(new JFrame(),
                                              "Do you want to continue with commit?", "Warnings Detected",
                                              JOptionPane.YES_NO_OPTION);

                           performCommit = selection == JOptionPane.YES_OPTION;
                        } else {
                           try {
                              SwingUtilities.invokeAndWait(new Runnable() {
                                 @Override
                                 public void run() {
                                    int selection = JOptionPane.showConfirmDialog(new JFrame(),
                                                       "Do you want to continue with commit?",
                                                       "Warnings Detected", JOptionPane.YES_NO_OPTION);

                                    performCommit = selection == JOptionPane.YES_OPTION;
                                 }
                              });
                           } catch (InvocationTargetException e) {
                              AceLog.getAppLog().alertAndLogException(e);
                              performCommit = false;
                           }
                        }
                     }
                  }

                  if (performCommit) {
                     lastCommit = Bdb.gVersion.incrementAndGet();

                     for (Concept annotationConcept : Bdb.annotationConcepts) {
                        dbWriterService.execute(new ConceptWriter(annotationConcept));
                     }

                     Bdb.annotationConcepts.clear();
                     KindOfComputer.reset();

                     NidBitSetItrBI uncommittedCNidItr = uncommittedCNids.iterator();

                     while (uncommittedCNidItr.next()) {
                        if (getActiveFrame() != null) {
                           int cnid = uncommittedCNidItr.nid();

                           for (IsaCoordinate isac :
                                   getActiveFrame().getViewCoordinate().getIsaCoordinates()) {
                              KindOfComputer.updateIsaCacheUsingStatedView(isac, cnid);
                           }

                           Concept c = Concept.get(cnid);

                           c.modified(lastCommit);
                        }
                     }

                     NidBitSetItrBI uncommittedCNidItrNoChecks = uncommittedCNidsNoChecks.iterator();

                     while (uncommittedCNidItrNoChecks.next()) {
                        if (getActiveFrame() != null) {

                           for (IsaCoordinate isac :
                                   getActiveFrame().getViewCoordinate().getIsaCoordinates()) {
                              KindOfComputer.updateIsaCacheUsingStatedView(isac,
                                      uncommittedCNidItrNoChecks.nid());
                           }
                        }
                     }

                     long   commitTime        = System.currentTimeMillis();
                     IntSet sapNidsFromCommit = Bdb.getSapDb().commit(commitTime);

                     if (writeChangeSets && (sapNidsFromCommit.size() > 0)) {
                        if (changeSetPolicy == null) {
                           changeSetPolicy = ChangeSetPolicy.OFF;
                        }

                        if (changeSetWriterThreading == null) {
                           changeSetWriterThreading = ChangeSetWriterThreading.SINGLE_THREAD;
                        }

                        switch (changeSetPolicy) {
                        case COMPREHENSIVE :
                        case INCREMENTAL :
                        case MUTABLE_ONLY :
                           uncommittedCNidsNoChecks.or(uncommittedCNids);

                           if (uncommittedCNidsNoChecks.cardinality() > 0) {
                              ChangeSetWriterHandler handler =
                                 new ChangeSetWriterHandler(uncommittedCNidsNoChecks, commitTime,
                                                            sapNidsFromCommit, changeSetPolicy.convert(),
                                                            changeSetWriterThreading, Svn.rwl);

                              changeSetWriterService.execute(handler);
                              passedRelease = true;
                           }

                           break;

                        case OFF :
                           break;

                        default :
                           throw new RuntimeException("Can't handle policy: " + changeSetPolicy);
                        }
                     }

                     uncommittedCNids.clear();
                     uncommittedCNidsNoChecks = Terms.get().getEmptyIdSet();
                     WorkflowHistoryRefsetWriter.unLockMutex();
                     luceneWriterPermit.acquire();

                     IdentifierSet descNidsToCommit = new IdentifierSet((IdentifierSet) uncommittedDescNids);

                     uncommittedDescNids.clear();
                     luceneWriterService.execute(new DescLuceneWriter(descNidsToCommit));

                     Set<I_ExtendByRef> wfMembersToCommit = uncommittedWfMemberIds.getClass().newInstance();

                     wfMembersToCommit.addAll(uncommittedWfMemberIds);
                     luceneWriterService.execute(new WfHxLuceneWriter(wfMembersToCommit));
                     uncommittedWfMemberIds.clear();
                     dataCheckMap.clear();
                  }
               }
            }
         }

         if (performCommit) {
            Bdb.sync();
            BdbCommitSequence.nextSequence();
         }
      } catch (IOException e1) {
         AceLog.getAppLog().alertAndLogException(e1);
      } catch (InterruptedException e1) {
         AceLog.getAppLog().alertAndLogException(e1);
      } catch (ExecutionException e1) {
         AceLog.getAppLog().alertAndLogException(e1);
      } catch (TerminologyException e1) {
         AceLog.getAppLog().alertAndLogException(e1);
      } catch (Exception e1) {
         AceLog.getAppLog().alertAndLogException(e1);
      } finally {
         if (!passedRelease) {
            Svn.rwl.release();
         }
      }

      fireCommit();

      if (performCommit) {
         return true;
      }

      return false;
   }

   public static boolean commit(Concept c, ChangeSetPolicy changeSetPolicy,
                                ChangeSetWriterThreading changeSetWriterThreading) {
      if ((uncommittedCNids.cardinality() == 1) && (uncommittedCNidsNoChecks.cardinality() == 1)
              && uncommittedCNids.isMember(c.getNid()) && uncommittedCNidsNoChecks.isMember(c.getNid())) {
         return commit(changeSetPolicy, changeSetWriterThreading);
      } else if ((uncommittedCNids.cardinality() == 1) && (uncommittedCNidsNoChecks.cardinality() == 0)
                 && uncommittedCNids.isMember(c.getNid())) {
         return commit(changeSetPolicy, changeSetWriterThreading);
      } else if ((uncommittedCNids.cardinality() == 0) && (uncommittedCNidsNoChecks.cardinality() == 1)
                 && uncommittedCNidsNoChecks.isMember(c.getNid())) {
         return commit(changeSetPolicy, changeSetWriterThreading);
      }

      Svn.rwl.acquireUninterruptibly();

      try {
         AceLog.getAppLog().info("Committing concept: " + c.toUserString() + " UUID: "
                                 + Ts.get().getUuidsForNid(c.getNid()).toString());

         int errorCount   = 0;
         int warningCount = 0;

         performCommit = true;

         Set<AlertToDataConstraintFailure> warningsAndErrors = new HashSet<AlertToDataConstraintFailure>();

         dataCheckMap.put(c, warningsAndErrors);

         DataCheckRunner checkRunner = DataCheckRunner.runDataChecks(c, commitTests);
         CountDownLatch  latch       = checkRunner.latch;

         latch.await();
         warningsAndErrors.addAll(checkRunner.get());

         for (AlertToDataConstraintFailure alert : warningsAndErrors) {
            if (alert.getAlertType().equals(ALERT_TYPE.ERROR)) {
               errorCount++;
            } else if (alert.getAlertType().equals(ALERT_TYPE.WARNING)) {
               warningCount++;
            }
         }

         if (errorCount + warningCount != 0) {
            if (errorCount > 0) {
               performCommit = false;
               SwingUtilities.invokeLater(new Runnable() {
                  @Override
                  public void run() {
                     JOptionPane.showMessageDialog(new JFrame(), "Please fix data errors prior to commit.",
                                                   "Data errors exist", JOptionPane.ERROR_MESSAGE);
                  }
               });
            } else {
               if (SwingUtilities.isEventDispatchThread()) {
                  int selection = JOptionPane.showConfirmDialog(new JFrame(),
                                     "Do you want to continue with commit?", "Warnings Detected",
                                     JOptionPane.YES_NO_OPTION);

                  performCommit = selection == JOptionPane.YES_OPTION;
               } else {
                  try {
                     SwingUtilities.invokeAndWait(new Runnable() {
                        @Override
                        public void run() {
                           int selection = JOptionPane.showConfirmDialog(new JFrame(),
                                              "Do you want to continue with commit?", "Warnings Detected",
                                              JOptionPane.YES_NO_OPTION);

                           performCommit = selection == JOptionPane.YES_OPTION;
                        }
                     });
                  } catch (InvocationTargetException e) {
                     AceLog.getAppLog().alertAndLogException(e);
                     performCommit = false;
                  }
               }
            }
         }

         if (performCommit) {
            BdbCommitSequence.nextSequence();

            for (Concept annotationConcept : Bdb.annotationConcepts) {
               dbWriterService.execute(new ConceptWriter(annotationConcept));
            }

            Bdb.annotationConcepts.clear();
            KindOfComputer.reset();

            for (IsaCoordinate isac : getActiveFrame().getViewCoordinate().getIsaCoordinates()) {
               KindOfComputer.updateIsaCacheUsingStatedView(isac, c.getNid());
            }

            long          commitTime        = System.currentTimeMillis();
            NidSetBI      sapNidsFromCommit = c.setCommitTime(commitTime);
            IdentifierSet commitSet         = new IdentifierSet();

            commitSet.setMember(c.getNid());
            c.modified();
            Bdb.getConceptDb().writeConcept(c);

            if (wfHistoryRefsetId < 0) {
                commitSet.setMember(wfHistoryRefsetId);
                Concept wfRefset = (Concept) Ts.get().getConcept(wfHistoryRefsetId);
                sapNidsFromCommit.addAll(wfRefset.setCommitTime(commitTime).getSetValues());
                wfRefset.modified();
                Bdb.getConceptDb().writeConcept(wfRefset);
            }

            if (writeChangeSets) {
               if (changeSetPolicy == null) {
                  changeSetPolicy = ChangeSetPolicy.OFF;
               }

               if (changeSetWriterThreading == null) {
                  changeSetWriterThreading = ChangeSetWriterThreading.SINGLE_THREAD;
               }

               switch (changeSetPolicy) {
               case COMPREHENSIVE :
               case INCREMENTAL :
               case MUTABLE_ONLY :
                  ChangeSetWriterHandler handler = new ChangeSetWriterHandler(commitSet, commitTime,
                                                      sapNidsFromCommit, changeSetPolicy.convert(),
                                                      changeSetWriterThreading, Svn.rwl);

                  changeSetWriterService.execute(handler);

                  break;

               case OFF :
                  break;

               default :
                  throw new RuntimeException("Can't handle policy: " + changeSetPolicy);
               }
            }

            uncommittedCNids.andNot(commitSet);
            uncommittedCNidsNoChecks.andNot(commitSet);
            WorkflowHistoryRefsetWriter.unLockMutex();
            luceneWriterPermit.acquire();

            IdentifierSet descNidsToCommit = new IdentifierSet();

            for (int dnid : c.getData().getDescNids()) {
               descNidsToCommit.setMember(dnid);
               uncommittedDescNids.setNotMember(dnid);
            }

            luceneWriterService.execute(new DescLuceneWriter(descNidsToCommit));

            Set<I_ExtendByRef> wfMembersToCommit = uncommittedWfMemberIds.getClass().newInstance();

            wfMembersToCommit.addAll(uncommittedWfMemberIds);
            luceneWriterService.execute(new WfHxLuceneWriter(wfMembersToCommit));
            uncommittedWfMemberIds.clear();
            dataCheckMap.remove(c);
         }
      } catch (Exception e1) {
         AceLog.getAppLog().alertAndLogException(e1);
      } finally {
         Svn.rwl.release();
      }

      fireCommit();

      if (performCommit) {
         return true;
      }

      return false;
   }

   private static void doUpdate() {
      if (lastDoUpdate < Bdb.gVersion.get()) {
         lastDoUpdate = Bdb.gVersion.get();

         try {
            for (Frame f : OpenFrames.getFrames()) {
               if (AceFrame.class.isAssignableFrom(f.getClass())) {
                  AceFrame af          = (AceFrame) f;
                  ACE      aceInstance = af.getCdePanel();

                  aceInstance.getDataCheckListScroller();
                  aceInstance.getUncommittedListModel().clear();

                  for (Collection<AlertToDataConstraintFailure> alerts : dataCheckMap.values()) {
                     aceInstance.getUncommittedListModel().addAll(alerts);
                  }

                  if (aceInstance.getUncommittedListModel().size() > 0) {
                     for (int i = 0; i < aceInstance.getLeftTabs().getTabCount(); i++) {
                        if (aceInstance.getLeftTabs().getTitleAt(i).equals(ACE.DATA_CHECK_TAB_LABEL)) {
                           aceInstance.getLeftTabs().setSelectedIndex(i);

                           break;
                        }
                     }

                     // show data checks tab...
                  } else {
                     for (TermComponentDataCheckSelectionListener l : aceInstance.getDataCheckListeners()) {
                        l.setSelection(null);
                     }

                     // hide data checks tab...
                  }

                  if (uncommittedCNids.cardinality() == 0) {
                     aceInstance.aceFrameConfig.setCommitEnabled(false);
                     aceInstance.aceFrameConfig.fireCommit();
                  } else {
                     aceInstance.aceFrameConfig.setCommitEnabled(true);
                  }
               } else if (ContradictionEditorFrame.class.isAssignableFrom(f.getClass())) {
                  ContradictionEditorFrame cef = (ContradictionEditorFrame) f;

                  if (uncommittedCNids.cardinality() == 0) {
                     cef.getActiveFrameConfig().setCommitEnabled(false);
                     cef.getActiveFrameConfig().fireCommit();
                  } else {
                     cef.getActiveFrameConfig().setCommitEnabled(true);
                  }
               }
            }
         } catch (Exception e) {
            AceLog.getAppLog().warning(e.toString());
         }
      }
   }

   public static void fireCancel() {
      if (DwfaEnv.isHeadless()) {
         return;
      }

      SwingUtilities.invokeLater(new Runnable() {
         @Override
         public void run() {
            try {
               if (Terms.get().getActiveAceFrameConfig() != null) {
                  for (I_ConfigAceFrame frameConfig :
                          Terms.get().getActiveAceFrameConfig().getDbConfig().getAceFrames()) {
                     frameConfig.fireCommit();
                     frameConfig.setCommitEnabled(false);
                  }
               }

               updateAlerts();
            } catch (TerminologyException e) {
               AceLog.getAppLog().alertAndLogException(e);
            } catch (IOException e) {
               AceLog.getAppLog().alertAndLogException(e);
            }
         }
      });
   }

   private static void fireCommit() {
      if (DwfaEnv.isHeadless()) {
         return;
      }

      SwingUtilities.invokeLater(new Runnable() {
         @Override
         public void run() {
            try {
               if (Terms.get().getActiveAceFrameConfig() != null) {
                  for (I_ConfigAceFrame frameConfig :
                          Terms.get().getActiveAceFrameConfig().getDbConfig().getAceFrames()) {
                     frameConfig.fireCommit();
                     frameConfig.setCommitEnabled(false);
                  }

                  updateAlerts();
               }
            } catch (TerminologyException e) {
               AceLog.getAppLog().alertAndLogException(e);
            } catch (IOException e) {
               AceLog.getAppLog().alertAndLogException(e);
            }
         }
      });
   }

   private static void flushUncommitted() throws InterruptedException {
      Concept c = lastUncommitted.getAndSet(null);

      if (c != null) {
         writeUncommitted(c);
      }
   }

   public static boolean forget(I_ConceptAttributeVersioned attr) throws IOException {
      Concept           c = Bdb.getConcept(attr.getConId());
      ConceptAttributes a = (ConceptAttributes) attr;

      if ((a.getTime() != Long.MAX_VALUE) && (a.getTime() != Long.MIN_VALUE)) {

         // Only need to forget additional versions;
         if (a.revisions != null) {
            synchronized (a.revisions) {
               List<ConceptAttributesRevision>     toRemove = new ArrayList<ConceptAttributesRevision>();
               Iterator<ConceptAttributesRevision> ri       = a.revisions.iterator();

               while (ri.hasNext()) {
                  ConceptAttributesRevision ar = ri.next();

                  if (ar.getTime() == Long.MAX_VALUE) {
                     toRemove.add(ar);
                  }
               }

               for (ConceptAttributesRevision r : toRemove) {
                  a.removeRevision(r);
                  r.sapNid = -1;
               }
            }
         }

         try {
            KindOfComputer.updateIsaCaches((Concept) c);
         } catch (Exception e) {
            AceLog.getAppLog().alertAndLog(Level.SEVERE, "Canceling cache for: " + c.toString(), e);
         }

         Terms.get().addUncommittedNoChecks(c);
      } else {
         a.primordialSapNid = -1;

         return true;
      }

      return false;
   }

   public static void forget(I_DescriptionVersioned desc) throws IOException {
      Description d = (Description) desc;
      Concept     c = Bdb.getConcept(d.getConceptNid());

      if (d.getTime() != Long.MAX_VALUE) {

         // Only need to forget additional versions;
         if (d.revisions == null) {
            throw new UnsupportedOperationException("Cannot forget a committed component.");
         } else {
            synchronized (d.revisions) {
               List<DescriptionRevision>     toRemove = new ArrayList<DescriptionRevision>();
               Iterator<DescriptionRevision> di       = d.revisions.iterator();

               while (di.hasNext()) {
                  DescriptionRevision dr = di.next();

                  if (dr.getTime() == Long.MAX_VALUE) {
                     toRemove.add(dr);
                  }
               }

               for (DescriptionRevision tr : toRemove) {
                  d.removeRevision(tr);
                  tr.sapNid = -1;
               }
            }
         }
      } else {

         // have to forget "all" references to component...
         c.getDescriptions().remove(d);
         c.getData().getDescNids().remove(d.getNid());
         d.primordialSapNid = -1;
      }

      c.modified();
      Terms.get().addUncommittedNoChecks(c);
   }

   @SuppressWarnings("unchecked")
   public static void forget(I_ExtendByRef extension) throws IOException {
      RefsetMember m         = (RefsetMember) extension;
      Concept      c         = Bdb.getConcept(m.getRefsetId());
      ComponentBI  component = Bdb.getComponent(m.getComponentNid());

      if (component instanceof Concept) {
         component = ((Concept) component).getConAttrs();
      }

      ConceptComponent comp = (ConceptComponent) component;

      if (m.getTime() != Long.MAX_VALUE) {

         // Only need to forget additional versions;
         if (m.revisions == null) {
            throw new UnsupportedOperationException("Cannot forget a committed component.");
         } else {
            synchronized (m.revisions) {
               List<RefsetRevision<?, ?>> toRemove = new ArrayList<RefsetRevision<?, ?>>();
               Iterator<?>                mi       = m.revisions.iterator();

               while (mi.hasNext()) {
                  RefsetRevision<?, ?> mr = (RefsetRevision<?, ?>) mi.next();

                  if (mr.getTime() == Long.MAX_VALUE) {
                     toRemove.add(mr);
                  }
               }

               for (RefsetRevision tr : toRemove) {
                  m.removeRevision(tr);
                  tr.sapNid = -1;
               }
            }
         }
      } else {

         // have to forget "all" references to component...
         if (c.isAnnotationStyleRefex()) {
            comp.getAnnotationsMod().remove(m);
         } else {
            c.getRefsetMembers().remove(m);
            c.getData().getMemberNids().remove(m.getMemberId());
         }

         m.setStatusAtPositionNid(-1);
      }

      c.modified();
      Terms.get().addUncommittedNoChecks(c);
   }

   public static void forget(I_GetConceptData concept) throws IOException {
      Concept c = (Concept) concept;

      c.cancel();
   }

   public static void forget(I_RelVersioned rel) throws IOException {
      Concept      c = Bdb.getConcept(rel.getC1Id());
      Relationship r = (Relationship) rel;

      if (r.getTime() != Long.MAX_VALUE) {

         // Only need to forget additional versions;
         if (r.revisions == null) {
            throw new UnsupportedOperationException("Cannot forget a committed component.");
         } else {
            synchronized (r.revisions) {
               List<RelationshipRevision>     toRemove = new ArrayList<RelationshipRevision>();
               Iterator<RelationshipRevision> ri       = r.revisions.iterator();

               while (ri.hasNext()) {
                  RelationshipRevision rr = ri.next();

                  if (rr.getTime() == Long.MAX_VALUE) {
                     toRemove.add(rr);
                  }
               }

               for (RelationshipRevision tr : toRemove) {
                  r.removeRevision(tr);
               }
            }
         }
      } else {

         // have to forget "all" references to component...
         c.getSourceRels().remove((Relationship) rel);
         c.getData().getSrcRelNids().remove(rel.getNid());
         r.primordialSapNid = -1;
      }

      c.modified();

      try {
         KindOfComputer.updateIsaCaches((Concept) c);
      } catch (Exception e) {
         AceLog.getAppLog().alertAndLog(Level.SEVERE, "Canceling cache for: " + c.toString(), e);
      }

      Terms.get().addUncommittedNoChecks(c);
   }

   private static void handleCanceledConcepts(I_RepresentIdSet uncommittedCNids2) throws IOException {
      NidBitSetItrBI idItr = uncommittedCNids2.iterator();

      while (idItr.next()) {
         try {
            Concept c = Concept.get(idItr.nid());

            if (c.isCanceled()) {
               Terms.get().forget(c);
            }

            c.flushVersions();
            c.modified();
            c.setLastWrite(Bdb.gVersion.incrementAndGet());

            try {
               KindOfComputer.updateIsaCaches((Concept) c);
            } catch (Exception e) {
               AceLog.getAppLog().alertAndLog(Level.SEVERE, "Canceling cache for: " + c.toString(), e);
            }
         } catch (Exception ex) {
            AceLog.getAppLog().alertAndLogException(ex);
         }
      }
   }

   private static void loadTests(String directory, List<I_TestDataConstraints> list) {
      File   componentPluginDir = new File(pluginRoot + File.separator + directory);
      File[] plugins            = componentPluginDir.listFiles(new FilenameFilter() {
         @Override
         public boolean accept(File arg0, String fileName) {
            return fileName.toLowerCase().endsWith(".task");
         }
      });

      if (plugins != null) {
         for (File f : plugins) {
            try {
               FileInputStream       fis  = new FileInputStream(f);
               BufferedInputStream   bis  = new BufferedInputStream(fis);
               ObjectInputStream     ois  = new ObjectInputStream(bis);
               I_TestDataConstraints test = (I_TestDataConstraints) ois.readObject();

               ois.close();
               list.add(test);
            } catch (Exception e) {
               AceLog.getAppLog().alertAndLog(Level.WARNING, "Processing: " + f.getAbsolutePath(), e);
            }
         }
      }
   }

   public static void removeUncommitted(final Concept concept) {
      if (uncommittedCNids.isMember(concept.getNid())) {
         uncommittedCNids.setNotMember(concept.getNid());

         if (uncommittedCNids.cardinality() == 0) {
            dataCheckMap.clear();
         } else {
            dataCheckMap.remove(concept);
         }

         if (getActiveFrame() != null) {
            SwingUtilities.invokeLater(new Runnable() {
               @Override
               public void run() {
                  removeUncommittedUpdateFrame(concept);
               }
            });
         }
      }
   }

   private static void removeUncommittedUpdateFrame(Concept concept) {
      for (I_ConfigAceFrame frameConfig : getActiveFrame().getDbConfig().getAceFrames()) {
         try {
            frameConfig.removeUncommitted(concept);
            updateAlerts();

            if (uncommittedCNids.cardinality() == 0) {
               frameConfig.setCommitEnabled(false);
            }
         } catch (Exception e) {
            AceLog.getAppLog().warning(e.toString());
         }
      }
   }

   public static void reset() {
      changeSetWriterService = Executors.newFixedThreadPool(1,
              new NamedThreadFactory(commitManagerThreadGroup, "Change set writer"));
      dbWriterService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors(),
              new NamedThreadFactory(commitManagerThreadGroup, "Db writer"));
      luceneWriterService = Executors.newFixedThreadPool(1,
              new NamedThreadFactory(commitManagerThreadGroup, "Lucene writer"));
      loadTests("commit", commitTests);
      loadTests("precommit", creationTests);
   }

   public static void resumeChangeSetWriters() {
      writeChangeSets = true;
   }

   public static void shutdown() throws InterruptedException {
      cancel();
      AceLog.getAppLog().info("Shutting down dbWriterService.");
      dbWriterService.shutdown();
      AceLog.getAppLog().info("Awaiting termination of dbWriterService.");
      dbWriterService.awaitTermination(90, TimeUnit.MINUTES);
      AceLog.getAppLog().info("Shutting down luceneWriterService.");
      luceneWriterService.shutdown();
      AceLog.getAppLog().info("Awaiting termination of luceneWriterService.");
      luceneWriterService.awaitTermination(90, TimeUnit.MINUTES);
      AceLog.getAppLog().info("Shutting down changeSetWriterService.");
      changeSetWriterService.shutdown();
      AceLog.getAppLog().info("Awaiting termination of changeSetWriterService.");
      changeSetWriterService.awaitTermination(90, TimeUnit.MINUTES);
      AceLog.getAppLog().info("BdbCommitManager is shutdown.");
   }

   public static void suspendChangeSetWriters() {
      writeChangeSets = false;
   }

   public static void updateAlerts() {
      SwingUtilities.invokeLater(new Runnable() {
         @Override
         public void run() {
            doUpdate();
         }
      });
   }

   public static void updateFrames() {
      SwingUtilities.invokeLater(new Runnable() {
         @Override
         public void run() {
            doUpdate();
         }
      });
   }

   public static void waitTillWritesFinished() {
      if (writerCount.get() > 0) {
         try {
            dbWriterPermit.acquireUninterruptibly(PERMIT_COUNT);
         } finally {
            dbWriterPermit.release(PERMIT_COUNT);
         }
      }
   }

   public static void writeImmediate(Concept concept) {
      new ConceptWriter(concept).run();
   }

   private static void writeUncommitted(Concept c) throws InterruptedException {
      if (c != null) {
         if (Bdb.watchList.containsKey(c.getNid())) {
            AceLog.getAppLog().info("---@@@ writeUncommitted checks: " + c.getNid() + " ---@@@ ");
         }

         dbWriterPermit.acquire();
         dbWriterService.execute(new SetNidsForCid(c));
         dbWriterService.execute(new ConceptWriter(c));
      }
   }

   //~--- get methods ---------------------------------------------------------

   private static I_ConfigAceFrame getActiveFrame() {
      try {
         return Terms.get().getActiveAceFrameConfig();
      } catch (TerminologyException e) {
         AceLog.getAppLog().alertAndLogException(e);
      } catch (IOException e) {
         AceLog.getAppLog().alertAndLogException(e);
      }

      return null;
   }

   public static List<AlertToDataConstraintFailure> getCommitErrorsAndWarnings() {
      Set<AlertToDataConstraintFailure> warningsAndErrors = new HashSet<AlertToDataConstraintFailure>();

      try {
         NidBitSetItrBI cNidItr = uncommittedCNids.iterator();

         while (cNidItr.next()) {
            try {
               Concept         toTest      = Concept.get(cNidItr.nid());
               DataCheckRunner checkRunner = DataCheckRunner.runDataChecks(toTest, commitTests);

               checkRunner.latch.await();
            } catch (IOException e) {
               AceLog.getAppLog().alertAndLogException(e);
            } catch (InterruptedException e) {

               // TODO Auto-generated catch block
               e.printStackTrace();
            }
         }
      } catch (IOException e) {
         AceLog.getAppLog().alertAndLogException(e);
      }

      List<AlertToDataConstraintFailure> warningsAndErrorsList =
         new ArrayList<AlertToDataConstraintFailure>();

      warningsAndErrorsList.addAll(warningsAndErrors);

      return warningsAndErrorsList;
   }

   public static ConcurrentHashMap<I_GetConceptData,
                                   Collection<AlertToDataConstraintFailure>> getDatacheckMap() {
      return dataCheckMap;
   }

   public static long getLastCancel() {
      return lastCancel;
   }

   public static long getLastCommit() {
      return lastCommit;
   }

   public static Set<Concept> getUncommitted() {
      try {
         Set<Concept>   returnSet = new HashSet<Concept>();
         NidBitSetItrBI cNidItr   = uncommittedCNids.iterator();

         while (cNidItr.next()) {
            returnSet.add(Concept.get(cNidItr.nid()));
         }

         cNidItr = uncommittedCNidsNoChecks.iterator();

         while (cNidItr.next()) {
            returnSet.add(Concept.get(cNidItr.nid()));
         }

         return returnSet;
      } catch (IOException e) {
         throw new RuntimeException(e);
      }
   }

   public static boolean isCheckCommitDataEnabled() {
      return performCommitTests;
   }

   public static boolean isCheckCreationDataEnabled() {
      return performCreationTests;
   }

   //~--- set methods ---------------------------------------------------------

   public static void setCheckCommitDataEnabled(boolean enabled) {
      performCommitTests = enabled;
   }

   public static void setCheckCreationDataEnabled(boolean enabled) {
      performCreationTests = enabled;
   }

   //~--- inner classes -------------------------------------------------------

   private static class ConceptWriter implements Runnable {
      private Concept c;

      //~--- constructors -----------------------------------------------------

      public ConceptWriter(Concept c) {
         super();
         assert c.readyToWrite();
         this.c = c;
         writerCount.incrementAndGet();
      }

      //~--- methods ----------------------------------------------------------

      @Override
      public void run() {
         try {
            while (c.isUnwritten() &&!c.isCanceled()) {
               Bdb.getConceptDb().writeConcept(c);
            }
         } catch (Throwable e) {
            String    exceptionStr = "Exception Writing: " + c.toLongString();
            Exception newEx        = new Exception(exceptionStr, e);

            System.out.println(exceptionStr + "\n\n" + e.toString());
            AceLog.getAppLog().alertAndLogException(newEx);
         } finally {
            dbWriterPermit.release();
            writerCount.decrementAndGet();
         }
      }
   }


   public static class DataCheckRunner
           extends SwingWorker<Collection<AlertToDataConstraintFailure>,
                               Collection<AlertToDataConstraintFailure>> {
      private static ConcurrentHashMap<Concept, DataCheckRunner> runners = new ConcurrentHashMap<Concept,
                                                                              DataCheckRunner>();

      //~--- fields -----------------------------------------------------------

      private boolean                     canceled = false;
      private Concept                     c;
      private CountDownLatch              latch;
      private List<I_TestDataConstraints> tests;

      //~--- constructors -----------------------------------------------------

      private DataCheckRunner(Concept c, List<I_TestDataConstraints> tests) {
         this.c     = c;
         this.tests = tests;
         latch      = new CountDownLatch(tests.size());

         DataCheckRunner oldRunner = runners.put(c, this);

         if (oldRunner != null) {
            oldRunner.cancel();
         }
      }

      //~--- methods ----------------------------------------------------------

      public void cancel() {
         while (latch.getCount() > 0) {
            latch.countDown();
         }

         this.canceled = true;
      }

      public static void cancelAll() {
         for (DataCheckRunner runner : runners.values()) {
            runner.cancel();
         }

         runners.clear();
      }

      @Override
      protected Collection<AlertToDataConstraintFailure> doInBackground() throws Exception {
         List<AlertToDataConstraintFailure> runnerAlerts = new ArrayList<AlertToDataConstraintFailure>();

         if (canceled) {
            return runnerAlerts;
         }

         if ((c != null) && (tests != null)) {
            for (I_TestDataConstraints test : tests) {
               if (canceled) {
                  return runnerAlerts;
               }

               try {
                  Collection<AlertToDataConstraintFailure> result = test.test(c, true);

                  runnerAlerts.addAll(result);

                  if (canceled) {
                     return runnerAlerts;
                  }

                  publish(result);

                  Collection<RefsetMember<?, ?>> extensions = c.getExtensions();

                  for (RefsetMember<?, ?> extension : extensions) {
                     if (canceled) {
                        return runnerAlerts;
                     }

                     if (extension.isUncommitted()) {
                        result = test.test(extension, true);
                        runnerAlerts.addAll(result);
                        publish(result);

                        if (canceled) {
                           return runnerAlerts;
                        }
                     }
                  }
               } catch (Throwable e) {
                  AceLog.getEditLog().alertAndLogException(e);
               }

               latch.countDown();
            }
         }

         return runnerAlerts;
      }

      @Override
      protected void done() {
         super.done();

         if (!canceled) {
            runners.remove(c);
         }
      }

      @Override
      protected void process(List<Collection<AlertToDataConstraintFailure>> chunks) {
         for (Collection<AlertToDataConstraintFailure> results : chunks) {
            Collection<AlertToDataConstraintFailure> currentAlerts = dataCheckMap.get(c);

            if (currentAlerts == null) {
               currentAlerts = new HashSet<AlertToDataConstraintFailure>();
            }

            currentAlerts.addAll(results);
            dataCheckMap.put(c, currentAlerts);
         }

         if (canceled) {
            return;
         }

         doUpdate();
      }

      public static DataCheckRunner runDataChecks(Concept c, List<I_TestDataConstraints> tests) {
         DataCheckRunner runner = new DataCheckRunner(c, tests);

         runner.execute();

         return runner;
      }

      //~--- get methods ------------------------------------------------------

      public CountDownLatch getLatch() {
         return latch;
      }

      public boolean isCanceled() {
         return canceled;
      }
   }


   private static class DescLuceneWriter implements Runnable {
      private int           batchSize = 200;
      private IdentifierSet descNidsToWrite;

      //~--- constructors -----------------------------------------------------

      public DescLuceneWriter(IdentifierSet descNidsToCommit) {
         super();
         this.descNidsToWrite = descNidsToCommit;
      }

      //~--- methods ----------------------------------------------------------

      @Override
      public void run() {
         try {
            ArrayList<Description> toIndex = new ArrayList<Description>(batchSize + 1);
            I_IterateIds           idItr   = descNidsToWrite.iterator();
            int                    count   = 0;

            while (idItr.next()) {
               count++;

               Description d = (Description) Bdb.getComponent(idItr.nid());

               toIndex.add(d);

               if (count > batchSize) {
                  count = 0;
                  LuceneManager.writeToLucene(toIndex, LuceneSearchType.DESCRIPTION);
                  toIndex = new ArrayList<Description>(batchSize + 1);
               }
            }

            LuceneManager.writeToLucene(toIndex, LuceneSearchType.DESCRIPTION);
         } catch (Exception e) {
            AceLog.getAppLog().alertAndLogException(e);
         }

         luceneWriterPermit.release();
      }
   }


   private static class SetNidsForCid implements Runnable {
      Concept concept;

      //~--- constructors -----------------------------------------------------

      public SetNidsForCid(Concept concept) {
         super();
         this.concept = concept;
      }

      //~--- methods ----------------------------------------------------------

      @Override
      public void run() {
         try {
            Collection<Integer> nids      = concept.getAllNids();
            NidCNidMapBdb       nidCidMap = Bdb.getNidCNidMap();

            for (int nid : nids) {
               nidCidMap.setCNidForNid(concept.getNid(), nid);
            }
         } catch (IOException e) {
            AceLog.getAppLog().alertAndLogException(e);
         }
      }
   }


   private static class UpdateFrames implements Runnable {
      Concept c;

      //~--- constructors -----------------------------------------------------

      public UpdateFrames(Concept c) {
         super();
         this.c = c;
      }

      //~--- methods ----------------------------------------------------------

      @Override
      public void run() {
         if (getActiveFrame() != null) {
            updateAlerts();

            I_ConfigAceFrame activeFrame = getActiveFrame();

            for (Frame f : OpenFrames.getFrames()) {
               I_ConfigAceFrame frameConfig = null;

               if (f instanceof ContradictionEditorFrame) {
                  frameConfig = ((ContradictionEditorFrame) f).getActiveFrameConfig();
               } else if (f instanceof AceFrame) {
                  frameConfig = ((AceFrame) f).getCdePanel().getAceFrameConfig();
               }

               if (frameConfig != null) {
                  frameConfig.setCommitEnabled(true);

                  if (c.isUncommitted()) {
                     frameConfig.addUncommitted(c);
                  } else {
                     frameConfig.removeUncommitted(c);
                  }
               }
            }
         }
      }
   }


   private static class WfHxLuceneWriter implements Runnable {
      private static Set<I_ExtendByRef> wfExtensionsToUpdate;

      //~--- fields -----------------------------------------------------------

      private int                         batchSize = 200;
      private WorkflowHistoryRefsetReader reader;

      //~--- constructors -----------------------------------------------------

      public WfHxLuceneWriter(Set<I_ExtendByRef> uncommittedWfMemberIds) {
         super();
         wfExtensionsToUpdate = uncommittedWfMemberIds;

         try {
            reader = new WorkflowHistoryRefsetReader();
         } catch (Exception e) {
            AceLog.getAppLog().log(Level.WARNING,
                                   "Unable to access Workflow History Refset with error: " + e.getMessage());
         }
      }

      //~--- methods ----------------------------------------------------------

      @Override
      public void run() {
         try {
            Set<UUID> workflowsUpdated = new HashSet<UUID>();

            for (I_ExtendByRef row : wfExtensionsToUpdate) {
               UUID workflowId = reader.getWorkflowId(((I_ExtendByRefPartStr) row).getStringValue());

               // If two rows to commit, both will be caught by method below, so do this once per WfId
               if (!workflowsUpdated.contains(workflowId)) {
                  workflowsUpdated.add(workflowId);

                  I_GetConceptData                   con            =
                     Terms.get().getConcept(row.getComponentNid());
                  SortedSet<WorkflowHistoryJavaBean> latestWorkflow =
                     WorkflowHelper.getLatestWfHxForConcept(con, workflowId);

                  WfHxLuceneManager.setWorkflowId(workflowId);
                  LuceneManager.writeToLucene(latestWorkflow, LuceneSearchType.WORKFLOW_HISTORY,
                                              getActiveFrame().getViewCoordinate());
               }
            }
         } catch (Exception e) {
            AceLog.getAppLog().alertAndLogException(e);
         }

         luceneWriterPermit.release();
      }
   }
}
