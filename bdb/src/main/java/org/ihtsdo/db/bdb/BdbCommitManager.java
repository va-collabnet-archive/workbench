package org.ihtsdo.db.bdb;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;

import javax.swing.SwingUtilities;

import org.dwfa.ace.ACE;
import org.dwfa.ace.TermComponentDataCheckSelectionListener;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_DescriptionVersioned;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_RelVersioned;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.api.ebr.I_ThinExtByRefVersioned;
import org.dwfa.ace.config.AceFrameConfig;
import org.dwfa.ace.log.AceLog;
import org.dwfa.ace.task.commit.AlertToDataConstraintFailure;
import org.dwfa.ace.task.commit.I_TestDataConstraints;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.db.bdb.concept.Concept;
import org.ihtsdo.db.bdb.concept.component.refset.RefsetMember;
import org.ihtsdo.db.util.ConcurrentSet;

public class BdbCommitManager {
		
	private static class ConceptWriter implements Callable<Boolean> {
		private Concept c;
		
		public ConceptWriter(Concept c) {
			super();
			this.c = c;
		}

		@Override
		public Boolean call() throws Exception {
			Bdb.getConceptDb().writeConcept(c);
			return true;
		}
	}

	private static class FutureGetter extends Thread {
		public FutureGetter() {
			super("FutureGetter");
		}

		@Override
		public void run() {
			try {
				Future<Boolean> f = writeConceptFutures.take();
				f.get();
			} catch (InterruptedException e) {
				AceLog.getAppLog().alertAndLogException(e);
			} catch (ExecutionException e) {
				AceLog.getAppLog().alertAndLogException(e);
			}
		}		
	}
	
	private static class UnwrittenConceptsManager extends Thread {
		ConcurrentSet<Concept> previous = null;
		private boolean sleeping = false;
		private int sleepTime = 1000 * 60 * 1;
		
		public UnwrittenConceptsManager() {
			super("UnwrittenConceptsManager");
		}
		
	    private void sleep() {
	        this.sleeping  = true;
	        try {
	            Thread.sleep(sleepTime);
	        } catch (InterruptedException e) {

	        }
	        this.sleeping = false;
	    }

	    public void awaken() {
	        if (this.sleeping) {
	            unwrittenConceptsThread.interrupt();
	        }
	    }


		@Override
		public void run() {
			while (unwrittenConcepts.size() > 10) {
				ConcurrentSet<Concept> replacement = new ConcurrentSet<Concept>(20);
				unwrittenConcepts.setReplacement(replacement);
				if (previous != null) {
					for (Concept c: previous) {
						Future<Boolean> f = Bdb.getExecutorPool().submit(new ConceptWriter(c));
						try {
							writeConceptFutures.put(f);
						} catch (InterruptedException e) {
							throw new RuntimeException("Should never happen");
						}
					}
				}
				ArrayList<Concept> writtenConcepts = new ArrayList<Concept>();
				for (Concept c: unwrittenConcepts) {
					Future<Boolean> f = Bdb.getExecutorPool().submit(new ConceptWriter(c));
					writtenConcepts.add(c);
					try {
						writeConceptFutures.put(f);
					} catch (InterruptedException e) {
						throw new RuntimeException("Should never happen");
					}
				}
				unwrittenConcepts.removeAll(writtenConcepts);
				unwrittenConcepts = replacement;
			}
			sleep();
		}		
	}

	private static ConcurrentSet<Integer> uncommittedCNids = new ConcurrentSet<Integer>(20);
	private static ConcurrentSet<Concept> unwrittenConcepts = new ConcurrentSet<Concept>(20);
	private static ArrayBlockingQueue<Future<Boolean>> writeConceptFutures = new ArrayBlockingQueue<Future<Boolean>>(100);
	
	private static Map<I_GetConceptData, Collection<AlertToDataConstraintFailure>> dataCheckMap = new HashMap<I_GetConceptData, Collection<AlertToDataConstraintFailure>>();

	public static void addUncommittedNoChecks(I_ThinExtByRefVersioned extension) {
		RefsetMember<?, ?> member = (RefsetMember<?, ?>) extension;
		addUncommittedNoChecks(member.enclosingConcept);
	}

	private static List<I_TestDataConstraints> commitTests = 
		new ArrayList<I_TestDataConstraints>();

	private static List<I_TestDataConstraints> creationTests = 
		new ArrayList<I_TestDataConstraints>();

	public static String pluginRoot = "plugins";

	private static FutureGetter futureGetterThread = new FutureGetter();
	private static UnwrittenConceptsManager unwrittenConceptsThread = new UnwrittenConceptsManager();
	static {
		futureGetterThread.start();
		unwrittenConceptsThread.start();
		loadTests("commit", commitTests);
		loadTests("precommit", creationTests);
	}

	public static void addUncommittedNoChecks(I_GetConceptData concept) {
		Concept old = unwrittenConcepts.replace((Concept) concept);
		if (old != null) {
			Future<Boolean> f = Bdb.getExecutorPool().submit(new ConceptWriter(old));
			try {
				writeConceptFutures.put(f);
			} catch (InterruptedException e) {
				throw new RuntimeException("Should never happen");
			}
		}
		unwrittenConceptsThread.awaken();
	}

	public static void addUncommitted(I_ThinExtByRefVersioned extension) {
		RefsetMember<?, ?> member = (RefsetMember<?, ?>) extension;
		addUncommitted(member.enclosingConcept);
	}

	public static void addUncommitted(I_GetConceptData igcd) {
		if (igcd == null) {
			return;
		}
		Concept concept = (Concept) igcd;
		if (concept.isUncommitted() == false) {
			dataCheckMap.remove(concept);
			removeUncommitted(concept);
			return;
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
		unwrittenConcepts.add(concept);
		unwrittenConceptsThread.awaken();

		Concept old = unwrittenConcepts.replace((Concept) concept);
		if (old != null) {
			Future<Boolean> f = Bdb.getExecutorPool().submit(new ConceptWriter(old));
			try {
				writeConceptFutures.put(f);
			} catch (InterruptedException e) {
				throw new RuntimeException("Should never happen");
			}
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
		//TODO add commit tests...
		long commitTime = System.currentTimeMillis();
		try {
			Bdb.getSapDb().commit(commitTime);
			synchronized (unwrittenConcepts) {
				uncommittedCNids.clear();
				for (Concept c: unwrittenConcepts) {
					Future<Boolean> f = Bdb.getExecutorPool().submit(new ConceptWriter(c));
					try {
						writeConceptFutures.put(f);
					} catch (InterruptedException e) {
						throw new RuntimeException("Should never happen");
					}
				}
				unwrittenConcepts.clear();
				uncommittedCNids.setCapacity(10);
			}
		} catch (IOException e1) {
			AceLog.getAppLog().alertAndLogException(e1);
		}
		updateFrames();
	}

	public static void cancel() {
		// TODO Auto-generated method stub
		uncommittedCNids.clear();
		uncommittedCNids.setCapacity(10);
		try {
			Bdb.getSapDb().commit(Long.MIN_VALUE);
		} catch (IOException e1) {
			AceLog.getAppLog().alertAndLogException(e1);
		}
		updateFrames();
	}

	public static void forget(I_RelVersioned rel) {
		// TODO Auto-generated method stub

	}

	public static void forget(I_DescriptionVersioned desc) {
		// TODO Auto-generated method stub

	}

	public static void forget(I_GetConceptData concept) {
		// TODO Auto-generated method stub

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
		unwrittenConcepts.remove(concept);
		if (uncommittedCNids.size() == 0) {
			dataCheckMap.clear();
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
		for (I_ConfigAceFrame frameConfig : 
				getActiveFrame().getDbConfig().getAceFrames()) {
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

}
