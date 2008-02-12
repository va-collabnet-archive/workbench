package org.dwfa.vodb;

import java.awt.AWTEvent;
import java.awt.Toolkit;
import java.awt.event.AWTEventListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.dwfa.ace.ACE;
import org.dwfa.ace.activity.ActivityPanel;
import org.dwfa.ace.activity.ActivityViewer;
import org.dwfa.ace.api.I_ConceptAttributePart;
import org.dwfa.ace.api.I_ConceptAttributeTuple;
import org.dwfa.ace.api.I_ConceptAttributeVersioned;
import org.dwfa.ace.api.I_ConfigAceDb;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_DescriptionPart;
import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_DescriptionVersioned;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IdPart;
import org.dwfa.ace.api.I_IdVersioned;
import org.dwfa.ace.api.I_ImageVersioned;
import org.dwfa.ace.api.I_ImplementTermFactory;
import org.dwfa.ace.api.I_IntList;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_Path;
import org.dwfa.ace.api.I_Position;
import org.dwfa.ace.api.I_ProcessConceptAttributes;
import org.dwfa.ace.api.I_ProcessConcepts;
import org.dwfa.ace.api.I_ProcessDescriptions;
import org.dwfa.ace.api.I_ProcessExtByRef;
import org.dwfa.ace.api.I_ProcessIds;
import org.dwfa.ace.api.I_ProcessImages;
import org.dwfa.ace.api.I_ProcessPaths;
import org.dwfa.ace.api.I_ProcessRelationships;
import org.dwfa.ace.api.I_RelPart;
import org.dwfa.ace.api.I_RelTuple;
import org.dwfa.ace.api.I_RelVersioned;
import org.dwfa.ace.api.I_Transact;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.api.TimePathId;
import org.dwfa.ace.api.cs.I_ReadChangeSet;
import org.dwfa.ace.api.cs.I_WriteChangeSet;
import org.dwfa.ace.api.ebr.I_GetExtensionData;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPart;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartBoolean;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartConcept;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartConceptInt;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartInteger;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartLanguage;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartLanguageScoped;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartMeasurement;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartString;
import org.dwfa.ace.api.ebr.I_ThinExtByRefVersioned;
import org.dwfa.ace.config.AceConfig;
import org.dwfa.ace.config.AceFrameConfig;
import org.dwfa.ace.cs.BinaryChangeSetReader;
import org.dwfa.ace.cs.BinaryChangeSetWriter;
import org.dwfa.ace.log.AceLog;
import org.dwfa.ace.search.I_TrackContinuation;
import org.dwfa.ace.search.LuceneMatch;
import org.dwfa.ace.search.SearchStringWorker.LuceneProgressUpdator;
import org.dwfa.ace.task.search.I_TestSearchResults;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.util.Stopwatch;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.cement.PrimordialId;
import org.dwfa.tapi.I_ConceptualizeLocally;
import org.dwfa.tapi.NoMappingException;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.tapi.impl.LocalFixedTerminology;
import org.dwfa.util.LogWithAlerts;
import org.dwfa.vodb.bind.BranchTimeBinder;
import org.dwfa.vodb.bind.PathBinder;
import org.dwfa.vodb.bind.ThinConVersionedBinding;
import org.dwfa.vodb.bind.ThinDescVersionedBinding;
import org.dwfa.vodb.bind.ThinExtBinder;
import org.dwfa.vodb.bind.ThinExtSecondaryKeyCreator;
import org.dwfa.vodb.bind.ThinIdVersionedBinding;
import org.dwfa.vodb.bind.ThinImageBinder;
import org.dwfa.vodb.bind.ThinRelVersionedBinding;
import org.dwfa.vodb.bind.ThinVersionHelper;
import org.dwfa.vodb.bind.TimePathIdBinder;
import org.dwfa.vodb.bind.UuidBinding;
import org.dwfa.vodb.jar.PathCollector;
import org.dwfa.vodb.jar.TimePathCollector;
import org.dwfa.vodb.types.ConceptBean;
import org.dwfa.vodb.types.ExtensionByReferenceBean;
import org.dwfa.vodb.types.I_ProcessConceptAttributeEntries;
import org.dwfa.vodb.types.I_ProcessDescriptionEntries;
import org.dwfa.vodb.types.I_ProcessExtByRefEntries;
import org.dwfa.vodb.types.I_ProcessIdEntries;
import org.dwfa.vodb.types.I_ProcessImageEntries;
import org.dwfa.vodb.types.I_ProcessPathEntries;
import org.dwfa.vodb.types.I_ProcessRelationshipEntries;
import org.dwfa.vodb.types.I_ProcessTimeBranchEntries;
import org.dwfa.vodb.types.IntList;
import org.dwfa.vodb.types.IntSet;
import org.dwfa.vodb.types.Path;
import org.dwfa.vodb.types.Position;
import org.dwfa.vodb.types.ThinConPart;
import org.dwfa.vodb.types.ThinConVersioned;
import org.dwfa.vodb.types.ThinDescPart;
import org.dwfa.vodb.types.ThinDescVersioned;
import org.dwfa.vodb.types.ThinExtByRefPartBoolean;
import org.dwfa.vodb.types.ThinExtByRefPartConcept;
import org.dwfa.vodb.types.ThinExtByRefPartConceptInt;
import org.dwfa.vodb.types.ThinExtByRefPartInteger;
import org.dwfa.vodb.types.ThinExtByRefPartLanguage;
import org.dwfa.vodb.types.ThinExtByRefPartLanguageScoped;
import org.dwfa.vodb.types.ThinExtByRefPartMeasurement;
import org.dwfa.vodb.types.ThinExtByRefPartString;
import org.dwfa.vodb.types.ThinExtByRefVersioned;
import org.dwfa.vodb.types.ThinIdPart;
import org.dwfa.vodb.types.ThinIdVersioned;
import org.dwfa.vodb.types.ThinImageVersioned;
import org.dwfa.vodb.types.ThinRelPart;
import org.dwfa.vodb.types.ThinRelVersioned;

import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.je.Cursor;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.je.JEVersion;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationStatus;
import com.sleepycat.je.PreloadConfig;
import com.sleepycat.je.SecondaryConfig;
import com.sleepycat.je.SecondaryCursor;
import com.sleepycat.je.SecondaryDatabase;

/**
 * @author kec
 * 
 */
public class VodbEnv implements I_ImplementTermFactory {
	private static Logger logger = Logger.getLogger(VodbEnv.class.getName());

	private Environment env;

	private Database conceptDb;

	private Database relDb;

	private Database descDb;

	private Database idDb;

	private Database imageDb;

	private Database pathDb;

	private Database extensionDb; // extensions

	private Database metaInfoDb; // change set info, version info...

	private SecondaryDatabase conceptDescMap;

	private SecondaryDatabase c1RelMap;

	private SecondaryDatabase c2RelMap;

	private SecondaryDatabase uidToIdMap;

	private SecondaryDatabase componentToExtMap;

	private SecondaryDatabase refsetToExtMap;

	private boolean readOnly;

	static ThinDescVersionedBinding descBinding = new ThinDescVersionedBinding();

	static ThinRelVersionedBinding relBinding = new ThinRelVersionedBinding();

	static ThinConVersionedBinding conBinding = new ThinConVersionedBinding();

	static PathBinder pathBinder = new PathBinder();

	TupleBinding intBinder = TupleBinding.getPrimitiveBinding(Integer.class);

	TupleBinding stringBinder = TupleBinding.getPrimitiveBinding(String.class);

	ConceptIdKeyForDescCreator descForConceptKeyCreator = new ConceptIdKeyForDescCreator(
			descBinding);

	C2KeyForRelCreator c2KeyCreator = new C2KeyForRelCreator(relBinding);

	C1KeyForRelCreator c1KeyCreator = new C1KeyForRelCreator(relBinding);

	static ThinIdVersionedBinding idBinding = new ThinIdVersionedBinding();

	BranchTimeBinder btBinder = new BranchTimeBinder();

	TimePathIdBinder tbBinder = new TimePathIdBinder();

	static ThinExtBinder extBinder = new ThinExtBinder();

	static ThinImageBinder imageBinder = new ThinImageBinder();

	UuidBinding uuidBinding = new UuidBinding();

	private int descCount = -1;

	private int databaseVersion = 1;

	private Database timeBranchDb;

	private SecondaryDatabase conceptImageMap;

	private File luceneDir;

	public VodbEnv() {
		LocalVersionedTerminology.set(this);
	}

	public VodbEnv(boolean stealth) {

	}

	private class StartupListener implements AWTEventListener {

		public void eventDispatched(AWTEvent event) {
			KeyEvent ke = (KeyEvent) event;
			if (ke.getKeyCode() == KeyEvent.VK_ESCAPE) {
				preloadRels = false;
				preloadDescriptions = !preloadDescriptions;
				activity
						.setProgressInfoUpper("Loading the terminology (preload descriptions = "
								+ preloadDescriptions + ")");
				Toolkit.getDefaultToolkit().removeAWTEventListener(this);
			}

		}

	}

	private static boolean preloadRels = false;

	private static boolean preloadDescriptions = false;

	private static boolean transactional = true;

	private static boolean deferredWrite = false;

	private static boolean txnNoSync = true;

	private static long transactionTimeout = 30000; // 30 seconds

	private ActivityPanel activity;

	private File licitWordsDir;

	private File envHome;

	private static Long cacheSize;

	public void setup(Object envHome, boolean readOnly, Long cacheSize)
			throws IOException {
		setup((File) envHome, readOnly, cacheSize);
	}

	/**
	 * @todo find out of all secondary databases have to be opened when the
	 *       primary is opened? How do they get updated, etc?
	 */
	public void setup(File envHome, boolean readOnly, Long cacheSize)
			throws IOException {
		try {
			long startTime = System.currentTimeMillis();
			this.envHome = envHome;
			if (VodbEnv.cacheSize == null) {
				VodbEnv.cacheSize = cacheSize;
			} else {
				cacheSize = VodbEnv.cacheSize;
			}
			activity = new ActivityPanel(true, true);
			StartupListener l = new StartupListener();
			Toolkit.getDefaultToolkit().addAWTEventListener(l,
					AWTEvent.KEY_EVENT_MASK);

			AceLog.getAppLog().info("Setting up db: " + envHome);
			activity.setIndeterminate(true);
			activity.setProgressInfoUpper("Loading the terminology");
			activity.setProgressInfoLower("Setting up the environment...");
			activity.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					System.out
							.println("System.exit from activity action listener: "
									+ e.getActionCommand());
					System.exit(0);
				}
			});
			try {
				ActivityViewer.addActivity(activity);
			} catch (Exception e1) {
				AceLog.getAppLog().alertAndLogException(e1);
			}

			this.readOnly = readOnly;
			if (this != LocalVersionedTerminology.getStealthfactory()) {
				LocalFixedTerminology.setStore(new VodbFixedServer(this));
			}
			envHome.mkdirs();
			luceneDir = new File(envHome, "lucene");
			licitWordsDir = new File(envHome, "lucene-licit");

			AceLog.getAppLog().info(
					"Setup transactional: " + transactional + " txnNoSync: "
							+ txnNoSync + " deferredWrite: " + deferredWrite
							+ " transactionTimeout: " + transactionTimeout);

			EnvironmentConfig envConfig = new EnvironmentConfig();
			envConfig.setTransactional(transactional);
			transactional = envConfig.getTransactional();
			
			envConfig.setTxnNoSync(txnNoSync);
			txnNoSync = envConfig.getTxnNoSync();
			
			envConfig.setTxnTimeout(transactionTimeout);
			transactionTimeout = envConfig.getTxnTimeout();

			if (VodbEnv.cacheSize != null) {
				envConfig.setCacheSize(VodbEnv.cacheSize);
				AceLog.getAppLog().info(
						"Setting cache size to: " + VodbEnv.cacheSize);
				AceLog.getAppLog().info(
						"Cache size is: " + envConfig.getCacheSize());
				AceLog.getAppLog().info(
						"Cache percent: " + envConfig.getCachePercent());

				activity.setProgressInfoLower("Setting cache size to: "
						+ VodbEnv.cacheSize);
			}

			envConfig.setReadOnly(readOnly);
			envConfig.setAllowCreate(!readOnly);
			env = new Environment(envHome, envConfig);
			AceLog.getAppLog().info(
					"Berkeley DB info: "
							+ JEVersion.CURRENT_VERSION.getVersionString());

			DatabaseConfig conceptDbConfig = makeConfig(readOnly, transactional);
			activity.setProgressInfoLower("Opening concepts...");
			conceptDb = env.openDatabase(null, "concept", conceptDbConfig);

			DatabaseConfig relDbConfig = makeConfig(readOnly, transactional);
			activity.setProgressInfoLower("Opening relationships...");
			relDb = env.openDatabase(null, "rel", relDbConfig);
			getC1RelMap();
			getC2RelMap();

			if (preloadRels) {
				activity.setProgressInfoLower("Loading relationships...");
				PreloadConfig relPreloadConfig = new PreloadConfig();
				relPreloadConfig.setLoadLNs(true);
				relDb.preload(relPreloadConfig);
			}
			DatabaseConfig descDbConfig = makeConfig(readOnly, transactional);
			descDb = env.openDatabase(null, "desc", descDbConfig);
			activity.setProgressInfoLower("Opening descriptions...");
			if (preloadDescriptions) {
				activity.setProgressInfoLower("Loading descriptions...");
				PreloadConfig descPreloadConfig = new PreloadConfig();
				descPreloadConfig.setLoadLNs(true);
				descDb.preload(descPreloadConfig);
			}
			getConceptDescMap();

			DatabaseConfig mapDbConfig = makeConfig(readOnly, transactional);
			activity.setProgressInfoLower("Opening ids...");
			idDb = env.openDatabase(null, "idDb", mapDbConfig);
			createUidToIdMap();

			// Reset the authority id so that each time the db starts, it gets a
			// new
			// authorityId.
			PrimordialId primId = PrimordialId.AUTHORITY_ID;
			I_IdVersioned thinId = new ThinIdVersioned(primId
					.getNativeId(Integer.MIN_VALUE), 1);
			ThinIdPart idPart = new ThinIdPart();
			idPart.setIdStatus(PrimordialId.CURRENT_ID
					.getNativeId(Integer.MIN_VALUE));
			idPart.setPathId(PrimordialId.ACE_AUXILIARY_ID
					.getNativeId(Integer.MIN_VALUE));
			idPart.setSource(PrimordialId.ACE_AUX_ENCODING_ID
					.getNativeId(Integer.MIN_VALUE));
			idPart.setSourceId(UUID.randomUUID());
			idPart.setVersion(Integer.MIN_VALUE);
			thinId.addVersion(idPart);
			writeId(thinId);

			DatabaseConfig imageDbConfig = makeConfig(readOnly, transactional);
			activity.setProgressInfoLower("Opening images...");
			imageDb = env.openDatabase(null, "imageDb", imageDbConfig);
			getConceptImageMap();

			DatabaseConfig timeBranchDbConfig = makeConfig(readOnly,
					transactional);
			activity.setProgressInfoLower("Opening time branches...");
			timeBranchDb = env.openDatabase(null, "timeBranchDb",
					timeBranchDbConfig);

			DatabaseConfig pathDbConfig = makeConfig(readOnly, transactional);
			activity.setProgressInfoLower("Opening paths...");
			pathDb = env.openDatabase(null, "pathDb", pathDbConfig);

			DatabaseConfig extensionDbConfig = makeConfig(readOnly,
					transactional);
			activity.setProgressInfoLower("Opening extensions...");
			extensionDb = env.openDatabase(null, "extensionDb",
					extensionDbConfig);
			getRefsetToExtMap();
			getComponentToExtMap();

			DatabaseConfig metaInfoDbConfig = makeConfig(readOnly,
					transactional);
			activity.setProgressInfoLower("Opening metaInfo...");
			metaInfoDb = env.openDatabase(null, "metaInfoDb", metaInfoDbConfig);

			String versionString = getProperty("dbVersion");
			if (versionString == null) {
				versionString = "1";
				setProperty("dbVersion", "1");
				logger.info("Setting db version to 1.");
				sync();
				versionString = getProperty("dbVersion");
			}
			databaseVersion = Integer.parseInt(versionString);
			logger.info(" db version is: " + databaseVersion);

			AceLog.getAppLog().info(
					"Cache percent: " + envConfig.getCachePercent());
			AceLog.getAppLog().info("Cache size: " + envConfig.getCacheSize());
			AceLog.getAppLog().info("preloadRels: " + preloadRels);
			AceLog.getAppLog().info(
					"preloadDescriptions: " + preloadDescriptions);
			AceLog.getAppLog().info(
					"je.maxMemory: "
							+ env.getConfig().getConfigParam("je.maxMemory"));

			activity.setProgressInfoLower("complete");
			activity.complete();
			Toolkit.getDefaultToolkit().removeAWTEventListener(l);
			long loadTime = System.currentTimeMillis() - startTime;
			logger.info("### Load time: " + loadTime + " ms");
		} catch (DatabaseException e) {
			throw new ToIoException(e);
		}
	}

	ThinExtSecondaryKeyCreator refsetKeyCreator = new ThinExtSecondaryKeyCreator(
			ThinExtSecondaryKeyCreator.KEY_TYPE.REFSET_ID);

	ThinExtSecondaryKeyCreator componentKeyCreator = new ThinExtSecondaryKeyCreator(
			ThinExtSecondaryKeyCreator.KEY_TYPE.COMPONENT_ID);

	private void createRefsetToExtMap() throws DatabaseException {
		if (refsetToExtMap == null) {

			SecondaryConfig extByRefsetIdConfig = new SecondaryConfig();
			extByRefsetIdConfig.setReadOnly(readOnly);
			extByRefsetIdConfig.setDeferredWrite(deferredWrite);
			extByRefsetIdConfig.setAllowCreate(!readOnly);
			extByRefsetIdConfig.setSortedDuplicates(false);
			extByRefsetIdConfig.setKeyCreator(refsetKeyCreator);
			extByRefsetIdConfig.setAllowPopulate(true);
			extByRefsetIdConfig.setTransactional(transactional);

			refsetToExtMap = env.openSecondaryDatabase(null, "refsetToExtMap",
					extensionDb, extByRefsetIdConfig);
		}
	}

	private void createComponentToExtMap() throws DatabaseException {
		if (componentToExtMap == null) {

			SecondaryConfig extByComponentIdConfig = new SecondaryConfig();
			extByComponentIdConfig.setReadOnly(readOnly);
			extByComponentIdConfig.setDeferredWrite(deferredWrite);
			extByComponentIdConfig.setAllowCreate(!readOnly);
			extByComponentIdConfig.setSortedDuplicates(false);
			extByComponentIdConfig.setKeyCreator(componentKeyCreator);
			extByComponentIdConfig.setAllowPopulate(true);
			extByComponentIdConfig.setTransactional(transactional);

			componentToExtMap = env.openSecondaryDatabase(null,
					"componentToExtMap", extensionDb, extByComponentIdConfig);
		}
	}

	private void createConceptDescMap() throws DatabaseException {
		if (conceptDescMap == null) {
			ConceptIdKeyForDescCreator descConceptKeyCreator = new ConceptIdKeyForDescCreator(
					descBinding);

			SecondaryConfig descByConceptIdConfig = new SecondaryConfig();
			descByConceptIdConfig.setReadOnly(readOnly);
			descByConceptIdConfig.setDeferredWrite(deferredWrite);
			descByConceptIdConfig.setAllowCreate(!readOnly);
			descByConceptIdConfig.setSortedDuplicates(false);
			descByConceptIdConfig.setKeyCreator(descConceptKeyCreator);
			descByConceptIdConfig.setAllowPopulate(true);
			descByConceptIdConfig.setTransactional(transactional);

			conceptDescMap = env.openSecondaryDatabase(null, "conceptDescMap",
					descDb, descByConceptIdConfig);
		}
	}

	public void createC1RelMap() throws DatabaseException {
		if (c1RelMap == null) {
			C1KeyForRelCreator c1ToRelKeyCreator = new C1KeyForRelCreator(
					relBinding);

			SecondaryConfig relByC1IdConfig = new SecondaryConfig();
			relByC1IdConfig.setReadOnly(readOnly);
			relByC1IdConfig.setDeferredWrite(deferredWrite);
			relByC1IdConfig.setAllowCreate(!readOnly);
			relByC1IdConfig.setSortedDuplicates(false);
			relByC1IdConfig.setKeyCreator(c1ToRelKeyCreator);
			relByC1IdConfig.setAllowPopulate(true);
			relByC1IdConfig.setTransactional(transactional);

			c1RelMap = env.openSecondaryDatabase(null, "c1RelMap", relDb,
					relByC1IdConfig);
		}
	}

	public void createConceptImageMap() throws DatabaseException {
		if (conceptImageMap == null) {
			ConceptKeyForImageCreator concToImageKeyCreator = new ConceptKeyForImageCreator();

			SecondaryConfig imageByConConfig = new SecondaryConfig();
			imageByConConfig.setReadOnly(readOnly);
			imageByConConfig.setDeferredWrite(deferredWrite);
			imageByConConfig.setAllowCreate(!readOnly);
			imageByConConfig.setSortedDuplicates(true);
			imageByConConfig.setKeyCreator(concToImageKeyCreator);
			imageByConConfig.setAllowPopulate(true);

			imageByConConfig.setTransactional(transactional);
			conceptImageMap = env.openSecondaryDatabase(null,
					"conceptImageMap", imageDb, imageByConConfig);
		}
	}

	public void createC2RelMap() throws DatabaseException {
		if (c2RelMap == null) {
			C2KeyForRelCreator c2ToRelKeyCreator = new C2KeyForRelCreator(
					relBinding);

			SecondaryConfig relByC2IdConfig = new SecondaryConfig();
			relByC2IdConfig.setReadOnly(readOnly);
			relByC2IdConfig.setDeferredWrite(deferredWrite);
			relByC2IdConfig.setAllowCreate(!readOnly);
			relByC2IdConfig.setSortedDuplicates(false);
			relByC2IdConfig.setKeyCreator(c2ToRelKeyCreator);
			relByC2IdConfig.setAllowPopulate(true);

			relByC2IdConfig.setTransactional(transactional);
			c2RelMap = env.openSecondaryDatabase(null, "c2RelMap", relDb,
					relByC2IdConfig);
		}
	}

	public void createIdMaps() throws DatabaseException {
		if (uidToIdMap == null) {
			createUidToIdMap();
		}
	}

	private void createUidToIdMap() throws DatabaseException {
		if (uidToIdMap == null) {
			SecondaryConfig uidToIdMapConfig = new SecondaryConfig();
			uidToIdMapConfig.setReadOnly(readOnly);
			uidToIdMapConfig.setDeferredWrite(deferredWrite);
			uidToIdMapConfig.setAllowCreate(!readOnly);
			uidToIdMapConfig.setSortedDuplicates(false);
			uidToIdMapConfig.setMultiKeyCreator(new UidKeyCreator(uuidBinding,
					idBinding));
			uidToIdMapConfig.setAllowPopulate(true);
			uidToIdMapConfig.setTransactional(transactional);
			uidToIdMap = env.openSecondaryDatabase(null, "uidToIdMap",
					getIdDb(), uidToIdMapConfig);
		}
	}

	private DatabaseConfig makeConfig(boolean readOnly, boolean transactional) {
		DatabaseConfig dbConfig = new DatabaseConfig();
		dbConfig.setReadOnly(readOnly);
		dbConfig.setAllowCreate(!readOnly);
		dbConfig.setDeferredWrite(deferredWrite);
		dbConfig.setSortedDuplicates(false);
		dbConfig.setTransactional(transactional);
		return dbConfig;
	}

	public Environment getEnv() {
		return env;
	}

	public void sync() throws IOException {
		if (deferredWrite) {
			try {
				if (env.getConfig().getReadOnly()) {
					return;
				}
				if (env != null) {
					if (conceptDb != null) {
						if (!conceptDb.getConfig().getReadOnly()) {
							conceptDb.sync();
						}
					}
					if (relDb != null) {
						if (!relDb.getConfig().getReadOnly()) {
							relDb.sync();
						}
					}
					if (descDb != null) {
						if (!descDb.getConfig().getReadOnly()) {
							descDb.sync();
						}
					}
					if (conceptImageMap != null) {
						if (!conceptImageMap.getConfig().getReadOnly()) {
							conceptImageMap.sync();
						}
					}
					if (imageDb != null) {
						if (!imageDb.getConfig().getReadOnly()) {
							imageDb.sync();
						}
					}
					if (uidToIdMap != null) {
						if (!uidToIdMap.getConfig().getReadOnly()) {
							uidToIdMap.sync();
						}
					}
					if (conceptDescMap != null) {
						if (!conceptDescMap.getConfig().getReadOnly()) {
							conceptDescMap.sync();
						}
					}
					if (c1RelMap != null) {
						if (!c1RelMap.getConfig().getReadOnly()) {
							c1RelMap.sync();
						}
					}
					if (c2RelMap != null) {
						if (!c2RelMap.getConfig().getReadOnly()) {
							c2RelMap.sync();
						}
					}
					if (idDb != null) {
						if (!idDb.getConfig().getReadOnly()) {
							idDb.sync();
						}
					}
					if (timeBranchDb != null) {
						if (!timeBranchDb.getConfig().getReadOnly()) {
							timeBranchDb.sync();
						}
					}
					if (pathDb != null) {
						if (!pathDb.getConfig().getReadOnly()) {
							pathDb.sync();
						}
					}
					if (extensionDb != null) {
						if (!extensionDb.getConfig().getReadOnly()) {
							extensionDb.sync();
						}
					}

					if (componentToExtMap != null) {
						if (!componentToExtMap.getConfig().getReadOnly()) {
							componentToExtMap.sync();
						}
					}

					if (refsetToExtMap != null) {
						if (!refsetToExtMap.getConfig().getReadOnly()) {
							refsetToExtMap.sync();
						}
					}

					if (metaInfoDb != null) {
						if (!metaInfoDb.getConfig().getReadOnly()) {
							metaInfoDb.sync();
						}
					}
					if (!env.getConfig().getReadOnly()) {
						env.sync();
					}
				}
			} catch (DatabaseException ex) {
				throw new ToIoException(ex);
			}
		}

	}

	public void close() throws IOException {
		try {
			sync();
			if (env != null) {
				if (conceptDb != null) {
					conceptDb.close();
				}
				if (relDb != null) {
					relDb.close();
				}
				if (conceptDescMap != null) {
					conceptDescMap.close();
				}
				if (descDb != null) {
					descDb.close();
				}
				if (conceptImageMap != null) {
					conceptImageMap.close();
				}
				if (imageDb != null) {
					imageDb.close();
				}
				if (uidToIdMap != null) {
					uidToIdMap.close();
				}
				if (c1RelMap != null) {
					c1RelMap.close();
				}
				if (c2RelMap != null) {
					c2RelMap.close();
				}
				if (idDb != null) {
					idDb.close();
				}
				if (timeBranchDb != null) {
					timeBranchDb.close();
				}
				if (pathDb != null) {
					pathDb.close();
				}
				if (extensionDb != null) {
					extensionDb.close();
				}
				if (componentToExtMap != null) {
					componentToExtMap.close();
				}

				if (refsetToExtMap != null) {
					refsetToExtMap.close();
				}
				if (metaInfoDb != null) {
					metaInfoDb.close();
				}
				// env.cleanLog();
				env.close();
			}
		} catch (DatabaseException e) {
			logger.log(Level.SEVERE, e.getLocalizedMessage(), e);
		}
	}

	public Database getConceptDb() {
		return conceptDb;
	}

	public Database getDescDb() {
		return descDb;
	}

	public Database getRelDb() {
		return relDb;
	}

	public Logger getLogger() {
		return logger;
	}

	public SecondaryDatabase getRefsetToExtMap() throws DatabaseException {
		if (refsetToExtMap == null) {
			createRefsetToExtMap();
		}
		return refsetToExtMap;
	}

	public SecondaryDatabase getComponentToExtMap() throws DatabaseException {
		if (componentToExtMap == null) {
			createComponentToExtMap();
		}
		return componentToExtMap;
	}

	public SecondaryDatabase getConceptDescMap() throws DatabaseException {
		if (conceptDescMap == null) {
			createConceptDescMap();
		}
		return conceptDescMap;
	}

	public SecondaryDatabase getConceptImageMap() throws DatabaseException {
		if (conceptImageMap == null) {
			createConceptImageMap();
		}
		return conceptImageMap;
	}

	public SecondaryDatabase getC1RelMap() throws DatabaseException {
		if (c1RelMap == null) {
			createC1RelMap();
		}
		return c1RelMap;
	}

	public SecondaryDatabase getC2RelMap() throws DatabaseException {
		if (c2RelMap == null) {
			createC2RelMap();
		}
		return c2RelMap;
	}

	public I_ConceptAttributeVersioned getConceptAttributes(int conceptId)
			throws IOException {
		Stopwatch timer = null;
		if (logger.isLoggable(Level.FINE)) {
			logger.fine("Getting concept : " + conceptId);
			timer = new Stopwatch();
			timer.start();
		}
		DatabaseEntry conceptKey = new DatabaseEntry();
		DatabaseEntry conceptValue = new DatabaseEntry();
		intBinder.objectToEntry(conceptId, conceptKey);
		try {
			if (conceptDb.get(null, conceptKey, conceptValue, LockMode.DEFAULT) == OperationStatus.SUCCESS) {
				if (logger.isLoggable(Level.FINE)) {
					logger.fine("Got concept: " + conceptId + " elapsed time: "
							+ timer.getElapsedTime() / 1000 + " secs");
				}
				return (I_ConceptAttributeVersioned) conBinding
						.entryToObject(conceptValue);
			}
		} catch (DatabaseException e) {
			throw new ToIoException(e);
		}
		throw new ToIoException(new DatabaseException(
				"Concept attributes for: " + ConceptBean.get(conceptId)
						+ " not found."));
	}

	public I_DescriptionVersioned getDescription(int descId) throws IOException {
		DatabaseEntry descKey = new DatabaseEntry();
		DatabaseEntry descValue = new DatabaseEntry();
		intBinder.objectToEntry(descId, descKey);
		try {
			if (descDb.get(null, descKey, descValue, LockMode.DEFAULT) == OperationStatus.SUCCESS) {
				return (I_DescriptionVersioned) descBinding
						.entryToObject(descValue);
			}
		} catch (DatabaseException e) {
			throw new ToIoException(e);
		}

		try {
			throw new IOException("Description: " + descId + " "
					+ getUids(descId) + " not found.");
		} catch (TerminologyException e) {
			throw new ToIoException(e);
		}
	}

	public String getProperty(String key) throws IOException {
		DatabaseEntry propKey = new DatabaseEntry();
		DatabaseEntry propValue = new DatabaseEntry();

		stringBinder.objectToEntry(key, propKey);
		try {
			if (metaInfoDb.get(null, propKey, propValue, LockMode.DEFAULT) == OperationStatus.SUCCESS) {
				return (String) stringBinder.entryToObject(propValue);
			}
		} catch (DatabaseException e) {
			throw new ToIoException(e);
		}
		return null;
	}

	public Map<String, String> getProperties() throws IOException {
		try {
			Cursor concCursor = metaInfoDb.openCursor(null, null);
			DatabaseEntry foundKey = new DatabaseEntry();
			DatabaseEntry foundData = new DatabaseEntry();
			HashMap<String, String> propMap = new HashMap<String, String>();
			while (concCursor.getNext(foundKey, foundData, LockMode.DEFAULT) == OperationStatus.SUCCESS) {
				try {
					String key = (String) stringBinder.entryToObject(foundKey);
					String value = (String) stringBinder
							.entryToObject(foundData);
					propMap.put(key, value);
				} catch (Exception e) {
					concCursor.close();
					throw new ToIoException(e);
				}
			}
			concCursor.close();
			return Collections.unmodifiableMap(propMap);
		} catch (Exception e) {
			throw new ToIoException(e);
		}
	}

	public void setProperty(String key, String value) throws IOException {
		DatabaseEntry propKey = new DatabaseEntry();
		DatabaseEntry propValue = new DatabaseEntry();
		stringBinder.objectToEntry(key, propKey);
		stringBinder.objectToEntry(value, propValue);
		try {
			metaInfoDb.put(null, propKey, propValue);
		} catch (DatabaseException e) {
			throw new ToIoException(e);
		}
	}

	public boolean hasDescription(int descId) throws DatabaseException {
		DatabaseEntry descKey = new DatabaseEntry();
		DatabaseEntry descValue = new DatabaseEntry();
		intBinder.objectToEntry(descId, descKey);
		if (descDb.get(null, descKey, descValue, LockMode.DEFAULT) == OperationStatus.SUCCESS) {
			return true;
		}
		return false;
	}

	public boolean hasRel(int relId) throws DatabaseException {
		DatabaseEntry relKey = new DatabaseEntry();
		DatabaseEntry relValue = new DatabaseEntry();
		intBinder.objectToEntry(relId, relKey);
		if (relDb.get(null, relKey, relValue, LockMode.DEFAULT) == OperationStatus.SUCCESS) {
			return true;
		}
		return false;
	}

	public I_RelVersioned getRel(int relId) throws DatabaseException {
		DatabaseEntry relKey = new DatabaseEntry();
		DatabaseEntry relValue = new DatabaseEntry();
		intBinder.objectToEntry(relId, relKey);
		if (relDb.get(null, relKey, relValue, LockMode.DEFAULT) == OperationStatus.SUCCESS) {
			return (I_RelVersioned) relBinding.entryToObject(relValue);
		}
		throw new DatabaseException("Rel: " + relId + " not found.");
	}

	public I_ThinExtByRefVersioned getExtension(int memberId)
			throws IOException {
		DatabaseEntry extKey = new DatabaseEntry();
		DatabaseEntry extValue = new DatabaseEntry();
		intBinder.objectToEntry(memberId, extKey);
		try {
			if (extensionDb.get(null, extKey, extValue, LockMode.DEFAULT) == OperationStatus.SUCCESS) {
				return (I_ThinExtByRefVersioned) extBinder
						.entryToObject(extValue);
			}
		} catch (DatabaseException ex) {
			throw new ToIoException(ex);
		}
		throw new IOException("Ext: " + memberId + " not found.");
	}

	public boolean hasExtension(int memberId) throws DatabaseException {
		DatabaseEntry extKey = new DatabaseEntry();
		DatabaseEntry extValue = new DatabaseEntry();
		intBinder.objectToEntry(memberId, extKey);
		if (extensionDb.get(null, extKey, extValue, LockMode.DEFAULT) == OperationStatus.SUCCESS) {
			return true;
		}
		return false;
	}

	public boolean hasConcept(int conceptId) throws DatabaseException {
		DatabaseEntry conceptKey = new DatabaseEntry();
		DatabaseEntry conceptValue = new DatabaseEntry();
		intBinder.objectToEntry(conceptId, conceptKey);
		if (conceptDb.get(null, conceptKey, conceptValue, LockMode.DEFAULT) == OperationStatus.SUCCESS) {
			return true;
		}
		return false;
	}

	public List<I_DescriptionVersioned> getDescriptions(int conceptId)
			throws DatabaseException {
		Stopwatch timer = null;
		if (logger.isLoggable(Level.FINE)) {
			logger.fine("Getting descriptions for: " + conceptId);
			timer = new Stopwatch();
			timer.start();
		}
		DatabaseEntry secondaryKey = new DatabaseEntry();

		descForConceptKeyCreator.createSecondaryKey(Integer.MIN_VALUE,
				conceptId, secondaryKey);
		DatabaseEntry foundData = new DatabaseEntry();

		SecondaryCursor mySecCursor = getConceptDescMap().openSecondaryCursor(
				null, null);
		OperationStatus retVal = mySecCursor.getSearchKeyRange(secondaryKey,
				foundData, LockMode.DEFAULT);
		List<I_DescriptionVersioned> matches = new ArrayList<I_DescriptionVersioned>();
		while (retVal == OperationStatus.SUCCESS) {
			ThinDescVersioned descFromConceptId = (ThinDescVersioned) descBinding
					.entryToObject(foundData);
			if (descFromConceptId.getConceptId() == conceptId) {
				matches.add(descFromConceptId);
			} else {
				break;
			}
			retVal = mySecCursor.getNext(secondaryKey, foundData,
					LockMode.DEFAULT);
		}
		mySecCursor.close();
		if (logger.isLoggable(Level.FINE)) {
			logger.fine("Descriptions fetched for: " + conceptId
					+ " elapsed time: " + timer.getElapsedTime() / 1000
					+ " secs");
		}
		return matches;
	}

	public boolean hasDestRelTuple(int conceptId, I_IntSet allowedStatus,
			I_IntSet destRelTypes, Set<I_Position> positions)
			throws DatabaseException {
		Stopwatch timer = null;
		if (logger.isLoggable(Level.FINE)) {
			logger.fine("Getting dest rels for: " + conceptId);
			timer = new Stopwatch();
			timer.start();
		}
		DatabaseEntry secondaryKey = new DatabaseEntry();

		c2KeyCreator.createSecondaryKey(Integer.MIN_VALUE, conceptId,
				secondaryKey);
		DatabaseEntry foundData = new DatabaseEntry();

		SecondaryCursor mySecCursor = getC2RelMap().openSecondaryCursor(null,
				null);
		OperationStatus retVal = mySecCursor.getSearchKeyRange(secondaryKey,
				foundData, LockMode.DEFAULT);
		List<I_RelTuple> returnRels = new ArrayList<I_RelTuple>();
		while (retVal == OperationStatus.SUCCESS) {
			I_RelVersioned relFromConceptId = (I_RelVersioned) relBinding
					.entryToObject(foundData);
			if (relFromConceptId.getC2Id() == conceptId) {
				relFromConceptId.addTuples(allowedStatus, destRelTypes,
						positions, returnRels, false);
				if (returnRels.size() > 0) {
					return true;
				}
			} else {
				break;
			}
			retVal = mySecCursor.getNext(secondaryKey, foundData,
					LockMode.DEFAULT);
		}
		mySecCursor.close();
		if (logger.isLoggable(Level.FINE)) {
			logger.fine("dest rels fetched for: " + conceptId
					+ " elapsed time: " + timer.getElapsedTime() / 1000
					+ " secs");
		}
		return false;
	}

	public List<I_RelVersioned> getDestRels(int conceptId)
			throws DatabaseException {
		Stopwatch timer = null;
		if (logger.isLoggable(Level.FINE)) {
			logger.fine("Getting dest rels for: " + conceptId);
			timer = new Stopwatch();
			timer.start();
		}
		DatabaseEntry secondaryKey = new DatabaseEntry();

		c2KeyCreator.createSecondaryKey(Integer.MIN_VALUE, conceptId,
				secondaryKey);
		DatabaseEntry foundData = new DatabaseEntry();

		SecondaryCursor mySecCursor = getC2RelMap().openSecondaryCursor(null,
				null);
		OperationStatus retVal = mySecCursor.getSearchKeyRange(secondaryKey,
				foundData, LockMode.DEFAULT);
		List<I_RelVersioned> matches = new ArrayList<I_RelVersioned>();
		while (retVal == OperationStatus.SUCCESS) {
			ThinRelVersioned relFromConceptId = (ThinRelVersioned) relBinding
					.entryToObject(foundData);
			if (relFromConceptId.getC2Id() == conceptId) {
				matches.add(relFromConceptId);
			} else {
				break;
			}
			retVal = mySecCursor.getNext(secondaryKey, foundData,
					LockMode.DEFAULT);
		}
		mySecCursor.close();
		if (logger.isLoggable(Level.FINE)) {
			logger.fine("dest rels fetched for: " + conceptId
					+ " elapsed time: " + timer.getElapsedTime() / 1000
					+ " secs");
		}
		return matches;
	}

	public List<I_GetExtensionData> getExtensionsForComponent(int componentId)
			throws IOException {
		try {
			Stopwatch timer = null;
			if (logger.isLoggable(Level.FINE)) {
				logger.fine("Getting extensions from componentId for: "
						+ componentId);
				timer = new Stopwatch();
				timer.start();
			}
			DatabaseEntry secondaryKey = new DatabaseEntry();

			componentKeyCreator.createSecondaryKey(Integer.MIN_VALUE,
					componentId, secondaryKey);
			DatabaseEntry foundData = new DatabaseEntry();

			SecondaryCursor mySecCursor = getComponentToExtMap()
					.openSecondaryCursor(null, null);
			OperationStatus retVal = mySecCursor.getSearchKeyRange(
					secondaryKey, foundData, LockMode.DEFAULT);
			List<I_GetExtensionData> matches = new ArrayList<I_GetExtensionData>();
			int count = 0;
			int rejected = 0;
			while (retVal == OperationStatus.SUCCESS) {
				I_ThinExtByRefVersioned extFromComponentId = (I_ThinExtByRefVersioned) extBinder
						.entryToObject(foundData);
				if (extFromComponentId.getComponentId() == componentId) {
					count++;
					matches.add(ExtensionByReferenceBean.make(
							extFromComponentId.getMemberId(),
							extFromComponentId));
				} else {
					rejected++;
					break;
				}
				retVal = mySecCursor.getNext(secondaryKey, foundData,
						LockMode.DEFAULT);
			}
			mySecCursor.close();
			if (logger.isLoggable(Level.FINE)) {
				logger.fine(count + " extensions fetched, " + rejected
						+ " extensions rejected " + "for: " + componentId
						+ " elapsed time: " + timer.getElapsedTime() / 1000
						+ " secs");
			}
			return matches;
		} catch (DatabaseException ex) {
			throw new ToIoException(ex);
		}
	}

	public List<I_ThinExtByRefVersioned> getAllExtensionsForComponent(
			int componentId, boolean addUncommitted) throws IOException {
		List<I_ThinExtByRefVersioned> extensions = getAllExtensionsForComponent(componentId);

		for (I_GetExtensionData wrapper : ExtensionByReferenceBean
				.getNewExtensions(componentId)) {
			extensions.add(wrapper.getExtension());
		}

		return extensions;
	}

	public List<I_ThinExtByRefVersioned> getAllExtensionsForComponent(
			int componentId) throws IOException {
		try {
			Stopwatch timer = null;
			if (logger.isLoggable(Level.FINE)) {
				logger.fine("Getting extensions from componentId for: "
						+ componentId);
				timer = new Stopwatch();
				timer.start();
			}
			DatabaseEntry secondaryKey = new DatabaseEntry();

			componentKeyCreator.createSecondaryKey(Integer.MIN_VALUE,
					componentId, secondaryKey);
			DatabaseEntry foundData = new DatabaseEntry();

			SecondaryCursor mySecCursor = getComponentToExtMap()
					.openSecondaryCursor(null, null);
			OperationStatus retVal = mySecCursor.getSearchKeyRange(
					secondaryKey, foundData, LockMode.DEFAULT);
			List<I_ThinExtByRefVersioned> matches = new ArrayList<I_ThinExtByRefVersioned>();
			int count = 0;
			int rejected = 0;
			while (retVal == OperationStatus.SUCCESS) {
				I_ThinExtByRefVersioned extFromComponentId = (I_ThinExtByRefVersioned) extBinder
						.entryToObject(foundData);
				if (extFromComponentId.getComponentId() == componentId) {
					count++;
					ExtensionByReferenceBean extBean = ExtensionByReferenceBean
							.make(extFromComponentId.getMemberId(),
									extFromComponentId);
					matches.add(extBean.getExtension());
				} else {
					rejected++;
					break;
				}
				retVal = mySecCursor.getNext(secondaryKey, foundData,
						LockMode.DEFAULT);
			}
			mySecCursor.close();
			if (logger.isLoggable(Level.FINE)) {
				logger.fine(count + " extensions fetched, " + rejected
						+ " extensions rejected " + "for: " + componentId
						+ " elapsed time: " + timer.getElapsedTime() / 1000
						+ " secs");
			}
			return matches;
		} catch (DatabaseException ex) {
			throw new ToIoException(ex);
		}
	}

	public List<ExtensionByReferenceBean> getExtensionsForRefset(int refsetId)
			throws DatabaseException {
		Stopwatch timer = null;
		if (logger.isLoggable(Level.FINE)) {
			logger.fine("Getting extensions from refsetId for: " + refsetId);
			timer = new Stopwatch();
			timer.start();
		}
		DatabaseEntry secondaryKey = new DatabaseEntry();

		refsetKeyCreator.createSecondaryKey(Integer.MIN_VALUE, refsetId,
				secondaryKey);
		DatabaseEntry foundData = new DatabaseEntry();

		SecondaryCursor mySecCursor = getRefsetToExtMap().openSecondaryCursor(
				null, null);
		OperationStatus retVal = mySecCursor.getSearchKeyRange(secondaryKey,
				foundData, LockMode.DEFAULT);
		List<ExtensionByReferenceBean> matches = new ArrayList<ExtensionByReferenceBean>();
		while (retVal == OperationStatus.SUCCESS) {
			I_ThinExtByRefVersioned extFromComponentId = (I_ThinExtByRefVersioned) extBinder
					.entryToObject(foundData);
			if (extFromComponentId.getRefsetId() == refsetId) {
				matches.add(ExtensionByReferenceBean.make(extFromComponentId
						.getMemberId(), extFromComponentId));
			} else {
				break;
			}
			retVal = mySecCursor.getNext(secondaryKey, foundData,
					LockMode.DEFAULT);
		}
		mySecCursor.close();
		if (logger.isLoggable(Level.FINE)) {
			logger.fine("Extensions fetched for: " + refsetId
					+ " elapsed time: " + timer.getElapsedTime() / 1000
					+ " secs");
		}
		return matches;
	}

	public boolean hasDestRels(int conceptId) throws DatabaseException {
		Stopwatch timer = null;
		if (logger.isLoggable(Level.FINE)) {
			logger.fine("Getting dest rels for: " + conceptId);
			timer = new Stopwatch();
			timer.start();
		}
		DatabaseEntry secondaryKey = new DatabaseEntry();

		c2KeyCreator.createSecondaryKey(Integer.MIN_VALUE, conceptId,
				secondaryKey);
		DatabaseEntry foundData = new DatabaseEntry();

		SecondaryCursor mySecCursor = getC2RelMap().openSecondaryCursor(null,
				null);
		OperationStatus retVal = mySecCursor.getSearchKeyRange(secondaryKey,
				foundData, LockMode.DEFAULT);
		while (retVal == OperationStatus.SUCCESS) {
			I_RelVersioned relFromConceptId = (I_RelVersioned) relBinding
					.entryToObject(foundData);
			if (relFromConceptId.getC2Id() == conceptId) {
				mySecCursor.close();
				return true;
			} else {
				break;
			}
		}
		mySecCursor.close();
		if (logger.isLoggable(Level.FINE)) {
			logger.fine("dest rels fetched for: " + conceptId
					+ " elapsed time: " + timer.getElapsedTime() / 1000
					+ " secs");
		}
		return false;
	}

	public boolean hasDestRel(int conceptId, Set<Integer> destRelTypeIds)
			throws DatabaseException {
		Stopwatch timer = null;
		if (logger.isLoggable(Level.FINE)) {
			logger.fine("hasDestRel for: " + conceptId);
			timer = new Stopwatch();
			timer.start();
		}
		DatabaseEntry secondaryKey = new DatabaseEntry();

		c2KeyCreator.createSecondaryKey(Integer.MIN_VALUE, conceptId,
				secondaryKey);
		DatabaseEntry foundData = new DatabaseEntry();

		SecondaryCursor mySecCursor = getC2RelMap().openSecondaryCursor(null,
				null);
		OperationStatus retVal = mySecCursor.getSearchKeyRange(secondaryKey,
				foundData, LockMode.DEFAULT);
		while (retVal == OperationStatus.SUCCESS) {
			I_RelVersioned relFromConceptId = (I_RelVersioned) relBinding
					.entryToObject(foundData);
			if (relFromConceptId.getC2Id() == conceptId) {
				if (destRelTypeIds == null) {
					mySecCursor.close();
					return true;
				}
				if (destRelTypeIds.contains(relFromConceptId.getVersions().get(
						0).getRelTypeId())) {
					mySecCursor.close();
					return true;
				}

			} else {
				break;
			}
			retVal = mySecCursor.getNext(secondaryKey, foundData,
					LockMode.DEFAULT);
		}
		mySecCursor.close();
		if (logger.isLoggable(Level.FINE)) {
			logger.fine("hasDestRel for: " + conceptId + " elapsed time: "
					+ timer.getElapsedTime() / 1000 + " secs");
		}
		return false;
	}

	public List<I_RelVersioned> getSrcRels(int conceptId)
			throws DatabaseException {
		Stopwatch timer = null;
		if (logger.isLoggable(Level.FINE)) {
			logger.fine("Getting src rels for: " + conceptId);
			timer = new Stopwatch();
			timer.start();
		}

		DatabaseEntry secondaryKey = new DatabaseEntry();
		c1KeyCreator.createSecondaryKey(Integer.MIN_VALUE, conceptId,
				secondaryKey);
		DatabaseEntry foundData = new DatabaseEntry();

		SecondaryCursor mySecCursor = getC1RelMap().openSecondaryCursor(null,
				null);
		OperationStatus retVal = mySecCursor.getSearchKeyRange(secondaryKey,
				foundData, LockMode.DEFAULT);
		List<I_RelVersioned> matches = new ArrayList<I_RelVersioned>();
		while (retVal == OperationStatus.SUCCESS) {
			ThinRelVersioned relFromConceptId = (ThinRelVersioned) relBinding
					.entryToObject(foundData);
			if (relFromConceptId.getC1Id() == conceptId) {
				matches.add(relFromConceptId);
			} else {
				break;
			}
			retVal = mySecCursor.getNext(secondaryKey, foundData,
					LockMode.DEFAULT);
		}
		mySecCursor.close();
		if (logger.isLoggable(Level.FINE)) {
			logger.fine("src rels fetched for: " + conceptId
					+ " elapsed time: " + timer.getElapsedTime() / 1000
					+ " secs");
		}
		return matches;
	}

	public boolean hasSrcRelTuple(int conceptId, I_IntSet allowedStatus,
			I_IntSet sourceRelTypes, Set<I_Position> positions)
			throws DatabaseException {
		Stopwatch timer = null;
		if (logger.isLoggable(Level.FINE)) {
			logger.fine("Getting src rels for: " + conceptId);
			timer = new Stopwatch();
			timer.start();
		}

		DatabaseEntry secondaryKey = new DatabaseEntry();
		c1KeyCreator.createSecondaryKey(Integer.MIN_VALUE, conceptId,
				secondaryKey);
		DatabaseEntry foundData = new DatabaseEntry();

		SecondaryCursor mySecCursor = getC1RelMap().openSecondaryCursor(null,
				null);
		OperationStatus retVal = mySecCursor.getSearchKeyRange(secondaryKey,
				foundData, LockMode.DEFAULT);
		List<I_RelTuple> tuples = new ArrayList<I_RelTuple>();
		while (retVal == OperationStatus.SUCCESS) {
			I_RelVersioned relFromConceptId = (I_RelVersioned) relBinding
					.entryToObject(foundData);
			if (relFromConceptId.getC1Id() == conceptId) {
				relFromConceptId.addTuples(allowedStatus, sourceRelTypes,
						positions, tuples, false);
				if (tuples.size() > 0) {
					return true;
				}
			} else {
				break;
			}
			retVal = mySecCursor.getNext(secondaryKey, foundData,
					LockMode.DEFAULT);
		}
		mySecCursor.close();
		if (logger.isLoggable(Level.FINE)) {
			logger.fine("src rels fetched for: " + conceptId
					+ " elapsed time: " + timer.getElapsedTime() / 1000
					+ " secs");
		}
		return false;
	}

	public boolean hasSrcRels(int conceptId) throws DatabaseException {
		Stopwatch timer = null;
		if (logger.isLoggable(Level.FINE)) {
			logger.fine("Getting src rels for: " + conceptId);
			timer = new Stopwatch();
			timer.start();
		}

		DatabaseEntry secondaryKey = new DatabaseEntry();
		c1KeyCreator.createSecondaryKey(Integer.MIN_VALUE, conceptId,
				secondaryKey);
		DatabaseEntry foundData = new DatabaseEntry();

		SecondaryCursor mySecCursor = getC1RelMap().openSecondaryCursor(null,
				null);
		OperationStatus retVal = mySecCursor.getSearchKeyRange(secondaryKey,
				foundData, LockMode.DEFAULT);
		while (retVal == OperationStatus.SUCCESS) {
			I_RelVersioned relFromConceptId = (I_RelVersioned) relBinding
					.entryToObject(foundData);
			if (relFromConceptId.getC1Id() == conceptId) {
				return true;
			} else {
				break;
			}
		}
		mySecCursor.close();
		if (logger.isLoggable(Level.FINE)) {
			logger.fine("src rels fetched for: " + conceptId
					+ " elapsed time: " + timer.getElapsedTime() / 1000
					+ " secs");
		}
		return false;
	}

	public boolean hasSrcRel(int conceptId, Set<Integer> srcRelTypeIds)
			throws DatabaseException {
		Stopwatch timer = null;
		if (logger.isLoggable(Level.FINE)) {
			logger.fine("Getting src rels for: " + conceptId);
			timer = new Stopwatch();
			timer.start();
		}

		DatabaseEntry secondaryKey = new DatabaseEntry();
		c1KeyCreator.createSecondaryKey(Integer.MIN_VALUE, conceptId,
				secondaryKey);
		DatabaseEntry foundData = new DatabaseEntry();

		SecondaryCursor mySecCursor = getC1RelMap().openSecondaryCursor(null,
				null);
		OperationStatus retVal = mySecCursor.getSearchKeyRange(secondaryKey,
				foundData, LockMode.DEFAULT);
		while (retVal == OperationStatus.SUCCESS) {
			I_RelVersioned relFromConceptId = (I_RelVersioned) relBinding
					.entryToObject(foundData);
			if (relFromConceptId.getC1Id() == conceptId) {
				if (srcRelTypeIds == null) {
					mySecCursor.close();
					return true;
				}
				if (srcRelTypeIds.contains(relFromConceptId.getVersions()
						.get(0).getRelTypeId())) {
					mySecCursor.close();
					return true;
				}
			} else {
				break;
			}
			retVal = mySecCursor.getNext(secondaryKey, foundData,
					LockMode.DEFAULT);
		}
		mySecCursor.close();
		if (logger.isLoggable(Level.FINE)) {
			logger.fine("src rels fetched for: " + conceptId
					+ " elapsed time: " + timer.getElapsedTime() / 1000
					+ " secs");
		}
		return false;
	}

	public SecondaryDatabase getUidToIdMap() throws DatabaseException {
		if (uidToIdMap == null) {
			createUidToIdMap();
		}
		return uidToIdMap;
	}

	public Database getIdDb() {
		return idDb;
	}

	public int countDescriptions() throws DatabaseException {
		if (descCount > 0) {
			return descCount;
		}
		Stopwatch timer = null;
		if (logger.isLoggable(Level.INFO)) {
			timer = new Stopwatch();
			timer.start();
		}
		Cursor descCursor = getDescDb().openCursor(null, null);
		DatabaseEntry foundKey = new DatabaseEntry();
		foundKey.setPartial(0, 0, true);
		DatabaseEntry foundData = new DatabaseEntry();
		foundData.setPartial(0, 0, true);
		int count = 0;
		while (descCursor.getNext(foundKey, foundData, LockMode.DEFAULT) == OperationStatus.SUCCESS) {
			count++;
		}
		descCursor.close();
		if (logger.isLoggable(Level.INFO)) {
			logger.info("Desc count: " + count + " count time: "
					+ timer.getElapsedTime());
			timer.stop();
		}
		descCount = count;
		return count;
	}

	/**
	 * This method is multithreaded hot.
	 * 
	 * @param continueWork
	 * @param p
	 * @param matches
	 * @param latch
	 * @throws DatabaseException
	 */
	public void searchRegex(I_TrackContinuation tracker, Pattern p,
			Collection<ThinDescVersioned> matches, CountDownLatch latch,
			List<I_TestSearchResults> checkList, I_ConfigAceFrame config)
			throws DatabaseException {
		Stopwatch timer = null;
		if (logger.isLoggable(Level.INFO)) {
			timer = new Stopwatch();
			timer.start();
		}
		Cursor descCursor = getDescDb().openCursor(null, null);
		DatabaseEntry foundKey = new DatabaseEntry();
		DatabaseEntry foundData = new DatabaseEntry();
		while (descCursor.getNext(foundKey, foundData, LockMode.DEFAULT) == OperationStatus.SUCCESS) {
			if (tracker.continueWork()) {
				ThinDescVersioned descV = (ThinDescVersioned) descBinding
						.entryToObject(foundData);
				ACE.threadPool.execute(new CheckAndProcessRegexMatch(p,
						matches, descV, checkList, config));
			} else {
				while (latch.getCount() > 0) {
					latch.countDown();
				}
				break;
			}
			latch.countDown();
		}
		descCursor.close();
		if (logger.isLoggable(Level.INFO)) {
			if (tracker.continueWork()) {
				logger.info("Search time: " + timer.getElapsedTime());
			} else {
				logger
						.info("Canceled. Elapsed time: "
								+ timer.getElapsedTime());
			}
			timer.stop();
		}
	}

	/*
	 * For issues upgrading to lucene 2.x, see this link:
	 * http://www.nabble.com/Lucene-in-Action-examples-complie-problem-tf2418478.html#a6743189
	 */

	public CountDownLatch searchLucene(I_TrackContinuation tracker,
			String query, Collection<LuceneMatch> matches,
			CountDownLatch latch, List<I_TestSearchResults> checkList,
			I_ConfigAceFrame config, LuceneProgressUpdator updater)
			throws DatabaseException, IOException, ParseException {
		Stopwatch timer = null;
		if (logger.isLoggable(Level.INFO)) {
			timer = new Stopwatch();
			timer.start();
		}
		if (luceneDir.exists() == false) {
			updater
					.setProgressInfo("Making lucene index -- this may take a while...");
			createLuceneDescriptionIndex();
		}
		updater.setIndeterminate(true);
		if (luceneSearcher == null) {
			updater.setProgressInfo("Opening search index...");
			luceneSearcher = new IndexSearcher(luceneDir.getAbsolutePath());
		}
		updater.setProgressInfo("Starting lucene query...");
		long startTime = System.currentTimeMillis();
		Query q = new QueryParser("desc", new StandardAnalyzer()).parse(query);
		updater.setProgressInfo("Query complete in "
				+ Long.toString(System.currentTimeMillis() - startTime)
				+ " ms.");
		Hits hits = luceneSearcher.search(q);
		updater.setProgressInfo("Query complete in "
				+ Long.toString(System.currentTimeMillis() - startTime)
				+ " ms. Hits: " + hits.length());

		CountDownLatch hitLatch = new CountDownLatch(hits.length());
		updater.setHits(hits.length());
		updater.setIndeterminate(false);

		for (int i = 0; i < hits.length(); i++) {
			Document doc = hits.doc(i);
			float score = hits.score(i);
			if (logger.isLoggable(Level.FINE)) {
				logger.fine("Hit: " + doc + " Score: " + score);
			}

			ACE.threadPool.execute(new CheckAndProcessLuceneMatch(hitLatch,
					updater, doc, score, matches, checkList, config,
					VodbEnv.this));
		}
		if (logger.isLoggable(Level.INFO)) {
			if (tracker.continueWork()) {
				logger.info("Search time: " + timer.getElapsedTime());
			} else {
				logger.info("Search Canceled. Elapsed time: "
						+ timer.getElapsedTime());
			}
			timer.stop();
		}
		return hitLatch;
	}

	public void createLuceneDescriptionIndex() throws IOException {
		try {
			Stopwatch timer = new Stopwatch();
			timer.start();
			luceneDir.mkdirs();
			IndexWriter writer = new IndexWriter(luceneDir,
					new StandardAnalyzer(), true);
			writer.setUseCompoundFile(true);
			writer.setMergeFactor(10000);
			writer.setMaxMergeDocs(Integer.MAX_VALUE);
			writer.setMaxBufferedDocs(1000);
			Cursor descCursor = getDescDb().openCursor(null, null);
			DatabaseEntry foundKey = new DatabaseEntry();
			DatabaseEntry foundData = new DatabaseEntry();
			int counter = 0;
			int optimizeInterval = 10000;
			while (descCursor.getNext(foundKey, foundData, LockMode.DEFAULT) == OperationStatus.SUCCESS) {
				ThinDescVersioned descV = (ThinDescVersioned) descBinding
						.entryToObject(foundData);
				Document doc = new Document();
				doc.add(new Field("dnid", Integer.toString(descV.getDescId()),
						Field.Store.YES, Field.Index.UN_TOKENIZED));
				doc.add(new Field("cnid", Integer
						.toString(descV.getConceptId()), Field.Store.YES,
						Field.Index.UN_TOKENIZED));
				addIdsToIndex(doc, getId(descV.getDescId()));
				addIdsToIndex(doc, getId(descV.getConceptId()));

				String lastDesc = null;
				for (I_DescriptionTuple tuple : descV.getTuples()) {
					if (lastDesc == null
							|| lastDesc.equals(tuple.getText()) == false) {
						doc.add(new Field("desc", tuple.getText(),
								Field.Store.NO, Field.Index.TOKENIZED));
					}

				}
				writer.addDocument(doc);
				counter++;
				if (counter == optimizeInterval) {
					writer.optimize();
					counter = 0;
				}
			}
			descCursor.close();
			logger.info("Optimizing index time: " + timer.getElapsedTime());
			writer.optimize();
			writer.close();
			if (logger.isLoggable(Level.INFO)) {
				logger.info("Index time: " + timer.getElapsedTime());
				timer.stop();
			}
		} catch (DatabaseException ex) {
			throw new ToIoException(ex);
		}
	}

	private void addIdsToIndex(Document doc, I_IdVersioned did) {
		for (I_IdPart p : did.getVersions()) {
			doc.add(new Field("desc", p.getSourceId().toString(),
					Field.Store.NO, Field.Index.UN_TOKENIZED));
		}
	}

	IndexSearcher luceneSearcher = null;

	private IndexSearcher luceneLicitSearcher;

	private IndexWriter licitIndexWriter;

	private boolean debugWrites = false;

	private static class CheckAndProcessRegexMatch implements Runnable {
		Pattern p;

		Collection<ThinDescVersioned> matches;

		ThinDescVersioned descV;

		List<I_TestSearchResults> checkList;

		I_ConfigAceFrame config;

		public CheckAndProcessRegexMatch(Pattern p,
				Collection<ThinDescVersioned> matches, ThinDescVersioned descV,
				List<I_TestSearchResults> checkList, I_ConfigAceFrame config) {
			super();
			this.p = p;
			this.matches = matches;
			this.descV = descV;
			this.checkList = checkList;
			this.config = config;
		}

		public void run() {
			if (descV.matches(p)) {
				if (checkList == null || checkList.size() == 0) {
					matches.add(descV);
				} else {
					try {
						boolean failed = false;
						for (I_TestSearchResults test : checkList) {
							if (test.test(descV, config) == false) {
								failed = true;
								break;
							}
						}

						if (failed == false) {
							matches.add(descV);
						}
					} catch (TaskFailedException e) {
						if (ACE.editMode) {
							AceLog.getAppLog().alertAndLogException(e);
						} else {
							AceLog.getAppLog().log(Level.SEVERE,
									e.getLocalizedMessage(), e);
						}
					}
				}
			}
		}

	}

	private static class CheckAndProcessLuceneMatch implements Runnable {

		Collection<LuceneMatch> matches;

		List<I_TestSearchResults> checkList;

		I_ConfigAceFrame config;

		Document doc;

		VodbEnv env;

		private float score;

		private CountDownLatch hitLatch;

		public CheckAndProcessLuceneMatch(CountDownLatch hitLatch,
				LuceneProgressUpdator updater, Document doc, float score,
				Collection<LuceneMatch> matches,
				List<I_TestSearchResults> checkList, I_ConfigAceFrame config,
				VodbEnv env) {
			super();
			this.doc = doc;
			this.score = score;
			this.matches = matches;
			this.checkList = checkList;
			this.config = config;
			this.env = env;
			this.hitLatch = hitLatch;
		}

		public void run() {
			if (hitLatch.getCount() > 0) {
				int nid = Integer.parseInt(doc.get("dnid"));
				try {
					ThinDescVersioned descV = (ThinDescVersioned) env
							.getDescription(nid);
					LuceneMatch match = new LuceneMatch(descV, score);
					if (checkList == null || checkList.size() == 0) {
						matches.add(match);
						if (logger.isLoggable(Level.FINE)) {
							logger.fine("processing match: " + descV
									+ " new match size: " + matches.size());
						}
					} else {
						try {
							boolean failed = false;
							for (I_TestSearchResults test : checkList) {
								if (test.test(descV, config) == false) {
									failed = true;
									break;
								}
							}

							if (failed == false) {
								matches.add(match);
							}
						} catch (TaskFailedException e) {
							AceLog.getAppLog().alertAndLogException(e);
						}
					}
				} catch (IOException e1) {
					AceLog.getAppLog().alertAndLogException(e1);
				}
				this.hitLatch.countDown();
				if (logger.isLoggable(Level.FINE)) {
					logger.fine("Hit latch: " + this.hitLatch.getCount());
				}

			}

		}
	}

	public Database getTimeBranchDb() {
		return timeBranchDb;
	}

	public void addTimeBranchValues(Set<TimePathId> values)
			throws DatabaseException {
		DatabaseEntry key = new DatabaseEntry();
		DatabaseEntry value = new DatabaseEntry();
		for (TimePathId tb : values) {
			btBinder.objectToEntry(tb, key);
			tbBinder.objectToEntry(tb, value);
			timeBranchDb.put(null, key, value);
		}
	}

	public void populateTimeBranchDb() throws Exception {
		Set<TimePathId> values = new HashSet<TimePathId>();
		DescChangesProcessor p = new DescChangesProcessor(values);
		iterateDescriptionEntries(p);
		iterateConceptAttributeEntries(p);
		iterateRelationships(p);
		addTimeBranchValues(values);
	}

	private static class DescChangesProcessor implements
			I_ProcessDescriptionEntries, I_ProcessConceptAttributeEntries,
			I_ProcessRelationshipEntries {
		Set<TimePathId> values;

		public DescChangesProcessor(Set<TimePathId> values) {
			super();
			this.values = values;
		}

		public void processDesc(DatabaseEntry key, DatabaseEntry value)
				throws Exception {
			I_DescriptionVersioned desc = (I_DescriptionVersioned) descBinding
					.entryToObject(value);
			for (I_DescriptionPart d : desc.getVersions()) {
				TimePathId tb = new TimePathId(d.getVersion(), d.getPathId());
				values.add(tb);
			}
		}

		public void processConceptAttributeEntry(DatabaseEntry key,
				DatabaseEntry value) throws IOException {
			I_ConceptAttributeVersioned conc = (I_ConceptAttributeVersioned) conBinding
					.entryToObject(value);
			for (I_ConceptAttributePart c : conc.getVersions()) {
				TimePathId tb = new TimePathId(c.getVersion(), c.getPathId());
				values.add(tb);
			}
		}

		public void processRel(DatabaseEntry key, DatabaseEntry value)
				throws Exception {
			I_RelVersioned rel = (I_RelVersioned) relBinding
					.entryToObject(value);
			for (I_RelPart r : rel.getVersions()) {
				TimePathId tb = new TimePathId(r.getVersion(), r.getPathId());
				values.add(tb);
			}
		}

		public DatabaseEntry getDataEntry() {
			return new DatabaseEntry();
		}

		public DatabaseEntry getKeyEntry() {
			return new DatabaseEntry();
		}

	}

	public void iterateDescriptionEntries(I_ProcessDescriptionEntries processor)
			throws Exception {
		Cursor descCursor = getDescDb().openCursor(null, null);
		DatabaseEntry foundKey = processor.getKeyEntry();
		DatabaseEntry foundData = processor.getDataEntry();
		while (descCursor.getNext(foundKey, foundData, LockMode.DEFAULT) == OperationStatus.SUCCESS) {
			try {
				processor.processDesc(foundKey, foundData);
			} catch (Exception e) {
				descCursor.close();
				throw e;
			}
		}
		descCursor.close();
	}

	public void iterateRelationships(I_ProcessRelationshipEntries processor)
			throws Exception {
		Cursor relCursor = getRelDb().openCursor(null, null);
		DatabaseEntry foundKey = processor.getKeyEntry();
		DatabaseEntry foundData = processor.getDataEntry();
		while (relCursor.getNext(foundKey, foundData, LockMode.DEFAULT) == OperationStatus.SUCCESS) {
			try {
				processor.processRel(foundKey, foundData);
			} catch (Exception e) {
				relCursor.close();
				throw e;
			}
		}
		relCursor.close();
	}

	public void iterateExtByRefEntries(I_ProcessExtByRefEntries processor)
			throws Exception {
		Cursor extCursor = extensionDb.openCursor(null, null);
		DatabaseEntry foundKey = processor.getKeyEntry();
		DatabaseEntry foundData = processor.getDataEntry();
		while (extCursor.getNext(foundKey, foundData, LockMode.DEFAULT) == OperationStatus.SUCCESS) {
			try {
				processor.processEbr(foundKey, foundData);
			} catch (Exception e) {
				extCursor.close();
				throw e;
			}
		}
		extCursor.close();
	}

	public List<I_ThinExtByRefVersioned> getRefsetExtensionMembers(int refsetId)
			throws IOException {
		try {
			List<I_ThinExtByRefVersioned> returnList = new ArrayList<I_ThinExtByRefVersioned>();
			Cursor extCursor = extensionDb.openCursor(null, null);
			DatabaseEntry foundKey = new DatabaseEntry();
			DatabaseEntry foundData = new DatabaseEntry();
			while (extCursor.getNext(foundKey, foundData, LockMode.DEFAULT) == OperationStatus.SUCCESS) {
				try {
					I_ThinExtByRefVersioned extension = (I_ThinExtByRefVersioned) extBinder
							.entryToObject(foundData);
					if (extension.getRefsetId() == refsetId) {
						returnList.add(extension);
					}
				} catch (Exception e) {
					extCursor.close();
					throw e;
				}
			}
			extCursor.close();
			return returnList;
		} catch (Exception e) {
			throw new ToIoException(e);
		}
	}

	private class ConceptIterator implements Iterator<I_GetConceptData> {

		DatabaseEntry foundKey = new DatabaseEntry();

		DatabaseEntry foundData = new DatabaseEntry();

		boolean hasNext;

		private Integer conceptId;

		private Cursor concCursor;

		private ConceptIterator() throws IOException {
			super();
			try {
				concCursor = getConceptDb().openCursor(null, null);
				getNext();
			} catch (DatabaseException e) {
				throw new ToIoException(e);
			}
		}

		private void getNext() {
			try {
				hasNext = (concCursor.getNext(foundKey, foundData,
						LockMode.DEFAULT) == OperationStatus.SUCCESS);
				if (hasNext) {
					conceptId = (Integer) intBinder.entryToObject(foundKey);
				} else {
					conceptId = null;
					concCursor.close();
				}
			} catch (Exception ex) {
				try {
					concCursor.close();
				} catch (DatabaseException e) {
					AceLog.getAppLog().alertAndLogException(ex);
				}
				AceLog.getAppLog().alertAndLogException(ex);
				hasNext = false;
			}
		}

		public boolean hasNext() {
			return hasNext;
		}

		public I_GetConceptData next() {
			if (hasNext) {
				I_GetConceptData next = ConceptBean.get(conceptId);
				getNext();
				return next;
			}
			return null;
		}

		public void remove() {
			throw new UnsupportedOperationException();
		}

	}

	public Iterator<I_GetConceptData> getConceptIterator() throws IOException {
		return new ConceptIterator();
	}

	public void iterateConceptAttributeEntries(
			I_ProcessConceptAttributeEntries processor) throws Exception {
		Cursor concCursor = getConceptDb().openCursor(null, null);
		DatabaseEntry foundKey = processor.getKeyEntry();
		DatabaseEntry foundData = processor.getDataEntry();
		while (concCursor.getNext(foundKey, foundData, LockMode.DEFAULT) == OperationStatus.SUCCESS) {
			try {
				processor.processConceptAttributeEntry(foundKey, foundData);
			} catch (Exception e) {
				concCursor.close();
				throw e;
			}
		}
		concCursor.close();
	}

	public void iterateIdEntries(I_ProcessIdEntries processor) throws Exception {
		Cursor idCursor = getIdDb().openCursor(null, null);
		DatabaseEntry foundKey = processor.getKeyEntry();
		DatabaseEntry foundData = processor.getDataEntry();
		while (idCursor.getNext(foundKey, foundData, LockMode.DEFAULT) == OperationStatus.SUCCESS) {
			try {
				processor.processId(foundKey, foundData);
			} catch (Exception e) {
				idCursor.close();
				throw e;
			}
		}
		idCursor.close();
	}

	public void iterateImages(I_ProcessImageEntries processor) throws Exception {
		Cursor imageCursor = getImageDb().openCursor(null, null);
		DatabaseEntry foundKey = processor.getKeyEntry();
		DatabaseEntry foundData = processor.getDataEntry();
		while (imageCursor.getNext(foundKey, foundData, LockMode.DEFAULT) == OperationStatus.SUCCESS) {
			try {
				processor.processImages(foundKey, foundData);
			} catch (Exception e) {
				imageCursor.close();
				throw e;
			}
		}
		imageCursor.close();
	}

	public void iteratePaths(I_ProcessPathEntries processor) throws Exception {
		Cursor pathCursor = getPathDb().openCursor(null, null);
		DatabaseEntry foundKey = processor.getKeyEntry();
		DatabaseEntry foundData = processor.getDataEntry();
		while (pathCursor.getNext(foundKey, foundData, LockMode.DEFAULT) == OperationStatus.SUCCESS) {
			try {
				processor.processPath(foundKey, foundData);
			} catch (Exception e) {
				pathCursor.close();
				throw e;
			}
		}
		pathCursor.close();
	}

	public void iterateTimeBranch(I_ProcessTimeBranchEntries processor)
			throws Exception {
		Cursor timeBranchCursor = getTimeBranchDb().openCursor(null, null);
		DatabaseEntry foundKey = processor.getKeyEntry();
		DatabaseEntry foundData = processor.getDataEntry();
		while (timeBranchCursor.getNext(foundKey, foundData, LockMode.DEFAULT) == OperationStatus.SUCCESS) {
			try {
				processor.processTimeBranch(foundKey, foundData);
			} catch (Exception e) {
				timeBranchCursor.close();
				throw e;
			}
		}
		timeBranchCursor.close();
	}

	public int uuidToNative(UUID uid) throws TerminologyException, IOException {
		if (uid.equals(originalStatedUuid) || uid.equals(additionalStatedUuid)) {
			return PrimordialId.STATED_CHARACTERISTIC_ID.getNativeId(Integer.MIN_VALUE);
		}
		if (uid.equals(originalInferredUuid) || uid.equals(additionalInferredUuid)) {
			return PrimordialId.INFERRED_CHARACTERISTIC_ID.getNativeId(Integer.MIN_VALUE);
		}
		
		I_IdVersioned id = getId(uid);
		if (id != null) {
			return id.getNativeId();
		}
		throw new NoMappingException("No id for: " + uid);
	}

	public int uuidToNative(Collection<UUID> uids) throws TerminologyException,
			IOException {
		I_IdVersioned id = getId(uids);
		if (id == null) {
			throw new NoMappingException("No id for: " + uids);
		}
		return id.getNativeId();
	}

	//TODO remove these after sufficient releases have gont by to ensure that the additional uids are no longer in use. 
	public static UUID originalStatedUuid = UUID.fromString("3fde38f6-e079-3cdc-a819-eda3ec74732d");
	public static UUID originalInferredUuid = UUID.fromString("d8fb4fb0-18c3-3352-9431-4919193f85bc");

	public static UUID additionalStatedUuid = UUID.fromString("7cf285d6-a3b7-11dc-8314-0800200c9a66");
	public static UUID additionalInferredUuid = UUID.fromString("1ebef994-a3b8-11dc-8314-0800200c9a66");

	public ThinIdVersioned getId(UUID uid) throws TerminologyException,
			IOException {
		ThinIdVersioned returnValue = getIdCore(uid);
		if (returnValue == null) {
			if (uid.equals(originalStatedUuid)) {
				returnValue = getIdCore(additionalStatedUuid);
			} else if (uid.equals(additionalStatedUuid)) {
				returnValue = getIdCore(originalStatedUuid);
			} else if (uid.equals(originalInferredUuid)) {
				returnValue = getIdCore(additionalInferredUuid);
			} else if (uid.equals(additionalInferredUuid)) {
				returnValue = getIdCore(originalInferredUuid);
			}
		}
		return returnValue;
	}

	private ThinIdVersioned getIdCore(UUID uid) throws ToIoException {
		Stopwatch timer = null;
		
		
		if (logger.isLoggable(Level.FINE)) {
			logger.fine("Getting nativeId : " + uid);
			timer = new Stopwatch();
			timer.start();
		}
		DatabaseEntry idKey = new DatabaseEntry();
		DatabaseEntry idValue = new DatabaseEntry();
		uuidBinding.objectToEntry(uid, idKey);
		try {
			if (getUidToIdMap().get(null, idKey, idValue, LockMode.DEFAULT) == OperationStatus.SUCCESS) {
				if (logger.isLoggable(Level.FINE)) {
					logger.fine("Got nativeId: " + uid + " elapsed time: "
							+ timer.getElapsedTime() / 1000 + " secs");
				}
				return (ThinIdVersioned) idBinding.entryToObject(idValue);
			}
		} catch (DatabaseException e) {
			throw new ToIoException(e);
		}
		return null;
	}

	public I_IdVersioned getId(Collection<UUID> uids)
			throws TerminologyException, IOException {
		Set<ThinIdVersioned> ids = new HashSet<ThinIdVersioned>(1);
		for (UUID uid : uids) {
			ThinIdVersioned thinId = getId(uid);
			if (thinId != null) {
				ids.add(thinId);
			}
		}
		if (ids.isEmpty()) {
			return null;
		} else if (ids.size() == 1) {
			return ids.iterator().next();
		}
		throw new TerminologyException("UIDs have multiple id records: " + ids
				+ " when getting for: " + uids);
	}

	public boolean hasId(Collection<UUID> uids) throws DatabaseException {
		for (UUID uid : uids) {
			if (hasId(uid)) {
				return true;
			}
		}
		return false;
	}

	public boolean hasId(UUID uid) throws DatabaseException {
		Stopwatch timer = null;
		if (logger.isLoggable(Level.FINE)) {
			logger.fine("Getting nativeId : " + uid);
			timer = new Stopwatch();
			timer.start();
		}
		DatabaseEntry idKey = new DatabaseEntry();
		DatabaseEntry idValue = new DatabaseEntry();
		uuidBinding.objectToEntry(uid, idKey);
		if (getUidToIdMap().get(null, idKey, idValue, LockMode.DEFAULT) == OperationStatus.SUCCESS) {
			if (logger.isLoggable(Level.FINE)) {
				logger.fine("Got nativeId: " + uid + " elapsed time: "
						+ timer.getElapsedTime() / 1000 + " secs");
			}
			return true;
		}
		return false;
	}

	public int uuidToNativeWithGeneration(UUID uid, int source, I_Path idPath,
			int version) throws TerminologyException, IOException {
		List<UUID> uids = new ArrayList<UUID>(1);
		uids.add(uid);
		return uuidToNativeWithGeneration(uids, source, idPath, version);
	}

	public int uuidToNativeWithGeneration(Collection<UUID> uids, int source,
			I_Path idPath, int version) throws TerminologyException,
			IOException {
		try {
			return uuidToNative(uids);
		} catch (TerminologyException e) {
			// create a new one...
			Cursor idCursor;
			try {
				idCursor = getIdDb().openCursor(null, null);
				DatabaseEntry foundKey = new DatabaseEntry();
				DatabaseEntry foundData = new DatabaseEntry();
				int lastId = Integer.MIN_VALUE;
				if (idCursor.getPrev(foundKey, foundData, LockMode.DEFAULT) == OperationStatus.SUCCESS) {
					lastId = (Integer) intBinder.entryToObject(foundKey);
				}
				idCursor.close();
				I_IdVersioned newId = new ThinIdVersioned(lastId + 1, 0);
				// AceLog.getLog().info("Last id: " + lastId + " NewId: " +
				// newId.getNativeId());
				ThinIdPart idPart = new ThinIdPart();
				for (UUID uid : uids) {
					idPart.setIdStatus(uuidToNativeWithGeneration(
							ArchitectonicAuxiliary.Concept.CURRENT.getUids(),
							source, idPath, version));
					idPart.setPathId(uuidToNativeWithGeneration(
							ArchitectonicAuxiliary.Concept.ARCHITECTONIC_BRANCH
									.getUids(), source, idPath, version));
					idPart.setSource(source);
					idPart.setSourceId(uid);
					idPart.setVersion(version);
					newId.addVersion(idPart);
				}

				DatabaseEntry idKey = new DatabaseEntry();
				DatabaseEntry idValue = new DatabaseEntry();
				intBinder.objectToEntry(newId.getNativeId(), idKey);
				idBinding.objectToEntry(newId, idValue);

				try {
					idPutSemaphore.acquire();
					idDb.put(null, idKey, idValue);
					idPutSemaphore.release();
				} catch (InterruptedException e2) {
					throw new DatabaseException(e2);
				}

				return newId.getNativeId();
			} catch (DatabaseException ex) {
				throw new ToIoException(ex);
			}
		}
	}

	public int uuidToNativeWithGeneration(UUID uid, int source,
			Collection<I_Path> idPaths, int version)
			throws TerminologyException, IOException {
		try {
			return uuidToNative(uid);
		} catch (NoMappingException e) {
			// create a new one...
			try {
				Cursor idCursor = getIdDb().openCursor(null, null);
				DatabaseEntry foundKey = new DatabaseEntry();
				DatabaseEntry foundData = new DatabaseEntry();
				int lastId = Integer.MIN_VALUE;
				if (idCursor.getPrev(foundKey, foundData, LockMode.DEFAULT) == OperationStatus.SUCCESS) {
					lastId = (Integer) intBinder.entryToObject(foundKey);
				}
				idCursor.close();
				I_IdVersioned newId = new ThinIdVersioned(lastId + 1, 0);
				// AceLog.getLog().info("Last id: " + lastId + " NewId: " +
				// newId.getNativeId());
				ThinIdPart idPart = new ThinIdPart();
				for (I_Path p : idPaths) {
					idPart.setIdStatus(uuidToNativeWithGeneration(
							ArchitectonicAuxiliary.Concept.CURRENT.getUids(),
							source, p, version));
					idPart.setPathId(uuidToNativeWithGeneration(
							ArchitectonicAuxiliary.Concept.ARCHITECTONIC_BRANCH
									.getUids(), source, p, version));
					idPart.setSource(source);
					idPart.setSourceId(uid);
					idPart.setVersion(version);
					newId.addVersion(idPart);
				}

				DatabaseEntry idKey = new DatabaseEntry();
				DatabaseEntry idValue = new DatabaseEntry();
				intBinder.objectToEntry(newId.getNativeId(), idKey);
				idBinding.objectToEntry(newId, idValue);
				try {
					idPutSemaphore.acquire();
					idDb.put(null, idKey, idValue);
					idPutSemaphore.release();
				} catch (InterruptedException ex) {
					throw new DatabaseException(ex);
				}
				return newId.getNativeId();
			} catch (DatabaseException e2) {
				throw new ToIoException(e2);
			}
		}
	}

	public Class<Integer> getNativeIdClass() {
		return Integer.class;
	}

	Semaphore idPutSemaphore = new Semaphore(1);

	public void writeId(I_IdVersioned id) throws DatabaseException {
		DatabaseEntry idKey = new DatabaseEntry();
		DatabaseEntry idValue = new DatabaseEntry();
		intBinder.objectToEntry(id.getNativeId(), idKey);
		idBinding.objectToEntry(id, idValue);
		if (logger.isLoggable(Level.FINE)) {
			logger.fine("Writing nativeId : " + id);
			for (I_IdPart p : id.getVersions()) {
				if (UUID.class.isAssignableFrom(p.getSourceId().getClass())) {
					UUID secondaryId = (UUID) p.getSourceId();
					try {
						int nid = uuidToNative(secondaryId);
						logger.fine("Found nid: " + nid + " for : "
								+ secondaryId);
					} catch (TerminologyException e) {
						logger.fine("No nid for : " + secondaryId);
					} catch (IOException e) {
						logger.fine("No nid for : " + secondaryId);
					}
				}
			}
		}
		try {
			idPutSemaphore.acquire();
			idDb.put(null, idKey, idValue);
			idPutSemaphore.release();
		} catch (InterruptedException e) {
			throw new DatabaseException(e);
		}
	}

	public void deleteId(I_IdVersioned id) throws DatabaseException {
		DatabaseEntry idKey = new DatabaseEntry();
		DatabaseEntry idValue = new DatabaseEntry();
		intBinder.objectToEntry(id.getNativeId(), idKey);
		idBinding.objectToEntry(id, idValue);
		idDb.delete(null, idKey);
	}

	public void writeImage(I_ImageVersioned image) throws DatabaseException {
		DatabaseEntry imageKey = new DatabaseEntry();
		DatabaseEntry imageValue = new DatabaseEntry();
		intBinder.objectToEntry(image.getImageId(), imageKey);
		imageBinder.objectToEntry(image, imageValue);
		imageDb.put(null, imageKey, imageValue);
	}

	public void writeConceptAttributes(I_ConceptAttributeVersioned concept)
			throws DatabaseException {

		int tupleCount = concept.getTuples().size();
		HashSet<I_ConceptAttributeTuple> tupleSet = new HashSet<I_ConceptAttributeTuple>(
				concept.getTuples());
		if (tupleCount != tupleSet.size()) {
			logger.severe("Tuples: " + concept.getTuples());
			logger.severe("Tuple set: " + tupleSet);
			throw new RuntimeException("Tuple set != tuple count...");
		}

		DatabaseEntry key = new DatabaseEntry();
		DatabaseEntry value = new DatabaseEntry();
		intBinder.objectToEntry(concept.getConId(), key);
		conBinding.objectToEntry(concept, value);
		conceptDb.put(null, key, value);
	}

	public void writeRel(I_RelVersioned rel) throws DatabaseException {
		DatabaseEntry key = new DatabaseEntry();
		DatabaseEntry value = new DatabaseEntry();
		if (debugWrites) {
			HashSet<I_RelPart> partSet = new HashSet<I_RelPart>(rel
					.getVersions());
			if (partSet.size() != rel.getVersions().size()) {
				throw new DatabaseException("Redundant parts: "
						+ rel.getVersions());
			} else {
				logger.info("rel parts same size: " + rel.getVersions().size());
			}
		}
		intBinder.objectToEntry(rel.getRelId(), key);
		relBinding.objectToEntry(rel, value);
		relDb.put(null, key, value);
	}

	public void writeExt(I_ThinExtByRefVersioned ext) throws DatabaseException,
			IOException {
		DatabaseEntry key = new DatabaseEntry();
		DatabaseEntry value = new DatabaseEntry();
		if (debugWrites) {
			if (componentToExtMap == null) {
				throw new DatabaseException(
						"componentToExtMap is not initialized. ");
			}
			if (refsetToExtMap == null) {
				throw new DatabaseException(
						"refsetToExtMap is not initialized. ");
			}
			HashSet<I_ThinExtByRefPart> partSet = new HashSet<I_ThinExtByRefPart>(
					ext.getVersions());
			if (partSet.size() != ext.getVersions().size()) {
				throw new DatabaseException("Redundant parts: "
						+ ext.getVersions());
			} else {
				logger.info("ext parts same size: " + ext.getVersions().size());
			}
		}
		intBinder.objectToEntry(ext.getMemberId(), key);
		extBinder.objectToEntry(ext, value);
		extensionDb.put(null, key, value);

		if (debugWrites) {
			I_ThinExtByRefVersioned ext2 = getExtension(ext.getMemberId());
			if (ext2.equals(ext)) {
				logger.fine("write/read test succeeded");
			} else {
				throw new DatabaseException("written and read are not equal: "
						+ ext + " " + ext2);
			}
			boolean foundByComponent = false;
			for (I_GetExtensionData extBean : getExtensionsForComponent(ext
					.getComponentId())) {
				try {
					if (ext2.equals(extBean.getExtension())) {
						foundByComponent = true;
						logger.fine("write/read for component test succeeded");
						break;
					}
				} catch (IOException e) {
					throw new DatabaseException(e);
				}
			}
			boolean foundByRefset = false;
			for (I_GetExtensionData extBean : getExtensionsForRefset((ext
					.getRefsetId()))) {
				try {
					if (ext2.equals(extBean.getExtension())) {
						foundByRefset = true;
						logger.fine("write/read for refset test succeeded");
						break;
					}
				} catch (IOException e) {
					throw new DatabaseException(e);
				}
			}
			if (foundByComponent == false || foundByRefset == false) {
				throw new DatabaseException(
						"Can't find extension by componentId: "
								+ foundByComponent + " by refset: "
								+ foundByRefset);
			}
		}
	}

	public void writeDescription(I_DescriptionVersioned desc)
			throws DatabaseException {
		try {
			IndexReader reader = IndexReader.open(luceneDir);
			reader.deleteDocuments(new Term("dnid", Integer.toString(desc
					.getDescId())));
			reader.close();
			IndexWriter writer = new IndexWriter(luceneDir,
					new StandardAnalyzer(), false);
			Document doc = new Document();
			doc.add(new Field("dnid", Integer.toString(desc.getDescId()),
					Field.Store.YES, Field.Index.UN_TOKENIZED));
			doc.add(new Field("cnid", Integer.toString(desc.getConceptId()),
					Field.Store.YES, Field.Index.UN_TOKENIZED));
			addIdsToIndex(doc, getId(desc.getDescId()));
			addIdsToIndex(doc, getId(desc.getConceptId()));

			String lastDesc = null;
			for (I_DescriptionTuple tuple : desc.getTuples()) {
				if (lastDesc == null
						|| lastDesc.equals(tuple.getText()) == false) {
					if (logger.isLoggable(Level.FINE)) {
						logger.fine("Adding to index. dnid:  "
								+ desc.getDescId() + " desc: "
								+ tuple.getText());
					}
					doc.add(new Field("desc", tuple.getText(), Field.Store.NO,
							Field.Index.TOKENIZED));
				}

			}
			writer.addDocument(doc);
			writer.close();
		} catch (CorruptIndexException e) {
			throw new DatabaseException(e);
		} catch (IOException e) {
			throw new DatabaseException(e);
		}
		DatabaseEntry key = new DatabaseEntry();
		DatabaseEntry value = new DatabaseEntry();
		intBinder.objectToEntry(desc.getDescId(), key);
		descBinding.objectToEntry(desc, value);
		descDb.put(null, key, value);
	}

	public void writePath(I_Path p) throws DatabaseException {
		DatabaseEntry key = new DatabaseEntry();
		DatabaseEntry value = new DatabaseEntry();
		intBinder.objectToEntry(p.getConceptId(), key);
		pathBinder.objectToEntry(p, value);
		pathDb.put(null, key, value);
		ACE.addUncommitted((I_Transact) p);
	}

	public I_Path getPath(int nativeId) throws DatabaseException {
		Stopwatch timer = null;
		if (logger.isLoggable(Level.FINE)) {
			logger.fine("Getting path : " + nativeId);
			timer = new Stopwatch();
			timer.start();
		}
		DatabaseEntry pathKey = new DatabaseEntry();
		DatabaseEntry pathValue = new DatabaseEntry();
		intBinder.objectToEntry(nativeId, pathKey);
		if (pathDb.get(null, pathKey, pathValue, LockMode.DEFAULT) == OperationStatus.SUCCESS) {
			if (logger.isLoggable(Level.FINE)) {
				logger.fine("Got path: " + nativeId + " elapsed time: "
						+ timer.getElapsedTime() / 1000 + " secs");
			}
			return (I_Path) pathBinder.entryToObject(pathValue);
		}
		throw new DatabaseException("Path: "
				+ ConceptBean.get(nativeId).toString() + " not found.");
	}

	public boolean hasPath(int nativeId) throws DatabaseException {
		Stopwatch timer = null;
		if (logger.isLoggable(Level.FINE)) {
			logger.fine("Getting path : " + nativeId);
			timer = new Stopwatch();
			timer.start();
		}
		DatabaseEntry pathKey = new DatabaseEntry();
		DatabaseEntry pathValue = new DatabaseEntry();
		intBinder.objectToEntry(nativeId, pathKey);
		if (pathDb.get(null, pathKey, pathValue, LockMode.DEFAULT) == OperationStatus.SUCCESS) {
			if (logger.isLoggable(Level.FINE)) {
				logger.fine("Got path: " + nativeId + " elapsed time: "
						+ timer.getElapsedTime() / 1000 + " secs");
			}
			return true;
		}
		return false;
	}

	public I_ImageVersioned getImage(UUID uid) throws TerminologyException,
			IOException, DatabaseException {
		return getImage(uuidToNative(uid));
	}

	public boolean hasImage(int imageId) throws DatabaseException {
		Stopwatch timer = null;
		if (logger.isLoggable(Level.FINE)) {
			logger.fine("Getting image : " + imageId);
			timer = new Stopwatch();
			timer.start();
		}
		DatabaseEntry imageKey = new DatabaseEntry();
		DatabaseEntry imageValue = new DatabaseEntry();
		intBinder.objectToEntry(imageId, imageKey);
		if (imageDb.get(null, imageKey, imageValue, LockMode.DEFAULT) == OperationStatus.SUCCESS) {
			if (logger.isLoggable(Level.FINE)) {
				logger.fine("Got image: " + imageId + " elapsed time: "
						+ timer.getElapsedTime() / 1000 + " secs");
			}
			return true;
		}
		return false;
	}

	public I_ImageVersioned getImage(int nativeId) throws DatabaseException {
		Stopwatch timer = null;
		if (logger.isLoggable(Level.FINE)) {
			logger.fine("Getting image : " + nativeId);
			timer = new Stopwatch();
			timer.start();
		}
		DatabaseEntry imageKey = new DatabaseEntry();
		DatabaseEntry imageValue = new DatabaseEntry();
		intBinder.objectToEntry(nativeId, imageKey);
		if (imageDb.get(null, imageKey, imageValue, LockMode.DEFAULT) == OperationStatus.SUCCESS) {
			I_ImageVersioned image = (I_ImageVersioned) imageBinder
					.entryToObject(imageValue);
			if (logger.isLoggable(Level.FINE)) {
				logger.fine("Got image: " + nativeId + " for concept: "
						+ ConceptBean.get(image.getConceptId())
						+ " elapsed time: " + timer.getElapsedTime() / 1000
						+ " secs");
			}
			return image;
		}
		throw new DatabaseException("Image for: " + nativeId + " not found.");
	}

	public Database getImageDb() {
		return imageDb;
	}

	public Database getPathDb() {
		return pathDb;
	}

	public List<I_ImageVersioned> getImages(int conceptId)
			throws DatabaseException {
		Stopwatch timer = null;
		if (logger.isLoggable(Level.FINE)) {
			logger.fine("Getting images for: " + conceptId);
			timer = new Stopwatch();
			timer.start();
		}
		DatabaseEntry secondaryKey = new DatabaseEntry();

		intBinder.objectToEntry(conceptId, secondaryKey);
		DatabaseEntry foundData = new DatabaseEntry();

		SecondaryCursor mySecCursor = getConceptImageMap().openSecondaryCursor(
				null, null);
		OperationStatus retVal = mySecCursor.getSearchKey(secondaryKey,
				foundData, LockMode.DEFAULT);
		List<I_ImageVersioned> matches = new ArrayList<I_ImageVersioned>();
		while (retVal == OperationStatus.SUCCESS) {
			ThinImageVersioned imageFromConceptId = (ThinImageVersioned) imageBinder
					.entryToObject(foundData);
			if (imageFromConceptId.getConceptId() == conceptId) {
				matches.add(imageFromConceptId);
			} else {
				break;
			}
			retVal = mySecCursor.getNextDup(secondaryKey, foundData,
					LockMode.DEFAULT);
		}
		mySecCursor.close();
		if (logger.isLoggable(Level.FINE)) {
			logger.fine("Images fetched for: " + conceptId + " elapsed time: "
					+ timer.getElapsedTime() / 1000 + " secs");
		}
		return matches;
	}

	public List<UUID> nativeToUuid(int nativeId) throws DatabaseException {
		Stopwatch timer = null;
		if (logger.isLoggable(Level.FINE)) {
			logger.fine("Getting id record for : " + nativeId);
			timer = new Stopwatch();
			timer.start();
		}
		DatabaseEntry idKey = new DatabaseEntry();
		DatabaseEntry idValue = new DatabaseEntry();
		intBinder.objectToEntry(nativeId, idKey);
		if (idDb.get(null, idKey, idValue, LockMode.DEFAULT) == OperationStatus.SUCCESS) {
			if (logger.isLoggable(Level.FINE)) {
				logger.fine("Got id record for: " + nativeId
						+ " elapsed time: " + timer.getElapsedTime() / 1000
						+ " secs");
			}
			return ((I_IdVersioned) idBinding.entryToObject(idValue)).getUIDs();
		}
		throw new DatabaseException("Concept: " + nativeId + " not found.");
	}

	public I_IdVersioned getId(int nativeId) throws IOException {
		I_IdVersioned id = getIdNullOk(nativeId);
		if (id != null) {
			return id;
		}
		throw new ToIoException(new DatabaseException("Concept: " + nativeId
				+ " not found."));
	}

	public I_IdVersioned getIdNullOk(int nativeId) throws IOException {
		Stopwatch timer = null;
		if (logger.isLoggable(Level.FINER)) {
			logger.finer("Getting id record for : " + nativeId);
			timer = new Stopwatch();
			timer.start();
		}
		DatabaseEntry idKey = new DatabaseEntry();
		DatabaseEntry idValue = new DatabaseEntry();
		intBinder.objectToEntry(nativeId, idKey);
		try {
			if (idDb.get(null, idKey, idValue, LockMode.DEFAULT) == OperationStatus.SUCCESS) {
				if (logger.isLoggable(Level.FINER)) {
					logger.finer("Got id record for: " + nativeId
							+ " elapsed time: " + timer.getElapsedTime() / 1000
							+ " secs");
				}
				return (I_IdVersioned) idBinding.entryToObject(idValue);
			}
		} catch (DatabaseException e) {
			new ToIoException(e);
		}
		return null;
	}

	public List<TimePathId> getTimePathList() throws Exception {
		TimePathCollector tpCollector = new TimePathCollector();
		iterateTimeBranch(tpCollector);
		return tpCollector.getTimePathIdList();
	}

	public List<I_Path> getPaths() throws Exception {
		PathCollector collector = new PathCollector();
		iteratePaths(collector);
		return collector.getPaths();
	}

	public int getMinId() throws DatabaseException {
		Cursor idCursor = getIdDb().openCursor(null, null);
		DatabaseEntry foundKey = new DatabaseEntry();
		DatabaseEntry foundData = new DatabaseEntry();
		int id = Integer.MAX_VALUE;
		if (idCursor.getNext(foundKey, foundData, LockMode.DEFAULT) == OperationStatus.SUCCESS) {
			id = (Integer) intBinder.entryToObject(foundKey);
		}
		idCursor.close();
		return id;
	}

	public int getMaxId() throws DatabaseException {
		Cursor idCursor = getIdDb().openCursor(null, null);
		DatabaseEntry foundKey = new DatabaseEntry();
		DatabaseEntry foundData = new DatabaseEntry();
		int id = Integer.MAX_VALUE;
		if (idCursor.getPrev(foundKey, foundData, LockMode.DEFAULT) == OperationStatus.SUCCESS) {
			id = (Integer) intBinder.entryToObject(foundKey);
		}
		idCursor.close();
		return id;
	}

	public void writeTimePath(TimePathId jarTimePath) throws DatabaseException {
		DatabaseEntry key = new DatabaseEntry();
		DatabaseEntry value = new DatabaseEntry();
		btBinder.objectToEntry(jarTimePath, key);
		tbBinder.objectToEntry(jarTimePath, value);
		timeBranchDb.put(null, key, value);
	}

	public void forget(I_GetConceptData concept) {
		try {
			AceLog.getEditLog().info("Forgetting: " + concept.getUids());
		} catch (IOException e) {
			AceLog.getEditLog().alertAndLogException(e);
		}
		ACE.removeUncommitted((I_Transact) concept);
	}

	public void forget(I_DescriptionVersioned desc) {
		throw new UnsupportedOperationException();

	}

	public void forget(I_RelVersioned rel) {
		throw new UnsupportedOperationException();

	}

	public LogWithAlerts getEditLog() {
		return AceLog.getEditLog();
	}

	public I_GetConceptData newConcept(UUID newConceptId, boolean defined,
			I_ConfigAceFrame aceFrameConfig) throws TerminologyException,
			IOException {
		canEdit(aceFrameConfig);
		int idSource = uuidToNative(ArchitectonicAuxiliary.Concept.UNSPECIFIED_UUID
				.getUids());
		int nid = uuidToNativeWithGeneration(newConceptId, idSource,
				aceFrameConfig.getEditingPathSet(), Integer.MAX_VALUE);
		AceLog.getEditLog().info(
				"Creating new concept: " + newConceptId + " (" + nid
						+ ") defined: " + defined);
		ConceptBean newBean = ConceptBean.get(nid);
		newBean.setPrimordial(true);
		int status = aceFrameConfig.getDefaultStatus().getConceptId();
		ThinConVersioned conceptAttributes = new ThinConVersioned(nid,
				aceFrameConfig.getEditingPathSet().size());
		for (I_Path p : aceFrameConfig.getEditingPathSet()) {
			ThinConPart attributePart = new ThinConPart();
			attributePart.setVersion(Integer.MAX_VALUE);
			attributePart.setDefined(defined);
			attributePart.setPathId(p.getConceptId());
			attributePart.setConceptStatus(status);
			conceptAttributes.addVersion(attributePart);
		}
		newBean.setUncommittedConceptAttributes(conceptAttributes);
		newBean.getUncommittedIds().add(nid);
		ACE.addUncommitted(newBean);
		return newBean;
	}

	public I_DescriptionVersioned newDescription(UUID newDescriptionId,
			I_GetConceptData concept, String lang, String text,
			I_ConceptualizeLocally descType, I_ConfigAceFrame aceFrameConfig)
			throws TerminologyException, IOException {
		canEdit(aceFrameConfig);
		ACE.addUncommitted((I_Transact) concept);
		int idSource = uuidToNative(ArchitectonicAuxiliary.Concept.UNSPECIFIED_UUID
				.getUids());
		int descId = uuidToNativeWithGeneration(newDescriptionId, idSource,
				aceFrameConfig.getEditingPathSet(), Integer.MAX_VALUE);
		if (AceLog.getEditLog().isLoggable(Level.FINE)) {
			AceLog.getEditLog().fine(
					"Creating new description: " + newDescriptionId + " ("
							+ descId + "): " + text);
		}
		ThinDescVersioned desc = new ThinDescVersioned(descId, concept
				.getConceptId(), aceFrameConfig.getEditingPathSet().size());
		boolean capStatus = false;
		int status = aceFrameConfig.getDefaultStatus().getConceptId();
		for (I_Path p : aceFrameConfig.getEditingPathSet()) {
			ThinDescPart descPart = new ThinDescPart();
			descPart.setVersion(Integer.MAX_VALUE);
			descPart.setPathId(p.getConceptId());
			descPart.setInitialCaseSignificant(capStatus);
			descPart.setLang(lang);
			descPart.setStatusId(status);
			descPart.setText(text);
			descPart.setTypeId(descType.getNid());
			desc.addVersion(descPart);
		}
		concept.getUncommittedDescriptions().add(desc);
		concept.getUncommittedIds().add(descId);
		return desc;
	}

	private void canEdit(I_ConfigAceFrame aceFrameConfig)
			throws TerminologyException {
		if (aceFrameConfig.getEditingPathSet().size() == 0) {
			throw new TerminologyException(
					"<br><br>You must select an editing path before editing...<br><br>No editing path selected.");
		}
	}

	public I_RelVersioned newRelationship(UUID newRelUid,
			I_GetConceptData concept, I_ConfigAceFrame aceFrameConfig)
			throws TerminologyException, IOException {
		canEdit(aceFrameConfig);
		if (aceFrameConfig.getHierarchySelection() == null) {
			throw new TerminologyException(
					"<br><br>To create a new relationship, you must<br>select the rel destination in the hierarchy view....");
		}
		int idSource = uuidToNative(ArchitectonicAuxiliary.Concept.UNSPECIFIED_UUID
				.getUids());
		int relId = uuidToNativeWithGeneration(newRelUid, idSource,
				aceFrameConfig.getEditingPathSet(), Integer.MAX_VALUE);
		if (AceLog.getEditLog().isLoggable(Level.FINE)) {
			AceLog.getEditLog().fine(
					"Creating new relationship 1: " + newRelUid + " (" + relId
							+ ") from " + concept.getUids() + " to "
							+ aceFrameConfig.getHierarchySelection().getUids());
		}
		ThinRelVersioned rel = new ThinRelVersioned(relId, concept
				.getConceptId(), aceFrameConfig.getHierarchySelection()
				.getConceptId(), 1);
		int status = aceFrameConfig.getDefaultStatus().getConceptId();
		for (I_Path p : aceFrameConfig.getEditingPathSet()) {
			ThinRelPart relPart = new ThinRelPart();
			relPart.setVersion(Integer.MAX_VALUE);
			relPart.setPathId(p.getConceptId());
			relPart.setStatusId(status);
			relPart.setRelTypeId(aceFrameConfig.getDefaultRelationshipType()
					.getConceptId());
			relPart.setCharacteristicId(aceFrameConfig
					.getDefaultRelationshipCharacteristic().getConceptId());
			relPart.setRefinabilityId(aceFrameConfig
					.getDefaultRelationshipRefinability().getConceptId());
			relPart.setGroup(0);
			rel.addVersion(relPart);
		}
		concept.getUncommittedSourceRels().add(rel);
		concept.getUncommittedIds().add(relId);
		ACE.addUncommitted((I_Transact) concept);
		return rel;

	}

	public I_RelVersioned newRelationship(UUID newRelUid,
			I_GetConceptData concept, I_GetConceptData relType,
			I_GetConceptData relDestination,
			I_GetConceptData relCharacteristic,
			I_GetConceptData relRefinability, I_GetConceptData relStatus,
			int relGroup, I_ConfigAceFrame aceFrameConfig)
			throws TerminologyException, IOException {
		canEdit(aceFrameConfig);
		int idSource = uuidToNative(ArchitectonicAuxiliary.Concept.UNSPECIFIED_UUID
				.getUids());

		int relId = uuidToNativeWithGeneration(newRelUid, idSource,
				aceFrameConfig.getEditingPathSet(), Integer.MAX_VALUE);
		if (AceLog.getEditLog().isLoggable(Level.FINE)) {
			AceLog.getEditLog().fine(
					"Creating new relationship 2: " + newRelUid + " (" + relId
							+ ") from " + concept.getUids() + " to "
							+ relDestination.getUids());
		}
		ThinRelVersioned rel = new ThinRelVersioned(relId, concept
				.getConceptId(), relDestination.getConceptId(), aceFrameConfig
				.getEditingPathSet().size());

		ThinRelPart relPart = new ThinRelPart();

		rel.addVersion(relPart);

		int status = relStatus.getConceptId();

		for (I_Path p : aceFrameConfig.getEditingPathSet()) {
			relPart.setVersion(Integer.MAX_VALUE);
			relPart.setPathId(p.getConceptId());
			relPart.setStatusId(status);
			relPart.setRelTypeId(relType.getConceptId());
			relPart.setCharacteristicId(relCharacteristic.getConceptId());
			relPart.setRefinabilityId(relRefinability.getConceptId());
			relPart.setGroup(relGroup);
		}
		concept.getUncommittedSourceRels().add(rel);
		concept.getUncommittedIds().add(relId);
		ACE.addUncommitted((I_Transact) concept);
		return rel;

	}

	public I_GetConceptData getConcept(Collection<UUID> ids)
			throws TerminologyException, IOException {
		return ConceptBean.get(ids);
	}

	public I_GetConceptData getConcept(UUID[] ids) throws TerminologyException,
			IOException {
		return ConceptBean.get(Arrays.asList(ids));
	}

	public I_Position newPosition(I_Path path, int version) {
		return new Position(version, path);
	}

	public I_IntSet newIntSet() {
		return new IntSet();
	}

	public void addUncommitted(I_GetConceptData concept) {
		ACE.addUncommitted((I_Transact) concept);
	}

	public void addUncommitted(I_ThinExtByRefVersioned extension) {
		ACE.addUncommitted(ExtensionByReferenceBean.make(extension
				.getMemberId(), extension));
	}

	public void loadFromSingleJar(String jarFile, String dataPrefix)
			throws Exception {
		LoadBdb.loadFromSingleJar(jarFile, dataPrefix);
	}

	public void loadFromDirectory(File dataDir) throws Exception {
		LoadBdb.loadFromDirectory(dataDir);
	}

	/**
	 * 
	 * @param args
	 * @throws Exception
	 * @deprecated use loadFromSingleJar
	 */
	public void loadFromMultipleJars(String[] args) throws Exception {
		LoadBdb.main(args);
	}

	private static class ProcessorWrapper implements
			I_ProcessDescriptionEntries, I_ProcessConceptAttributeEntries,
			I_ProcessRelationshipEntries, I_ProcessIdEntries,
			I_ProcessImageEntries, I_ProcessPathEntries,
			I_ProcessExtByRefEntries {

		I_ProcessConceptAttributes conceptAttributeProcessor;

		I_ProcessDescriptions descProcessor;

		I_ProcessRelationships relProcessor;

		I_ProcessIds idProcessor;

		I_ProcessImages imageProcessor;

		I_ProcessPaths pathProcessor;

		I_ProcessExtByRef extProcessor;

		public ProcessorWrapper() {
			super();
		}

		public void processDesc(DatabaseEntry key, DatabaseEntry value)
				throws Exception {
			I_DescriptionVersioned desc = (I_DescriptionVersioned) descBinding
					.entryToObject(value);
			descProcessor.processDescription(desc);
		}

		public void processConceptAttributeEntry(DatabaseEntry key,
				DatabaseEntry value) throws Exception {
			I_ConceptAttributeVersioned conc = (I_ConceptAttributeVersioned) conBinding
					.entryToObject(value);
			conceptAttributeProcessor.processConceptAttributes(conc);
		}

		public void processRel(DatabaseEntry key, DatabaseEntry value)
				throws Exception {
			I_RelVersioned rel = (I_RelVersioned) relBinding
					.entryToObject(value);
			relProcessor.processRelationship(rel);
		}

		public DatabaseEntry getDataEntry() {
			return new DatabaseEntry();
		}

		public DatabaseEntry getKeyEntry() {
			return new DatabaseEntry();
		}

		public I_ProcessConceptAttributes getConceptAttributeProcessor() {
			return conceptAttributeProcessor;
		}

		public void setConceptAttributeProcessor(
				I_ProcessConceptAttributes conceptAttributeProcessor) {
			this.conceptAttributeProcessor = conceptAttributeProcessor;
		}

		public I_ProcessDescriptions getDescProcessor() {
			return descProcessor;
		}

		public void setDescProcessor(I_ProcessDescriptions descProcessor) {
			this.descProcessor = descProcessor;
		}

		public I_ProcessRelationships getRelProcessor() {
			return relProcessor;
		}

		public void setRelProcessor(I_ProcessRelationships relProcessor) {
			this.relProcessor = relProcessor;
		}

		public org.dwfa.ace.api.I_ProcessIds getIdProcessor() {
			return idProcessor;
		}

		public void setIdProcessor(org.dwfa.ace.api.I_ProcessIds processor) {
			this.idProcessor = processor;
		}

		public void processId(DatabaseEntry key, DatabaseEntry value)
				throws Exception {
			I_IdVersioned idv = (I_IdVersioned) idBinding.entryToObject(value);
			this.idProcessor.processId(idv);
		}

		public I_ProcessImages getImageProcessor() {
			return imageProcessor;
		}

		public void setImageProcessor(I_ProcessImages imageProcessor) {
			this.imageProcessor = imageProcessor;
		}

		public void processImages(DatabaseEntry key, DatabaseEntry value)
				throws Exception {
			I_ImageVersioned imageV = (I_ImageVersioned) imageBinder
					.entryToObject(value);
			this.imageProcessor.processImages(imageV);
		}

		public I_ProcessPaths getPathProcessor() {
			return pathProcessor;
		}

		public void setPathProcessor(I_ProcessPaths pathProcessor) {
			this.pathProcessor = pathProcessor;
		}

		public void processPath(DatabaseEntry key, DatabaseEntry value)
				throws Exception {
			I_Path path = (I_Path) pathBinder.entryToObject(value);
			this.pathProcessor.processPath(path);
		}

		public void setExtProcessor(I_ProcessExtByRef extProcessor) {
			this.extProcessor = extProcessor;
		}

		public void processEbr(DatabaseEntry key, DatabaseEntry value)
				throws Exception {
			I_ThinExtByRefVersioned extension = (I_ThinExtByRefVersioned) extBinder
					.entryToObject(value);
			this.extProcessor.processExtensionByReference(extension);
		}

	}

	public static class ConceptIteratorWrapper implements
			I_ProcessConceptAttributeEntries {

		I_ProcessConcepts processor;

		public ConceptIteratorWrapper(I_ProcessConcepts processor) {
			super();
			this.processor = processor;
		}

		public void processConceptAttributeEntry(DatabaseEntry key,
				DatabaseEntry value) throws Exception {
			I_ConceptAttributeVersioned conAttrVersioned = (I_ConceptAttributeVersioned) conBinding
					.entryToObject(value);
			ConceptBean bean = ConceptBean.get(conAttrVersioned.getConId());
			processor.processConcept(bean);
		}

		public DatabaseEntry getDataEntry() {
			return new DatabaseEntry();
		}

		public DatabaseEntry getKeyEntry() {
			return new DatabaseEntry();
		}

	}

	public void iterateConceptAttributes(I_ProcessConceptAttributes processor)
			throws Exception {
		ProcessorWrapper wrapper = new ProcessorWrapper();
		wrapper.setConceptAttributeProcessor(processor);
		iterateConceptAttributeEntries(wrapper);
	}

	private class DescriptionIterator implements
			Iterator<I_DescriptionVersioned> {

		DatabaseEntry foundKey = new DatabaseEntry();

		DatabaseEntry foundData = new DatabaseEntry();

		boolean hasNext;

		private I_DescriptionVersioned desc;

		private Cursor descCursor;

		private DescriptionIterator() throws IOException {
			super();
			try {
				descCursor = getDescDb().openCursor(null, null);
				getNext();
			} catch (DatabaseException e) {
				throw new ToIoException(e);
			}
		}

		private void getNext() {
			try {
				hasNext = (descCursor.getNext(foundKey, foundData,
						LockMode.DEFAULT) == OperationStatus.SUCCESS);
				if (hasNext) {
					desc = (I_DescriptionVersioned) descBinding
							.entryToObject(foundData);
				} else {
					desc = null;
					descCursor.close();
				}
			} catch (Exception ex) {
				try {
					descCursor.close();
				} catch (DatabaseException e) {
					AceLog.getAppLog().alertAndLogException(ex);
				}
				AceLog.getAppLog().alertAndLogException(ex);
				hasNext = false;
			}
		}

		public boolean hasNext() {
			return hasNext;
		}

		public I_DescriptionVersioned next() {
			if (hasNext) {
				I_DescriptionVersioned next = desc;
				getNext();
				return next;
			}
			return null;
		}

		public void remove() {
			throw new UnsupportedOperationException();
		}

	}

	public Iterator<I_DescriptionVersioned> getDescriptionIterator()
			throws IOException {
		return new DescriptionIterator();
	}

	public void iterateDescriptions(I_ProcessDescriptions processor)
			throws Exception {
		ProcessorWrapper wrapper = new ProcessorWrapper();
		wrapper.setDescProcessor(processor);
		iterateDescriptionEntries(wrapper);
	}

	public void iterateIds(org.dwfa.ace.api.I_ProcessIds processor)
			throws Exception {
		ProcessorWrapper wrapper = new ProcessorWrapper();
		wrapper.setIdProcessor(processor);
		iterateIdEntries(wrapper);
	}

	public void iterateImages(I_ProcessImages processor) throws Exception {
		ProcessorWrapper wrapper = new ProcessorWrapper();
		wrapper.setImageProcessor(processor);
		iterateImages(wrapper);

	}

	public void iteratePaths(I_ProcessPaths processor) throws Exception {
		ProcessorWrapper wrapper = new ProcessorWrapper();
		wrapper.setPathProcessor(processor);
		iteratePaths(wrapper);
	}

	public void iterateRelationships(I_ProcessRelationships processor)
			throws Exception {
		ProcessorWrapper wrapper = new ProcessorWrapper();
		wrapper.setRelProcessor(processor);
		iterateRelationships(wrapper);
	}

	public void iterateConcepts(I_ProcessConcepts processor) throws Exception {
		ConceptIteratorWrapper wrapper = new ConceptIteratorWrapper(processor);
		iterateConceptAttributeEntries(wrapper);
	}

	public void iterateExtByRefs(I_ProcessExtByRef processor) throws Exception {
		ProcessorWrapper wrapper = new ProcessorWrapper();
		wrapper.setExtProcessor(processor);
		iterateExtByRefEntries(wrapper);
	}

	private void writeTolicitIndex(String word, String type) throws IOException {
		if (licitIndexWriter == null) {
			licitIndexWriter = new IndexWriter(licitWordsDir,
					new StandardAnalyzer(), true);
			licitIndexWriter.setUseCompoundFile(true);
			licitIndexWriter.setMergeFactor(10000);
		}
		Document doc = new Document();
		doc.add(new Field("type", type, Field.Store.YES,
				Field.Index.UN_TOKENIZED));
		doc.add(new Field("word", word, Field.Store.NO, Field.Index.TOKENIZED));
		licitIndexWriter.addDocument(doc);
	}

	public void writeIllicitWord(String word) throws IOException {
		writeTolicitIndex(word, "i");
	}

	public void writeLicitWord(String word) throws IOException {
		writeTolicitIndex(word, "l");
	}

	public void optimizeLicitWords() throws IOException {
		if (licitIndexWriter != null) {
			licitIndexWriter.optimize();
		}
	}

	public Hits searchLicitWords(String query) throws IOException,
			ParseException {
		query = "type:\"l\" " + query;
		return doLicitSearch(query);
	}

	public Hits doLicitSearch(String query) throws IOException, ParseException {
		if (luceneLicitSearcher == null) {
			luceneLicitSearcher = new IndexSearcher(licitWordsDir
					.getAbsolutePath());
		}
		Query q = new QueryParser("word", new StandardAnalyzer()).parse(query);
		return luceneLicitSearcher.search(q);
	}

	public Hits searchIllicitWords(String query) throws IOException,
			ParseException {
		query = "type:\"i\" " + query;
		return doLicitSearch(query);
	}

	public void checkpoint() throws IOException {
		this.sync();
	}

	public Hits doLuceneSearch(String query) throws IOException, ParseException {
		if (luceneDir.exists() == false) {
			createLuceneDescriptionIndex();
		}
		if (luceneSearcher == null) {
			luceneSearcher = new IndexSearcher(luceneDir.getAbsolutePath());
		}
		Query q = new QueryParser("desc", new StandardAnalyzer()).parse(query);
		return luceneSearcher.search(q);
	}

	public I_GetConceptData getConcept(int nativeId)
			throws TerminologyException, IOException {
		return ConceptBean.get(nativeId);
	}

	public Collection<UUID> getUids(int nativeId) throws TerminologyException,
			IOException {
		try {
			return nativeToUuid(nativeId);
		} catch (DatabaseException e) {
			throw new ToIoException(e);
		}
	}

	public I_Path getPath(Collection<UUID> uids) throws TerminologyException,
			IOException {
		try {
			return getPath(uuidToNative(uids));
		} catch (DatabaseException e) {
			throw new ToIoException(e);
		}
	}

	public I_Path getPath(UUID[] uuids) throws TerminologyException,
			IOException {
		try {
			return getPath(uuidToNative(Arrays.asList(uuids)));
		} catch (DatabaseException e) {
			throw new ToIoException(e);
		}
	}

	public I_Path newPath(Set<I_Position> origins, I_GetConceptData pathConcept)
			throws TerminologyException, IOException {
		Path newPath = new Path(pathConcept.getConceptId(),
				new ArrayList<I_Position>(origins));
		AceLog.getEditLog().fine("writing new path: \n" + newPath);
		try {
			AceConfig.getVodb().writePath(newPath);
		} catch (DatabaseException e) {
			throw new ToIoException(e);
		}
		return newPath;
	}

	public void commit() throws Exception {
		ACE.commit();
	}

	public void cancel() throws IOException {
		ACE.abort();
	}

	public void addChangeSetWriter(I_WriteChangeSet csw) {
		ACE.getCsWriters().add(csw);

	}

	public I_ReadChangeSet newBinaryChangeSetReader(File changeSetFile) {
		BinaryChangeSetReader bcs = new BinaryChangeSetReader();
		bcs.setChangeSetFile(changeSetFile);
		return bcs;
	}

	public I_WriteChangeSet newBinaryChangeSetWriter(File changeSetFile) {
		File tempChangeSetFile = new File(changeSetFile.getParentFile(),
				changeSetFile.getName() + ".temp");
		BinaryChangeSetWriter bcs = new BinaryChangeSetWriter(changeSetFile,
				tempChangeSetFile);
		return bcs;
	}

	public void removeChangeSetWriter(I_WriteChangeSet csw) {
		ACE.getCsWriters().remove(csw);
	}

	public void closeChangeSets() throws IOException {
		for (I_WriteChangeSet cs : ACE.getCsWriters()) {
			cs.commit();
		}
		ACE.getCsWriters().clear();
	}

	public I_ConfigAceFrame newAceFrameConfig() throws TerminologyException,
			IOException {
		return new AceFrameConfig();
	}

	I_ConfigAceFrame activeAceFrameConfig;

	public I_ConfigAceFrame getActiveAceFrameConfig()
			throws TerminologyException, IOException {
		return activeAceFrameConfig;
	}

	public void setActiveAceFrameConfig(I_ConfigAceFrame activeAceFrameConfig)
			throws TerminologyException, IOException {
		this.activeAceFrameConfig = activeAceFrameConfig;
	}

	public I_ConfigAceDb newAceDbConfig() {
		return new AceConfig(envHome, readOnly, cacheSize);
	}

	public long convertToThickVersion(int version) {
		return ThinVersionHelper.convert(version);
	}

	public int convertToThinVersion(long time) {
		return ThinVersionHelper.convert(time);
	}

	public int convertToThinVersion(String dateStr)
			throws java.text.ParseException {
		return ThinVersionHelper.convert(dateStr);
	}

	public I_IntList newIntList() {
		return new IntList();
	}

	public void resumeChangeSetWriters() {
		ACE.resumeChangeSetWriters();

	}

	public void suspendChangeSetWriters() {
		ACE.suspendChangeSetWriters();

	}

	public static boolean getPreloadRels() {
		return preloadRels;
	}

	public static void setPreloadRels(boolean preloadRels) {
		VodbEnv.preloadRels = preloadRels;
	}

	public static boolean getPreloadDescriptions() {
		return preloadDescriptions;
	}

	public static void setPreloadDescriptions(boolean preloadDescriptions) {
		VodbEnv.preloadDescriptions = preloadDescriptions;
	}

	public static Long getCacheSize() {
		return cacheSize;
	}

	public static void setCacheSize(Long cacheSize) {
		VodbEnv.cacheSize = cacheSize;
	}

	public I_ConceptAttributePart newConceptAttributePart() {
		return new ThinConPart();
	}

	public I_DescriptionPart newDescriptionPart() {
		return new ThinDescPart();
	}

	public I_RelPart newRelPart() {
		return new ThinRelPart();
	}

	public I_ThinExtByRefPartBoolean newBooleanExtensionPart() {
		return new ThinExtByRefPartBoolean();
	}

	public I_ThinExtByRefPartConcept newConceptExtensionPart() {
		return new ThinExtByRefPartConcept();
	}

	public I_ThinExtByRefVersioned newExtension(int refsetId, int memberId,
			int componentId, int typeId) {
		ThinExtByRefVersioned thinEbr = new ThinExtByRefVersioned(refsetId,
				memberId, componentId, typeId);
		ExtensionByReferenceBean ebrBean = ExtensionByReferenceBean.makeNew(
				memberId, thinEbr);
		ACE.addUncommitted(ebrBean);
		return thinEbr;
	}

	public I_ThinExtByRefVersioned newExtensionBypassCommit(int refsetId,
			int memberId, int componentId, int typeId) {
		ThinExtByRefVersioned thinEbr = new ThinExtByRefVersioned(refsetId,
				memberId, componentId, typeId);
		return thinEbr;
	}

	public I_ThinExtByRefPartInteger newIntegerExtensionPart() {
		return new ThinExtByRefPartInteger();
	}

	public I_ThinExtByRefPartLanguage newLanguageExtensionPart() {
		return new ThinExtByRefPartLanguage();
	}

	public I_ThinExtByRefPartLanguageScoped newLanguageScopedExtensionPart() {
		return new ThinExtByRefPartLanguageScoped();
	}

	public I_ThinExtByRefPartMeasurement newMeasurementExtensionPart() {
		return new ThinExtByRefPartMeasurement();
	}

	public I_ThinExtByRefPartString newStringExtensionPart() {
		return new ThinExtByRefPartString();
	}

	public Set<I_Transact> getUncommitted() {
		return ACE.getUncommitted();
	}

	public I_ThinExtByRefPartConceptInt newConceptIntExtensionPart() {
		return new ThinExtByRefPartConceptInt();
	}

	public I_GetExtensionData getExtensionWrapper(int nid) throws IOException {
		return ExtensionByReferenceBean.get(nid);
	}

	public static boolean isTransactional() {
		return transactional;
	}

}
