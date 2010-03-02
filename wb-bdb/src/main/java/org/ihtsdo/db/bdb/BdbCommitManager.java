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
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;

import javax.swing.SwingUtilities;

import org.dwfa.ace.ACE;
import org.dwfa.ace.TermComponentDataCheckSelectionListener;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_DescriptionVersioned;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_RelVersioned;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.api.ebr.I_ExtendByRef;
import org.dwfa.ace.config.AceFrameConfig;
import org.dwfa.ace.log.AceLog;
import org.dwfa.ace.task.commit.AlertToDataConstraintFailure;
import org.dwfa.ace.task.commit.I_TestDataConstraints;
import org.dwfa.app.DwfaEnv;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.vodb.types.IntSet;
import org.ihtsdo.concept.Concept;
import org.ihtsdo.concept.component.description.Description;
import org.ihtsdo.concept.component.refset.RefsetMember;
import org.ihtsdo.concept.component.relationship.Relationship;
import org.ihtsdo.concept.component.relationship.RelationshipRevision;
import org.ihtsdo.cs.ChangeSetWriterHandler;
import org.ihtsdo.db.bdb.computer.kindof.KindOfComputer;
import org.ihtsdo.db.bdb.id.NidCNidMapBdb;
import org.ihtsdo.db.util.ConcurrentSet;
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
		private ConcurrentSet<Integer> uncommittedDescNids;

		public LuceneWriter(ConcurrentSet<Integer> uncommittedDescNids) {
			super();
			this.uncommittedDescNids = uncommittedDescNids;
		}

		@Override
		public void run() {
			try {
				ArrayList<Description> toIndex = new ArrayList<Description>(
						uncommittedDescNids.size());
				for (int dNid : uncommittedDescNids) {
					Description d = (Description) Bdb.getComponent(dNid);
					toIndex.add(d);
				}
				LuceneManager.writeToLucene(toIndex);
			} catch (Exception e) {
				AceLog.getAppLog().alertAndLogException(e);
			}
			luceneWriterPermit.release();
		}
	}


	
	private static ConcurrentSet<Integer> uncommittedCNids = new ConcurrentSet<Integer>(
			20);

	private static ConcurrentSet<Integer> uncommittedCNidsNoChecks = new ConcurrentSet<Integer>(
			20);

	private static AtomicReference<ConcurrentSet<Integer>> uncommittedDescNids = new AtomicReference<ConcurrentSet<Integer>>(
			new ConcurrentSet<Integer>(20));

	private static ConcurrentHashMap<I_GetConceptData, Collection<AlertToDataConstraintFailure>> dataCheckMap = new ConcurrentHashMap<I_GetConceptData, Collection<AlertToDataConstraintFailure>>();

	private static long lastCommit = Bdb.gVersion.incrementAndGet();

	public static void addUncommittedNoChecks(I_ExtendByRef extension) {
		RefsetMember<?, ?> member = (RefsetMember<?, ?>) extension;
		addUncommittedNoChecks(member.getEnclosingConcept());
	}

	private static List<I_TestDataConstraints> commitTests = new ArrayList<I_TestDataConstraints>();

	private static List<I_TestDataConstraints> creationTests = new ArrayList<I_TestDataConstraints>();

	public static String pluginRoot = "plugins";

	private static boolean writeChangeSets = true;

	private static ExecutorService dbWriterService;
	private static ExecutorService changeSetWriterService;
	private static ExecutorService luceneWriterService;
	
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
			dbWriterService.execute(new ConceptWriter(c));
			uncommittedCNidsNoChecks.add(c.getNid());
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
						"---ииии Removing uncommitted concept: "
								+ concept.getNid() + " ---ииии ");
			}
			return;
		}
		if (Bdb.watchList.containsKey(concept.getNid())) {
			AceLog.getAppLog().info(
					"---@@@ Adding uncommitted concept: " + concept.getNid()
							+ " ---@@@ ");
		}
		try {
			Collection<Integer> nids = concept.getAllNids();
			NidCNidMapBdb nidCidMap = Bdb.getNidCNidMap();
			for (int nid : nids) {
				nidCidMap.setCidForNid(igcd.getNid(), nid);
			}
			List<AlertToDataConstraintFailure> warningsAndErrors = new ArrayList<AlertToDataConstraintFailure>();
			dataCheckMap.put(concept, warningsAndErrors);
			for (I_TestDataConstraints test : creationTests) {
				try {
					warningsAndErrors.addAll(test.test(concept, false));
				} catch (Exception e) {
					AceLog.getEditLog().alertAndLogException(e);
				}
			}
			uncommittedCNids.add(concept.getNid());
			dbWriterPermit.acquire();
			dbWriterService.execute(new ConceptWriter(concept));
		} catch (IOException e) {
			throw new RuntimeException(e);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}

		if (getActiveFrame() != null) {
			I_ConfigAceFrame activeFrame = getActiveFrame();
			for (I_ConfigAceFrame frameConfig : activeFrame.getDbConfig()
					.getAceFrames()) {
				frameConfig.setCommitEnabled(true);
				updateAlerts();
				if (concept.isUncommitted()) {
					frameConfig.addUncommitted(concept);
				} else {
					frameConfig.removeUncommitted(concept);
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

	public static void commit() {
		// TODO add commit tests...
		KindOfComputer.reset();
		long commitTime = System.currentTimeMillis();
		try {
			synchronized (uncommittedCNids) {
				synchronized (uncommittedCNidsNoChecks) {
					IntSet sapNidsFromCommit = Bdb.getSapDb().commit(
							commitTime);

					if (writeChangeSets) {
						uncommittedCNidsNoChecks.addAll(uncommittedCNids);
						ChangeSetWriterHandler handler = new ChangeSetWriterHandler(
								uncommittedCNidsNoChecks, commitTime,
								sapNidsFromCommit);
						changeSetWriterService.execute(handler);
					}
					uncommittedCNids.clear();
					uncommittedCNidsNoChecks.clear();

					ConcurrentSet<Integer> descNidsToCommit = uncommittedDescNids
					.getAndSet(new ConcurrentSet<Integer>(20));

					luceneWriterPermit.acquire();
					luceneWriterService.execute(new LuceneWriter(descNidsToCommit));
					uncommittedCNids.setCapacity(10);
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
    				uncommittedCNidsNoChecks.setCapacity(10);
    				uncommittedCNids.clear();
    				uncommittedCNids.setCapacity(10);
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

	private static void handleCanceledConcepts(Set<Integer> cNids)
			throws IOException {
		for (int cNid : cNids) {
			Concept c = Concept.get(cNid);
			if (c.isCanceled()) {
				Terms.get().forget(c);
			}
		}
	}

	public static void forget(I_RelVersioned rel) throws IOException {
		Concept c = Bdb.getConcept(rel.getC1Id());
		synchronized (c) {
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
			}
		}
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	public static void forget(I_DescriptionVersioned desc) {
		Description d = (Description) desc;
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	public static void forget(I_GetConceptData concept) throws IOException {
		Concept c = (Concept) concept;
		Bdb.getConceptDb().forget(c);
	}

	public static void forget(I_ExtendByRef extension) {
		RefsetMember<?, ?> m = (RefsetMember<?, ?>) extension;
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
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
				if (uncommittedCNids.size() == 0) {
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
		uncommittedCNids.remove(concept.getNid());
		if (uncommittedCNids.size() == 0) {
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
				if (uncommittedCNids.size() == 0) {
					frameConfig.setCommitEnabled(false);
				}
			} catch (Exception e) {
				AceLog.getAppLog().warning(e.toString());
			}
		}
	}

	public static Set<Concept> getUncommitted() {
		Set<Concept> returnSet = new HashSet<Concept>();
		Iterator<Integer> cNidItr = uncommittedCNids.iterator();
		while (cNidItr.hasNext()) {
			try {
				returnSet.add(Concept.get(cNidItr.next()));
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		return returnSet;
	}

	public static List<AlertToDataConstraintFailure> getCommitErrorsAndWarnings() {
		List<AlertToDataConstraintFailure> warningsAndErrors = new ArrayList<AlertToDataConstraintFailure>();
		for (int cNid : uncommittedCNids) {
			try {
				Concept toTest = Concept.get(cNid);
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
		return warningsAndErrors;
	}

	public static void addUncommittedDescNid(int dNid) {
		uncommittedDescNids.get().add(dNid);
	}

	public static long getLastCommit() {
		return lastCommit;
	}

	public static void suspendChangeSetWriters() {
		writeChangeSets = false;
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
}
