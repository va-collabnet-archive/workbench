package org.ihtsdo.db.bdb;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import javax.swing.SwingUtilities;

import org.dwfa.ace.ACE;
import org.dwfa.ace.TermComponentDataCheckSelectionListener;
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
import org.dwfa.ace.config.AceFrameConfig;
import org.dwfa.ace.log.AceLog;
import org.dwfa.ace.task.commit.AlertToDataConstraintFailure;
import org.dwfa.ace.task.commit.I_TestDataConstraints;
import org.dwfa.app.DwfaEnv;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.vodb.types.IntSet;
import org.ihtsdo.concept.Concept;
import org.ihtsdo.concept.component.Revision;
import org.ihtsdo.concept.component.description.Description;
import org.ihtsdo.concept.component.description.DescriptionRevision;
import org.ihtsdo.concept.component.refset.RefsetMember;
import org.ihtsdo.concept.component.relationship.Relationship;
import org.ihtsdo.concept.component.relationship.RelationshipRevision;
import org.ihtsdo.cs.ChangeSetWriterHandler;
import org.ihtsdo.db.bdb.computer.kindof.KindOfComputer;
import org.ihtsdo.db.bdb.id.NidCNidMapBdb;
import org.ihtsdo.lucene.LuceneManager;
import org.ihtsdo.thread.NamedThreadFactory;

public class BdbCommitManager {

	private static Semaphore dbWriterPermit = new Semaphore(50);
	private static Semaphore luceneWriterPermit = new Semaphore(50);
	
	private static ThreadGroup commitManagerThreadGroup  = 
		new ThreadGroup("commit manager threads");
	
	private static class ConceptWriter implements Runnable {
		private Concept c;

		public ConceptWriter(Concept c) {
			super();
			this.c = c;
		}

		@Override
		public void run() {
			try {
				while (c.isUnwritten()) {
					Bdb.getConceptDb().writeConcept(c);
				}
			} catch (Exception e) {
				AceLog.getAppLog().alertAndLogException(e);
			}
			dbWriterPermit.release();
		}
	}

	private static class LuceneWriter implements Runnable {
	    private int batchSize = 200;
		private IdentifierSet descNidsToWrite;

		public LuceneWriter(IdentifierSet descNidsToCommit) {
			super();
			this.descNidsToWrite = descNidsToCommit;
		}

		@Override
		public void run() {
			try {
				ArrayList<Description> toIndex = new ArrayList<Description>(batchSize + 1);
				I_IterateIds idItr = descNidsToWrite.iterator();
				int count = 0;
				while (idItr.next()) {
				    count++;
					Description d = (Description) Bdb.getComponent(idItr.nid());
					toIndex.add(d);
					if (count > batchSize) {
					    count = 0;
		                LuceneManager.writeToLucene(toIndex);
		                toIndex = new ArrayList<Description>(batchSize + 1);
					}
				}
				LuceneManager.writeToLucene(toIndex);
			} catch (Exception e) {
				AceLog.getAppLog().alertAndLogException(e);
			}
			luceneWriterPermit.release();
		}
	}


	
	private static I_RepresentIdSet uncommittedCNids = new IdentifierSet();

	private static I_RepresentIdSet uncommittedCNidsNoChecks = new IdentifierSet();

	private static I_RepresentIdSet uncommittedDescNids = new IdentifierSet();

	private static ConcurrentHashMap<I_GetConceptData, Collection<AlertToDataConstraintFailure>> dataCheckMap = new ConcurrentHashMap<I_GetConceptData, Collection<AlertToDataConstraintFailure>>();

	private static long lastCommit = Bdb.gVersion.incrementAndGet();

	public static void addUncommittedNoChecks(I_ExtendByRef extension) {
		RefsetMember<?, ?> member = (RefsetMember<?, ?>) extension;
		addUncommittedNoChecks(member.getEnclosingConcept());
	}

	private static List<I_TestDataConstraints> commitTests = new ArrayList<I_TestDataConstraints>();

	private static List<I_TestDataConstraints> creationTests = new ArrayList<I_TestDataConstraints>();

	public static String pluginRoot = "plugins";

	private static ExecutorService dbWriterService;
	private static ExecutorService changeSetWriterService;
	private static ExecutorService luceneWriterService;
	
	private static boolean performCreationTests = true;
	
	private static boolean performCommitTests = true;
    private static boolean writeChangeSets = true;
	
	static {
		changeSetWriterService = Executors.newFixedThreadPool(1, new NamedThreadFactory(commitManagerThreadGroup,
		"Change set writer"));
		dbWriterService = Executors.newCachedThreadPool(new NamedThreadFactory(commitManagerThreadGroup,
		"Db writer"));
		luceneWriterService = Executors.newFixedThreadPool(1, new NamedThreadFactory(commitManagerThreadGroup,
				"Lucene writer"));
		loadTests("commit", commitTests);
		loadTests("precommit", creationTests);
	}

	public static void addUncommittedNoChecks(I_GetConceptData concept) {
		try {
			Concept c = (Concept) concept;
			dbWriterPermit.acquire();
            dbWriterService.execute(new SetNidsForCid(c));
			dbWriterService.execute(new ConceptWriter(c));
			uncommittedCNidsNoChecks.setMember(c.getNid());
			if (Bdb.watchList.containsKey(concept.getNid())) {
				AceLog.getAppLog().info(
						"---@@@ Adding uncommitted NO checks: "
								+ concept.getNid() + " ---@@@ ");
			}
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

	public static void addUncommitted(I_ExtendByRef extension) {
		RefsetMember<?, ?> member = (RefsetMember<?, ?>) extension;
		addUncommitted(member.getEnclosingConcept());
	}

	private static class SetNidsForCid implements Runnable {
	    Concept concept;
        public SetNidsForCid(Concept concept) {
            super();
            this.concept = concept;
        }
        @Override
        public void run() {
            try {
                Collection<Integer> nids = concept.getAllNids();
                NidCNidMapBdb nidCidMap = Bdb.getNidCNidMap();
                for (int nid : nids) {
                    nidCidMap.setCidForNid(concept.getNid(), nid);
                }
            } catch (IOException e) {
               AceLog.getAppLog().alertAndLogException(e);
            }
        }
	    
	}
	public static void addUncommitted(I_GetConceptData igcd) {
		if (igcd == null) {
			return;
		}
		Concept concept = (Concept) igcd;
		if (concept.isUncommitted() == false) {
			dataCheckMap.remove(concept);
			removeUncommitted(concept);
			if (Bdb.watchList.containsKey(concept.getNid())) {
				AceLog.getAppLog().info(
						"--- Removing uncommitted concept: "
								+ concept.getNid() + " --- ");
			}
			return;
		}
		if (Bdb.watchList.containsKey(concept.getNid())) {
			AceLog.getAppLog().info(
					"---@@@ Adding uncommitted concept: " + concept.getNid()
							+ " ---@@@ ");
		}
		try {
		    
		    if (performCreationTests) {
	            List<AlertToDataConstraintFailure> warningsAndErrors = new ArrayList<AlertToDataConstraintFailure>();
	            dataCheckMap.put(concept, warningsAndErrors);
	            for (I_TestDataConstraints test : creationTests) {
	                try {
	                    warningsAndErrors.addAll(test.test(concept, false));
	                    Collection<RefsetMember<?, ?>> extensions = concept.getExtensions();
	                    for (RefsetMember<?, ?> extension : extensions) {
	                        if (extension.isUncommitted()) {
	                            warningsAndErrors.addAll(test.test(extension, false));
	                        }
	                    }
	                } catch (Exception e) {
	                    AceLog.getEditLog().alertAndLogException(e);
	                }
	            }
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

	private static class UpdateFrames implements Runnable {
	    Concept c;
	    
        public UpdateFrames(Concept c) {
            super();
            this.c = c;
        }

        @Override
        public void run() {
            if (getActiveFrame() != null) {
                I_ConfigAceFrame activeFrame = getActiveFrame();
                for (I_ConfigAceFrame frameConfig : activeFrame.getDbConfig()
                        .getAceFrames()) {
                    frameConfig.setCommitEnabled(true);
                    updateAlerts();
                    if (c.isUncommitted()) {
                        frameConfig.addUncommitted(c);
                    } else {
                        frameConfig.removeUncommitted(c);
                    }
                }
            }
        }
	    
	}
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

    public static void commit(ChangeSetPolicy changeSetPolicy,
            ChangeSetWriterThreading changeSetWriterThreading) {
        // TODO add commit tests...
        try {
            synchronized (uncommittedCNids) {
                synchronized (uncommittedCNidsNoChecks) {
                    KindOfComputer.reset();
                    long commitTime = System.currentTimeMillis();
                    IntSet sapNidsFromCommit = Bdb.getSapDb().commit(
                            commitTime);

                    if (writeChangeSets) {
                        if (changeSetPolicy == null) {
                            changeSetPolicy = ChangeSetPolicy.OFF;
                        }
                        if (changeSetWriterThreading == null) {
                            changeSetWriterThreading = ChangeSetWriterThreading.SINGLE_THREAD;
                        }
                        switch (changeSetPolicy) {
                        case COMPREHENSIVE:
                        case INCREMENTAL:
                        case MUTABLE_ONLY:
                            uncommittedCNidsNoChecks.or(uncommittedCNids);
                            ChangeSetWriterHandler handler = new ChangeSetWriterHandler(
                                    uncommittedCNidsNoChecks, commitTime,
                                    sapNidsFromCommit, changeSetPolicy, changeSetWriterThreading);
                            changeSetWriterService.execute(handler);
                            break;
                        case OFF:
                            
                            break;

                        default:
                            throw new RuntimeException("Can't handle policy: " + changeSetPolicy);
                        }
                    }
                    uncommittedCNids.clear();
                    uncommittedCNidsNoChecks = Terms.get().getEmptyIdSet();

                    luceneWriterPermit.acquire();
                    IdentifierSet descNidsToCommit = new IdentifierSet((IdentifierSet) uncommittedDescNids);
                    uncommittedDescNids.clear();
                    luceneWriterService.execute(new LuceneWriter(descNidsToCommit));
                    dataCheckMap.clear();
                }
            }
            Bdb.sync();
        } catch (IOException e1) {
            AceLog.getAppLog().alertAndLogException(e1);
        } catch (InterruptedException e1) {
            AceLog.getAppLog().alertAndLogException(e1);
        } catch (ExecutionException e1) {
            AceLog.getAppLog().alertAndLogException(e1);
        }
        fireCommit();
        updateFrames();
    }

	public static void commit() {
	    commit(ChangeSetPolicy.INCREMENTAL,
            ChangeSetWriterThreading.SINGLE_THREAD);
	}

    private static void fireCommit() {
    	if (DwfaEnv.isHeadless()) {
    		return;
    	}
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                try {
					if (Terms.get().getActiveAceFrameConfig() != null) {
					    for (I_ConfigAceFrame frameConfig: 
					    		Terms.get().getActiveAceFrameConfig().getDbConfig().getAceFrames()) {
					        frameConfig.fireCommit();
					        frameConfig.setCommitEnabled(false);
					        updateAlerts(frameConfig);
					    }
					}
				} catch (TerminologyException e) {
					AceLog.getAppLog().alertAndLogException(e);
				} catch (IOException e) {
					AceLog.getAppLog().alertAndLogException(e);
				}
            }
        });
    }

    private static void fireCancel() {
    	if (DwfaEnv.isHeadless()) {
    		return;
    	}
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                try {
					if (Terms.get().getActiveAceFrameConfig() != null) {
					    for (I_ConfigAceFrame frameConfig: 
					    		Terms.get().getActiveAceFrameConfig().getDbConfig().getAceFrames()) {
					        frameConfig.fireCommit();
					        frameConfig.setCommitEnabled(false);
					        updateAlerts(frameConfig);
					    }
					}
				} catch (TerminologyException e) {
					AceLog.getAppLog().alertAndLogException(e);
				} catch (IOException e) {
					AceLog.getAppLog().alertAndLogException(e);
				}
            }
        });
    }

    public static void updateAlerts(final I_ConfigAceFrame frameConfig) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                doUpdate(frameConfig);
            }
        });
    }

    public static void cancel() {
    	synchronized (uncommittedCNids) {
    		synchronized (uncommittedCNidsNoChecks) {
    			try {
    				KindOfComputer.reset();
    				handleCanceledConcepts(uncommittedCNids);
    				handleCanceledConcepts(uncommittedCNidsNoChecks);
    				uncommittedCNidsNoChecks.clear();
    				uncommittedCNids.clear();
    				Bdb.getSapDb().commit(Long.MIN_VALUE);
    				dataCheckMap.clear();
    			} catch (IOException e1) {
    				AceLog.getAppLog().alertAndLogException(e1);
    			}
    		}
    	}
    	fireCancel();
    	updateFrames();
    }

	private static void handleCanceledConcepts(I_RepresentIdSet uncommittedCNids2)
			throws IOException {
	    I_IterateIds idItr = uncommittedCNids2.iterator();
		while (idItr.next()) {
			Concept c = Concept.get(idItr.nid());
			if (c.isCanceled()) {
				Terms.get().forget(c);
			}
		}
	}

	public static void forget(I_RelVersioned rel) throws IOException {
	    Concept c = Bdb.getConcept(rel.getC1Id());
	    Relationship r = (Relationship) rel;
	    if (r.getTime() != Long.MAX_VALUE) {
	        // Only need to forget additional versions;
	        if (r.revisions == null) {
	            throw new UnsupportedOperationException(
	            "Cannot forget a committed component.");
	        } else {
	            synchronized (r.revisions) {
	                Iterator<RelationshipRevision> ri = r.revisions
	                .iterator();
	                while (ri.hasNext()) {
	                    RelationshipRevision rr = ri.next();
	                    if (rr.getTime() == Long.MAX_VALUE) {
	                        ri.remove();
	                    }
	                }
	            }
	        }
	    } else {
	        // have to forget "all" references to component...
	        c.getSourceRels().remove(rel);
	        c.getData().getSrcRelNids().remove(rel.getNid());
	    }
	    c.modified();
	    Terms.get().addUncommittedNoChecks(c);
	}

	public static void forget(I_DescriptionVersioned desc) throws IOException {
		Description d = (Description) desc;
		Concept c = Bdb.getConcept(d.getConceptId());
		if (d.getTime() != Long.MAX_VALUE) {
		    // Only need to forget additional versions;
		    if (d.revisions == null) {
		        throw new UnsupportedOperationException(
		            "Cannot forget a committed component.");
		    } else {
		        synchronized (d.revisions) {
		            Iterator<DescriptionRevision> di = d.revisions.iterator();
		            while (di.hasNext()) {
		                DescriptionRevision dr = di.next();
		                if (dr.getTime() == Long.MAX_VALUE) {
		                    di.remove();
		                }
		            }
		        }
		    }
		} else {
		    // have to forget "all" references to component...
		    c.getDescriptions().remove(d);
            c.getData().getDescNids().remove(d.getNid());
		}
		c.modified();
		Terms.get().addUncommittedNoChecks(c);
	}

	public static void forget(I_GetConceptData concept) throws IOException {
		Concept c = (Concept) concept;
		Bdb.getConceptDb().forget(c);
	}

    public static void forget(I_ExtendByRef extension) throws IOException {
		RefsetMember<?, ?> m = (RefsetMember<?, ?>) extension;
        Concept c = Bdb.getConcept(m.getRefsetId());
        if (m.getTime() != Long.MAX_VALUE) {
            // Only need to forget additional versions;
            if (m.revisions == null) {
                throw new UnsupportedOperationException(
                    "Cannot forget a committed component.");
            } else {
                synchronized (m.revisions) {
                    Iterator<?> mi =  m.revisions.iterator();
                    while (mi.hasNext()) {
                        Revision<?,?> mr = (Revision<?, ?>) mi.next();
                        if (mr.getTime() == Long.MAX_VALUE) {
                            mi.remove();
                        }
                    }
                }
            }
        } else {
            // have to forget "all" references to component...
            c.getRefsetMembers().remove(m);
            c.getData().getMemberNids().remove(m.getNid());
        }
        c.modified();
        Terms.get().addUncommittedNoChecks(c);
	}

	private static void loadTests(String directory,
			List<I_TestDataConstraints> list) {
		File componentPluginDir = new File(pluginRoot + File.separator
				+ directory);
		File[] plugins = componentPluginDir.listFiles(new FilenameFilter() {
			public boolean accept(File arg0, String fileName) {
				return fileName.toLowerCase().endsWith(".task");
			}

		});

		if (plugins != null) {
			for (File f : plugins) {
				try {
					FileInputStream fis = new FileInputStream(f);
					BufferedInputStream bis = new BufferedInputStream(fis);
					ObjectInputStream ois = new ObjectInputStream(bis);
					I_TestDataConstraints test = (I_TestDataConstraints) ois
							.readObject();
					ois.close();
					list.add(test);
				} catch (Exception e) {
					AceLog.getAppLog().alertAndLog(Level.WARNING,
							"Processing: " + f.getAbsolutePath(), e);
				}
			}
		}
	}

	private static void doUpdate(I_ConfigAceFrame frameConfig) {
		try {
			if (((AceFrameConfig) frameConfig).getAceFrame() != null) {
				ACE aceInstance = ((AceFrameConfig) frameConfig).getAceFrame()
						.getCdePanel();
				aceInstance.getDataCheckListScroller();
				aceInstance.getUncommittedListModel().clear();

				for (Collection<AlertToDataConstraintFailure> alerts : dataCheckMap
						.values()) {
					aceInstance.getUncommittedListModel().addAll(alerts);
				}
				if (aceInstance.getUncommittedListModel().size() > 0) {
					for (int i = 0; i < aceInstance.getLeftTabs().getTabCount(); i++) {
						if (aceInstance.getLeftTabs().getTitleAt(i).equals(
								ACE.DATA_CHECK_TAB_LABEL)) {
							aceInstance.getLeftTabs().setSelectedIndex(i);
							break;
						}
					}
					// show data checks tab...
				} else {
					for (TermComponentDataCheckSelectionListener l : aceInstance
							.getDataCheckListeners()) {
						l.setSelection(null);
					}
					// hide data checks tab...
				}
				if (uncommittedCNids.cardinality() == 0) {
					frameConfig.setCommitEnabled(false);
					frameConfig.fireCommit();
				} else {
					frameConfig.setCommitEnabled(true);
				}
			}
		} catch (Exception e) {
			AceLog.getAppLog().warning(e.toString());
		}
	}

	public static void updateFrames() {
		if (getActiveFrame() != null) {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					doUpdate(getActiveFrame());
				}
			});
		}
	}

	public static void updateAlerts() {
		if (getActiveFrame() != null) {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					doUpdate(getActiveFrame());
				}
			});
		}
	}

	public static void removeUncommitted(final Concept concept) {
		uncommittedCNids.setNotMember(concept.getNid());
		if (uncommittedCNids.cardinality() == 0) {
			dataCheckMap.clear();
		} else {
			dataCheckMap.remove(concept);
		}
		if (getActiveFrame() != null) {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					removeUncommittedUpdateFrame(concept);
				}
			});
		}
	}

	private static void removeUncommittedUpdateFrame(Concept concept) {
		for (I_ConfigAceFrame frameConfig : getActiveFrame().getDbConfig()
				.getAceFrames()) {
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

	public static Set<Concept> getUncommitted() {
	    try {
	        Set<Concept> returnSet = new HashSet<Concept>();
	        I_IterateIds cNidItr = uncommittedCNids.iterator();
	        while (cNidItr.next()) {
	            returnSet.add(Concept.get(cNidItr.nid()));
	        }
	        return returnSet;
	    } catch (IOException e) {
	        throw new RuntimeException(e);
	    }
	}

	public static List<AlertToDataConstraintFailure> getCommitErrorsAndWarnings() {
		List<AlertToDataConstraintFailure> warningsAndErrors = new ArrayList<AlertToDataConstraintFailure>();
        try {
            I_IterateIds cNidItr = uncommittedCNids.iterator();
            while (cNidItr.next()) {
			try {
				Concept toTest = Concept.get(cNidItr.nid());
				for (I_TestDataConstraints test : commitTests) {
					try {
						for (AlertToDataConstraintFailure failure : test.test(
								toTest, true)) {
							warningsAndErrors.add(failure);
						}
					} catch (Exception e) {
						AceLog.getEditLog().alertAndLogException(e);
					}
				}
			} catch (IOException e) {
				AceLog.getAppLog().alertAndLogException(e);
			}
		}
        } catch (IOException e) {
            AceLog.getAppLog().alertAndLogException(e);
        }
		return warningsAndErrors;
	}

	public static void addUncommittedDescNid(int dNid) {
		uncommittedDescNids.setMember(dNid);
	}

	public static long getLastCommit() {
		return lastCommit;
	}

	public static void suspendChangeSetWriters() {
		writeChangeSets  = false;
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

	public static void writeImmediate(Concept concept) {
		new ConceptWriter(concept).run();
	}

    public static boolean isCheckCreationDataEnabled() {
        return performCreationTests;
    }

    public static boolean isCheckCommitDataEnabled() {
        return performCommitTests;
    }

    public static void setCheckCreationDataEnabled(boolean enabled) {
        performCreationTests = enabled;
    }

    public static void setCheckCommitDataEnabled(boolean enabled) {
        performCommitTests = enabled;
    }

}
