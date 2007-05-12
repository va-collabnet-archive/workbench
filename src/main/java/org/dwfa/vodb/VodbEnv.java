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
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import org.dwfa.ace.ACE;
import org.dwfa.ace.AceLog;
import org.dwfa.ace.activity.ActivityPanel;
import org.dwfa.ace.activity.ActivityViewer;
import org.dwfa.ace.api.I_ConceptAttributePart;
import org.dwfa.ace.api.I_ConceptAttributeVersioned;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_DescriptionPart;
import org.dwfa.ace.api.I_DescriptionVersioned;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IdVersioned;
import org.dwfa.ace.api.I_ImageVersioned;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_Path;
import org.dwfa.ace.api.I_Position;
import org.dwfa.ace.api.I_RelPart;
import org.dwfa.ace.api.I_RelTuple;
import org.dwfa.ace.api.I_RelVersioned;
import org.dwfa.ace.api.TimePathId;
import org.dwfa.ace.config.AceConfig;
import org.dwfa.ace.search.I_TrackContinuation;
import org.dwfa.bpa.util.Stopwatch;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.cement.PrimordialId;
import org.dwfa.tapi.NoMappingException;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.tapi.impl.LocalFixedTerminology;
import org.dwfa.vodb.bind.BranchTimeBinder;
import org.dwfa.vodb.bind.PathBinder;
import org.dwfa.vodb.bind.ThinConVersionedBinding;
import org.dwfa.vodb.bind.ThinDescVersionedBinding;
import org.dwfa.vodb.bind.ThinIdVersionedBinding;
import org.dwfa.vodb.bind.ThinImageBinder;
import org.dwfa.vodb.bind.ThinRelVersionedBinding;
import org.dwfa.vodb.bind.TimePathIdBinder;
import org.dwfa.vodb.bind.UuidBinding;
import org.dwfa.vodb.jar.PathCollector;
import org.dwfa.vodb.jar.TimePathCollector;
import org.dwfa.vodb.types.ConceptBean;
import org.dwfa.vodb.types.I_ProcessConcepts;
import org.dwfa.vodb.types.I_ProcessDescriptions;
import org.dwfa.vodb.types.I_ProcessIds;
import org.dwfa.vodb.types.I_ProcessImages;
import org.dwfa.vodb.types.I_ProcessPaths;
import org.dwfa.vodb.types.I_ProcessRelationships;
import org.dwfa.vodb.types.I_ProcessTimeBranch;
import org.dwfa.vodb.types.Path;
import org.dwfa.vodb.types.ThinDescVersioned;
import org.dwfa.vodb.types.ThinIdPart;
import org.dwfa.vodb.types.ThinIdVersioned;
import org.dwfa.vodb.types.ThinImageVersioned;
import org.dwfa.vodb.types.ThinRelVersioned;

import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.je.Cursor;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
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
public class VodbEnv {
	private static Logger logger = Logger.getLogger(VodbEnv.class.getName());

	private Environment env;

	private Database conceptDb;

	private Database relDb;

	private Database descDb;

	private Database idDb;

	private Database imageDb;

	private Database pathDb;

	private SecondaryDatabase conceptDescMap;

	private SecondaryDatabase c1RelMap;

	private SecondaryDatabase c2RelMap;

	private SecondaryDatabase uidToIdMap;

	private boolean readOnly;

	static ThinDescVersionedBinding descBinding = new ThinDescVersionedBinding();

	static ThinRelVersionedBinding relBinding = new ThinRelVersionedBinding();

	static ThinConVersionedBinding conBinding = new ThinConVersionedBinding();

	static PathBinder pathBinder = new PathBinder();

	TupleBinding intBinder = TupleBinding.getPrimitiveBinding(Integer.class);

	ConceptIdKeyForDescCreator descForConceptKeyCreator = new ConceptIdKeyForDescCreator(
			descBinding);

	C2KeyForRelCreator c2KeyCreator = new C2KeyForRelCreator(relBinding);

	C1KeyForRelCreator c1KeyCreator = new C1KeyForRelCreator(relBinding);

	ThinIdVersionedBinding idBinding = new ThinIdVersionedBinding();

	BranchTimeBinder btBinder = new BranchTimeBinder();

	TimePathIdBinder tbBinder = new TimePathIdBinder();

	ThinImageBinder imageBinder = new ThinImageBinder();

	UuidBinding uuidBinding = new UuidBinding();

	int descCount = -1;

	private Database timeBranchDb;

	private SecondaryDatabase conceptImageMap;

	public VodbEnv() {

	}
	private class StartupListener implements AWTEventListener {

		public void eventDispatched(AWTEvent event) {
			KeyEvent ke = (KeyEvent) event;
			if (ke.getKeyCode() == KeyEvent.VK_ESCAPE) {
				activity.setProgressInfoUpper("Loading the terminology (preload false)");
				preloadRels = false;
				preloadDescriptions = false;
				Toolkit.getDefaultToolkit().removeAWTEventListener(this);
			}
			
		}
		
	}

	boolean preloadRels = false;
	boolean preloadDescriptions = true;

	private ActivityPanel activity;
	/**
	 * @todo find out of all secondary databases have to be opened when the
	 * primary is opened? How do they get updated, etc?
	 */
	public void setup(File envHome, boolean readOnly, Long cacheSize) throws DatabaseException {
		activity = new ActivityPanel(true, true);
		StartupListener l = new StartupListener();
		Toolkit.getDefaultToolkit().addAWTEventListener(l, AWTEvent.KEY_EVENT_MASK);

		AceLog.getAppLog().info("Setting up db: " + envHome);
		activity.setIndeterminate(true);
		activity.setProgressInfoUpper("Loading the terminology");
		activity.setProgressInfoLower("Setting up the environment...");
		activity.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
	        	System.out.println("System.exit from activity action listener: " + e.getActionCommand());
				System.exit(0);
			}
		});
		try {
			ActivityViewer.addActivity(activity);
		} catch (Exception e1) {
			AceLog.getAppLog().alertAndLogException(e1);
		}
		
		
		this.readOnly = readOnly;
		LocalFixedTerminology.setStore(new VodbFixedServer(this));
		envHome.mkdirs();

		EnvironmentConfig envConfig = new EnvironmentConfig();
		if (cacheSize != null) {
			envConfig.setCacheSize(cacheSize);
			AceLog.getAppLog().info("Setting cache size to: " + cacheSize);
		}
		activity.setProgressInfoLower("Setting cache size to: " + cacheSize);

		envConfig.setReadOnly(readOnly);
		envConfig.setAllowCreate(!readOnly);
		env = new Environment(envHome, envConfig);
		DatabaseConfig conceptDbConfig = makeConfig(readOnly);
		activity.setProgressInfoLower("Opening concepts...");
		conceptDb = env.openDatabase(null, "concept", conceptDbConfig);

		DatabaseConfig relDbConfig = makeConfig(readOnly);
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
		DatabaseConfig descDbConfig = makeConfig(readOnly);
		descDb = env.openDatabase(null, "desc", descDbConfig);
		activity.setProgressInfoLower("Opening descriptions...");
		if (preloadDescriptions) {
			activity.setProgressInfoLower("Loading descriptions...");
			PreloadConfig descPreloadConfig = new PreloadConfig();
			descPreloadConfig.setLoadLNs(true);
			descDb.preload(descPreloadConfig);			
		}
		getConceptDescMap();

		DatabaseConfig mapDbConfig = makeConfig(readOnly);
		activity.setProgressInfoLower("Opening ids...");
		idDb = env.openDatabase(null, "idDb", mapDbConfig);
		uidToIdMap = createUidToIdMap();
		
		//Reset the authority id so that each time the db starts, it gets a new authorityId. 
		PrimordialId primId = PrimordialId.AUTHORITY_ID;
		I_IdVersioned thinId = new ThinIdVersioned(primId.getNativeId(Integer.MIN_VALUE), 1);
		ThinIdPart idPart = new ThinIdPart();
		idPart.setIdStatus(PrimordialId.CURRENT_ID.getNativeId(Integer.MIN_VALUE));
		idPart.setPathId(PrimordialId.ACE_AUXILIARY_ID.getNativeId(Integer.MIN_VALUE));
		idPart.setSource(PrimordialId.ACE_AUX_ENCODING_ID.getNativeId(Integer.MIN_VALUE));
		idPart.setSourceId(UUID.randomUUID());
		idPart.setVersion(Integer.MIN_VALUE);
		thinId.addVersion(idPart);
		writeId(thinId);
		


		DatabaseConfig imageDbConfig = makeConfig(readOnly);
		activity.setProgressInfoLower("Opening images...");
		imageDb = env.openDatabase(null, "imageDb", imageDbConfig);
		getConceptImageMap();

		DatabaseConfig timeBranchDbConfig = makeConfig(readOnly);
		activity.setProgressInfoLower("Opening time branches...");
		timeBranchDb = env.openDatabase(null, "timeBranchDb",
				timeBranchDbConfig);

		DatabaseConfig pathDbConfig = makeConfig(readOnly);
		activity.setProgressInfoLower("Opening paths...");
		pathDb = env.openDatabase(null, "pathDb", pathDbConfig);

		AceLog.getAppLog().info("Cache percent: " + envConfig.getCachePercent());
		AceLog.getAppLog().info("Cache size: " + envConfig.getCacheSize());
		
		activity.setProgressInfoLower("complete");
		activity.complete();
		Toolkit.getDefaultToolkit().removeAWTEventListener(l);
	}

	private void createConceptDescMap() throws DatabaseException {
		ConceptIdKeyForDescCreator descConceptKeyCreator = new ConceptIdKeyForDescCreator(
				descBinding);

		SecondaryConfig descByConceptIdConfig = new SecondaryConfig();
		descByConceptIdConfig.setReadOnly(readOnly);
		descByConceptIdConfig.setDeferredWrite(true);
		descByConceptIdConfig.setAllowCreate(!readOnly);
		descByConceptIdConfig.setSortedDuplicates(false);
		descByConceptIdConfig.setKeyCreator(descConceptKeyCreator);
		descByConceptIdConfig.setAllowPopulate(true);

		conceptDescMap = env.openSecondaryDatabase(null, "conceptDescMap",
				descDb, descByConceptIdConfig);
	}

	public void createC1RelMap() throws DatabaseException {
		C1KeyForRelCreator c1ToRelKeyCreator = new C1KeyForRelCreator(
				relBinding);

		SecondaryConfig relByC1IdConfig = new SecondaryConfig();
		relByC1IdConfig.setReadOnly(readOnly);
		relByC1IdConfig.setDeferredWrite(true);
		relByC1IdConfig.setAllowCreate(!readOnly);
		relByC1IdConfig.setSortedDuplicates(false);
		relByC1IdConfig.setKeyCreator(c1ToRelKeyCreator);
		relByC1IdConfig.setAllowPopulate(true);

		c1RelMap = env.openSecondaryDatabase(null, "c1RelMap", relDb,
				relByC1IdConfig);
	}

	public void createConceptImageMap() throws DatabaseException {
		ConceptKeyForImageCreator concToImageKeyCreator = new ConceptKeyForImageCreator();

		SecondaryConfig imageByConConfig = new SecondaryConfig();
		imageByConConfig.setReadOnly(readOnly);
		imageByConConfig.setDeferredWrite(true);
		imageByConConfig.setAllowCreate(!readOnly);
		imageByConConfig.setSortedDuplicates(true);
		imageByConConfig.setKeyCreator(concToImageKeyCreator);
		imageByConConfig.setAllowPopulate(true);

		conceptImageMap = env.openSecondaryDatabase(null, "conceptImageMap",
				imageDb, imageByConConfig);
	}

	public void createC2RelMap() throws DatabaseException {
		C2KeyForRelCreator c2ToRelKeyCreator = new C2KeyForRelCreator(
				relBinding);

		SecondaryConfig relByC2IdConfig = new SecondaryConfig();
		relByC2IdConfig.setReadOnly(readOnly);
		relByC2IdConfig.setDeferredWrite(true);
		relByC2IdConfig.setAllowCreate(!readOnly);
		relByC2IdConfig.setSortedDuplicates(false);
		relByC2IdConfig.setKeyCreator(c2ToRelKeyCreator);
		relByC2IdConfig.setAllowPopulate(true);

		c2RelMap = env.openSecondaryDatabase(null, "c2RelMap", relDb,
				relByC2IdConfig);
	}

	public void createIdMaps() throws DatabaseException {
		if (uidToIdMap == null) {
			uidToIdMap = createUidToIdMap();
		}
	}

	private SecondaryDatabase createUidToIdMap() throws DatabaseException {
		SecondaryConfig uidToIdMapConfig = new SecondaryConfig();
		uidToIdMapConfig.setReadOnly(readOnly);
		uidToIdMapConfig.setDeferredWrite(true);
		uidToIdMapConfig.setAllowCreate(!readOnly);
		uidToIdMapConfig.setSortedDuplicates(false);
		uidToIdMapConfig.setMultiKeyCreator(new UidKeyCreator(uuidBinding,
				idBinding));
		uidToIdMapConfig.setAllowPopulate(true);
		return env.openSecondaryDatabase(null, "uidToIdMap", getIdDb(),
				uidToIdMapConfig);
	}

	private DatabaseConfig makeConfig(boolean readOnly) {
		DatabaseConfig dbConfig = new DatabaseConfig();
		dbConfig.setReadOnly(readOnly);
		dbConfig.setAllowCreate(!readOnly);
		dbConfig.setDeferredWrite(true);
		dbConfig.setSortedDuplicates(false);
		return dbConfig;
	}

	public Environment getEnv() {
		return env;
	}

	public void sync() throws DatabaseException {
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
			if (!env.getConfig().getReadOnly()) {
				env.sync();
			}
		}

	}

	public void close() {
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

	public I_ConceptAttributeVersioned getConcept(int conceptId) throws DatabaseException {
		Stopwatch timer = null;
		if (logger.isLoggable(Level.FINE)) {
			logger.fine("Getting concept : " + conceptId);
			timer = new Stopwatch();
			timer.start();
		}
		DatabaseEntry conceptKey = new DatabaseEntry();
		DatabaseEntry conceptValue = new DatabaseEntry();
		intBinder.objectToEntry(conceptId, conceptKey);
		if (conceptDb.get(null, conceptKey, conceptValue, LockMode.DEFAULT) == OperationStatus.SUCCESS) {
			if (logger.isLoggable(Level.FINE)) {
				logger.fine("Got concept: " + conceptId + " elapsed time: "
						+ timer.getElapsedTime() / 1000 + " secs");
			}
			return (I_ConceptAttributeVersioned) conBinding.entryToObject(conceptValue);
		}
		throw new DatabaseException("Concept: " + conceptId + " not found.");
	}

	public I_DescriptionVersioned getDescription(int descId)
			throws DatabaseException {
		DatabaseEntry descKey = new DatabaseEntry();
		DatabaseEntry descValue = new DatabaseEntry();
		intBinder.objectToEntry(descId, descKey);
		if (descDb.get(null, descKey, descValue, LockMode.DEFAULT) == OperationStatus.SUCCESS) {
			return (I_DescriptionVersioned) descBinding.entryToObject(descValue);
		}
		throw new DatabaseException("Description: " + descId + " not found.");
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
			uidToIdMap = createUidToIdMap();
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
	 *            This collection must be synchronized since this is a
	 *            mutlithreaded method.
	 * @param latch
	 * @throws DatabaseException
	 */
	public void search(I_TrackContinuation tracker, Pattern p,
			Collection<ThinDescVersioned> matches, CountDownLatch latch, I_GetConceptData root, I_ConfigAceFrame config)
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
				ACE.threadPool.execute(new CheckAndProcessMatch(p, matches,
						descV, root, config));
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
			} else  {
				logger.info("Canceled. Elapsed time: " + timer.getElapsedTime());
			}
			timer.stop();
		}
	}

	private static class CheckAndProcessMatch implements Runnable {
		Pattern p;

		Collection<ThinDescVersioned> matches;

		ThinDescVersioned descV;
		
		I_GetConceptData root;
		
		I_ConfigAceFrame config;

		public CheckAndProcessMatch(Pattern p,
				Collection<ThinDescVersioned> matches, ThinDescVersioned descV, I_GetConceptData root, I_ConfigAceFrame config) {
			super();
			this.p = p;
			this.matches = matches;
			this.descV = descV;
			this.root = root;
			this.config = config;
		}

		public void run() {
			if (descV.matches(p)) {
				if (root == null) {
					matches.add(descV);
				} else {
					ConceptBean descConcept = ConceptBean.get(descV.getConceptId());
					try {
						if  (root.isParentOf(descConcept, config.getAllowedStatus(), config.getDestRelTypes(), 
								config.getViewPositionSet(), true)) {
							matches.add(descV);
						}
					} catch (IOException e) {
						AceLog.getAppLog().alertAndLogException(e);
					}
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
		iterateDescriptions(p);
		iterateConcepts(p);
		iterateRelationships(p);
		addTimeBranchValues(values);
	}

	private static class DescChangesProcessor implements I_ProcessDescriptions,
			I_ProcessConcepts, I_ProcessRelationships {
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

		public void processConcept(DatabaseEntry key, DatabaseEntry value)
				throws IOException {
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

	public void iterateDescriptions(I_ProcessDescriptions processor)
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

	public void iterateRelationships(I_ProcessRelationships processor)
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

	public void iterateConcepts(I_ProcessConcepts processor) throws Exception {
		Cursor concCursor = getConceptDb().openCursor(null, null);
		DatabaseEntry foundKey = processor.getKeyEntry();
		DatabaseEntry foundData = processor.getDataEntry();
		while (concCursor.getNext(foundKey, foundData, LockMode.DEFAULT) == OperationStatus.SUCCESS) {
			try {
				processor.processConcept(foundKey, foundData);
			} catch (Exception e) {
				concCursor.close();
				throw e;
			}
		}
		concCursor.close();
	}

	public void iterateIds(I_ProcessIds processor) throws Exception {
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

	public void iterateImages(I_ProcessImages processor) throws Exception {
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

	public void iteratePaths(I_ProcessPaths processor) throws Exception {
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

	public void iterateTimeBranch(I_ProcessTimeBranch processor)
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
		I_IdVersioned id = getId(uid);
		if (id != null) {
			return id.getNativeId();
		}
		throw new NoMappingException("No id for: " + uid);
	}

	public int uuidToNative(Collection<UUID> uids) throws TerminologyException, IOException {
		I_IdVersioned id = getId(uids);
		if (id == null) {
			throw new NoMappingException("No id for: " + uids);
		}
		return id.getNativeId();
	}

	public ThinIdVersioned getId(UUID uid) throws TerminologyException, IOException {
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
		throw new TerminologyException("UIDs have multiple id records: " + ids);
	}

	public boolean hasId(List<UUID> uids) throws DatabaseException {
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
			I_Path idPath, int version) throws TerminologyException, IOException {
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
							ArchitectonicAuxiliary.Concept.CURRENT.getUids(), source,
							idPath, version));
					idPart.setPathId(uuidToNativeWithGeneration(
							ArchitectonicAuxiliary.Concept.ARCHITECTONIC_BRANCH.getUids(),
							source, idPath, version));
					idPart.setSource(source);
					idPart.setSourceId(uid);
					idPart.setVersion(version);
					newId.addVersion(idPart);
				}

				DatabaseEntry idKey = new DatabaseEntry();
				DatabaseEntry idValue = new DatabaseEntry();
				intBinder.objectToEntry(newId.getNativeId(), idKey);
				idBinding.objectToEntry(newId, idValue);
				idDb.put(null, idKey, idValue);
				return newId.getNativeId();
			} catch (DatabaseException ex) {
				throw new ToIoException(ex);
			}
		}
	}

	public int uuidToNativeWithGeneration(UUID uid, int source,
			Collection<I_Path> idPaths, int version) throws TerminologyException, IOException {
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
							ArchitectonicAuxiliary.Concept.CURRENT.getUids(), source, p,
							version));
					idPart.setPathId(uuidToNativeWithGeneration(
							ArchitectonicAuxiliary.Concept.ARCHITECTONIC_BRANCH.getUids(),
							source, p, version));
					idPart.setSource(source);
					idPart.setSourceId(uid);
					idPart.setVersion(version);
					newId.addVersion(idPart);
				}

				DatabaseEntry idKey = new DatabaseEntry();
				DatabaseEntry idValue = new DatabaseEntry();
				intBinder.objectToEntry(newId.getNativeId(), idKey);
				idBinding.objectToEntry(newId, idValue);
				idDb.put(null, idKey, idValue);
				return newId.getNativeId();
			} catch (DatabaseException e2) {
				throw new ToIoException(e2);
			}
		}
	}
	
	public Class getNativeIdClass() {
		return Integer.class; 
	}

	public void writeId(I_IdVersioned id) throws DatabaseException {
		DatabaseEntry idKey = new DatabaseEntry();
		DatabaseEntry idValue = new DatabaseEntry();
		intBinder.objectToEntry(id.getNativeId(), idKey);
		idBinding.objectToEntry(id, idValue);
		idDb.put(null, idKey, idValue);
		if (logger.isLoggable(Level.FINE)) {
			logger.fine("Writing nativeId : " + id);
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

	public void writeConcept(I_ConceptAttributeVersioned concept) throws DatabaseException {
		DatabaseEntry key = new DatabaseEntry();
		DatabaseEntry value = new DatabaseEntry();
		intBinder.objectToEntry(concept.getConId(), key);
		conBinding.objectToEntry(concept, value);
		conceptDb.put(null, key, value);
	}

	public void writeRel(I_RelVersioned rel) throws DatabaseException {
		DatabaseEntry key = new DatabaseEntry();
		DatabaseEntry value = new DatabaseEntry();
		intBinder.objectToEntry(rel.getRelId(), key);
		relBinding.objectToEntry(rel, value);
		relDb.put(null, key, value);
	}

	public void writeDescription(I_DescriptionVersioned desc)
			throws DatabaseException {
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
		throw new DatabaseException("Path: " + nativeId + " not found.");
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

	public I_ImageVersioned getImage(UUID uid) throws TerminologyException, IOException, DatabaseException {
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
			I_ImageVersioned image = (I_ImageVersioned) imageBinder.entryToObject(imageValue);
			if (logger.isLoggable(Level.FINE)) {
				logger.fine("Got image: " + nativeId + " for concept: " + ConceptBean.get(image.getConceptId()) + " elapsed time: "
						+ timer.getElapsedTime() / 1000 + " secs");
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
			return ((I_IdVersioned) idBinding.entryToObject(idValue))
					.getUIDs();
		}
		throw new DatabaseException("Concept: " + nativeId + " not found.");
	}

	public I_IdVersioned getId(int nativeId) throws IOException {
		Stopwatch timer = null;
		if (logger.isLoggable(Level.FINE)) {
			logger.fine("Getting id record for : " + nativeId);
			timer = new Stopwatch();
			timer.start();
		}
		DatabaseEntry idKey = new DatabaseEntry();
		DatabaseEntry idValue = new DatabaseEntry();
		intBinder.objectToEntry(nativeId, idKey);
		try {
			if (idDb.get(null, idKey, idValue, LockMode.DEFAULT) == OperationStatus.SUCCESS) {
				if (logger.isLoggable(Level.FINE)) {
					logger.fine("Got id record for: " + nativeId
							+ " elapsed time: " + timer.getElapsedTime() / 1000
							+ " secs");
				}
				return (I_IdVersioned) idBinding.entryToObject(idValue);
			}
		} catch (DatabaseException e) {
			new ToIoException(e);
		}
		throw new ToIoException(new DatabaseException("Concept: " + nativeId + " not found."));
	}

	public List<TimePathId> getTimePathList() throws Exception {
		TimePathCollector tpCollector = new TimePathCollector();
		iterateTimeBranch(tpCollector);
		return tpCollector.getTimePathIdList();
	}

	public List<Path> getPaths() throws Exception {
		PathCollector collector = new PathCollector();
		AceConfig.vodb.iteratePaths(collector);
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

}
