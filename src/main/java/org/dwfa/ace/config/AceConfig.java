package org.dwfa.ace.config;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.beans.VetoableChangeListener;
import java.beans.VetoableChangeSupport;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.URL;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.dwfa.ace.ACE;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_Path;
import org.dwfa.ace.api.I_Position;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.cs.BinaryChangeSetWriter;
import org.dwfa.ace.log.AceLog;
import org.dwfa.ace.url.tiuid.ExtendedUrlStreamHandlerFactory;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.cement.DocumentAuxiliary;
import org.dwfa.cement.HL7;
import org.dwfa.cement.QueueType;
import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.cement.SNOMED;
import org.dwfa.fd.FileDialogUtil;
import org.dwfa.log.HtmlHandler;
import org.dwfa.log.LogViewerFrame;
import org.dwfa.svn.SvnPrompter;
import org.dwfa.tapi.I_ConceptualizeUniversally;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.io.FileIO;
import org.dwfa.util.io.JarExtractor;
import org.dwfa.vodb.VodbEnv;
import org.dwfa.vodb.types.ConceptBean;
import org.dwfa.vodb.types.IntSet;
import org.dwfa.vodb.types.Path;
import org.dwfa.vodb.types.Position;

import com.sleepycat.je.DatabaseException;

public class AceConfig implements Serializable {

	private static File dbFolderOverride = null;

	public static AceConfig config;

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static final int dataVersion = 4;

	private static String DEFAULT_LOGGER_CONFIG_FILE = "logViewer.config";

	private static String DEFAULT_ACE_CONFIG_FILE = "ace.config";

	private transient VetoableChangeSupport vetoSupport = new VetoableChangeSupport(
			this);

	private transient PropertyChangeSupport changeSupport = new PropertyChangeSupport(
			this);

	public List<AceFrameConfig> aceFrames = new ArrayList<AceFrameConfig>();

	private File dbFolder = new File("../test/berkeley-db");

	private String loggerRiverConfigFile = DEFAULT_LOGGER_CONFIG_FILE;

	private String aceRiverConfigFile = DEFAULT_ACE_CONFIG_FILE;

	private boolean readOnly = false;

	private Long cacheSize = null;

	// 4
	private String username;

	private String password;

	public AceConfig() throws DatabaseException {
		super();
		if (LocalVersionedTerminology.get() == null) {
			LocalVersionedTerminology.set(new VodbEnv());
		}
	}

	public AceConfig(File dbFolder) throws DatabaseException {
		this();
		this.dbFolder = dbFolder;
	}

	public AceConfig(File dbFolder, boolean readOnly) throws DatabaseException {
		super();
		this.dbFolder = dbFolder;
		this.readOnly = readOnly;
	}

	private void writeObject(ObjectOutputStream out) throws IOException {
		out.writeInt(dataVersion);
		out.writeObject(username);
		out.writeObject(password);
		out.writeObject(dbFolder);
		out.writeBoolean(readOnly);
		out.writeObject(cacheSize);
		out.writeObject(aceFrames);
		out.writeObject(loggerRiverConfigFile);
	}

	private static final String authFailureMsg = "Username and password do not match.";

	@SuppressWarnings("unchecked")
	private void readObject(java.io.ObjectInputStream in) throws IOException,
			ClassNotFoundException {
		int objDataVersion = in.readInt();
		if (objDataVersion <= dataVersion) {
			if (objDataVersion >= 4) {
				username = (String) in.readObject();
				password = (String) in.readObject();
				SvnPrompter prompter = new SvnPrompter();
				prompter.prompt("config file", username);
				if (username.equals(prompter.getUsername())
						&& password.equals(prompter.getPassword())) {
					// continue
				} else {
					throw new IOException(authFailureMsg);
				}
			} else {
				username = null;
				password = null;
			}
			if (objDataVersion >= 1) {
				dbFolder = (File) in.readObject();
				if (dbFolderOverride != null) {
					dbFolder = dbFolderOverride;
				}
				readOnly = in.readBoolean();
				if (objDataVersion >= 3) {
					cacheSize = (Long) in.readObject();
				} else {
					cacheSize = null;
				}
				try {
					if (AceConfig.getVodb() == null) {
						new VodbEnv();
					}
					AceConfig.getVodb().setup(dbFolder, readOnly, cacheSize);
				} catch (IOException e) {
					AceLog.getAppLog().alertAndLogException(e);
				}
				aceFrames = (List<AceFrameConfig>) in.readObject();
			}
			if (objDataVersion >= 2) {
				loggerRiverConfigFile = (String) in.readObject();
			} else {
				loggerRiverConfigFile = DEFAULT_LOGGER_CONFIG_FILE;
			}
		} else {
			throw new IOException("Can't handle dataversion: " + objDataVersion);
		}
		this.vetoSupport = new VetoableChangeSupport(this);
		this.changeSupport = new PropertyChangeSupport(this);
	}

	public static void main(String[] args) throws SecurityException,
			IOException {
		System.setProperty("apple.laf.useScreenMenuBar", "true");
		URL.setURLStreamHandlerFactory(new ExtendedUrlStreamHandlerFactory());
		AceLog.getEditLog().setLevel(Level.FINER);
		HtmlHandler h = new HtmlHandler(null, "edits");
		h.setLevel(Level.FINER);
		AceLog.getEditLog().addHandler(h);
		AceLog.getEditLog().info("Setting up for editing.");
		try {
			String fileStr;
			if (args.length == 0) {
				fileStr = "config.ace";
			} else {
				fileStr = args[0];
				if (args.length > 1) {
					dbFolderOverride = new File(args[1]);
				}
			}
			if (dbFolderOverride != null) {
				config = new AceConfig(dbFolderOverride);
			} else {
				config = new AceConfig();
			}

			if (config.isDbCreated() == false) {

				AceLog.getAppLog().info("DB not created");
				int n = JOptionPane
						.showConfirmDialog(
								new JFrame(),
								"Would you like to extract the db from your maven repository?",
								"DB does not exist", JOptionPane.YES_NO_OPTION);
				if (n == JOptionPane.YES_OPTION) {
					extractMavenLib(config);
				} else {
					AceLog
							.getAppLog()
							.info(
									"Exiting, user did not want to extract the DB from maven.");
					return;
				}
			}
			long defaultCacheSize = 600000000L;
			File configFile = new File(fileStr);
			if (configFile.exists() == false) {
				setupAceConfig(config, configFile, defaultCacheSize, false);
			} else {
				FileInputStream fis = new FileInputStream(configFile);
				ObjectInputStream ois = new ObjectInputStream(fis);
				try {
					config = (AceConfig) ois.readObject();
				} catch (IOException e) {
					if (e.getMessage().equalsIgnoreCase(authFailureMsg)) {
						IOException ioe = e;
						while (ioe != null) {
							ioe = null;
							ioe = loopOpenOrCreateConfig(defaultCacheSize, ioe);
						}

					} else {
						throw e;
					}
				}
			}
			File logConfigFile = new File(configFile.getParent(),
					config.loggerRiverConfigFile);
			if (logConfigFile.exists() == false) {
				URL logConfigUrl = AceConfig.class
						.getResource("/org/dwfa/resources/core/config/logViewer.config");
				AceLog.getAppLog().info(
						"Config file does not exist... " + logConfigUrl);
				InputStream is = logConfigUrl.openStream();
				FileOutputStream fos = new FileOutputStream(logConfigFile);
				FileIO.copyFile(is, fos, true);
				is.close();
			}
			new LogViewerFrame(
					new String[] { logConfigFile.getCanonicalPath() }, null);

			File aceRiverConfigFile = new File(configFile.getParent(), config
					.getAceRiverConfigFile());
			if (aceRiverConfigFile.exists() == false) {
				URL configUrl = AceConfig.class
						.getResource("/org/dwfa/ace/config/ace.config");
				AceLog.getAppLog().info(
						"Config file does not exist... " + configUrl);
				InputStream is = configUrl.openStream();
				FileOutputStream fos = new FileOutputStream(aceRiverConfigFile);
				FileIO.copyFile(is, fos, true);
				is.close();
			}

			ACE.setAceConfig(config);
			for (I_ConfigAceFrame ace : config.aceFrames) {
				if (ace.isActive()) {
					AceFrame af = new AceFrame(
							new String[] { aceRiverConfigFile.getAbsolutePath() },
							null, ace);
					af.setVisible(true);
				}
			}
		} catch (Exception e) {
			AceLog.getAppLog().alertAndLogException(e);
		}
	}

	private static IOException loopOpenOrCreateConfig(long defaultCacheSize,
			IOException ioe) throws TaskFailedException,
			ClassNotFoundException, DatabaseException, ParseException,
			TerminologyException, IOException {
		try {
			// Custom button text
			Object[] options = { "Select Config", "New Config", "Cancel" };
			int n = JOptionPane.showOptionDialog(null,
					"Would you like to select or "
							+ "\ncreate a configuration file?",
					"Authorization failure", JOptionPane.YES_NO_CANCEL_OPTION,
					JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
			switch (n) {
			case 0:
				// for reading existing
				File inFile = FileDialogUtil.getExistingFile(
						"Select a configuration file", new FilenameFilter() {
							public boolean accept(File dir, String name) {
								return name.endsWith(".ace");
							}
						});
				ObjectInputStream configStream = new ObjectInputStream(
						new BufferedInputStream(new FileInputStream(inFile)));
				config = (AceConfig) configStream.readObject();

				break;
			case 1:
				// for creating new
				File outFile = FileDialogUtil
						.getNewFile("Save environment to config file...");
				if (outFile.getName().endsWith(".ace") == false) {
					outFile = new File(outFile.getParentFile(), outFile
							.getName()
							+ ".ace");
				}
				config = new AceConfig();
				setupAceConfig(config, outFile, defaultCacheSize, false);
				break;
			case 2:
			default:
				System.out.println("System.exit from AceConfig ");
				System.exit(0);

			}
		} catch (IOException innerIoe) {
			if (innerIoe.getMessage().equalsIgnoreCase(authFailureMsg)) {
				ioe = innerIoe;
			} else {
				throw innerIoe;
			}
		}
		return ioe;
	}

	public static void setupAceConfig(AceConfig config, File configFile,
			Long cacheSize, boolean includeSnomed) throws DatabaseException, ParseException,
			TerminologyException, IOException, FileNotFoundException {
		AceConfig.getVodb().setup(config.dbFolder, config.readOnly, cacheSize);
		SvnPrompter prompter = new SvnPrompter();
		prompter.prompt("config file", "username");
		config.setUsername(prompter.getUsername());
		config.setPassword(prompter.getPassword());

		AceFrameConfig af = new AceFrameConfig(config);
		Set<I_Position> positions = new HashSet<I_Position>();
		for (I_Path p : Path.makeTestSnomedPaths(AceConfig.getVodb())) {
			positions.add(new Position(Integer.MAX_VALUE, p));
		}
		af.setViewPositions(positions);
		for (I_Position pos : positions) {
			af.addEditingPath(pos.getPath());
			break;
		}
		IntSet statusPopupTypes = new IntSet();
		statusPopupTypes.add(AceConfig.getVodb().getId(
				ArchitectonicAuxiliary.Concept.ACTIVE.getUids()).getNativeId());
		statusPopupTypes
				.add(AceConfig.getVodb().getId(
						ArchitectonicAuxiliary.Concept.CURRENT.getUids())
						.getNativeId());
		statusPopupTypes.add(AceConfig.getVodb().getId(
				ArchitectonicAuxiliary.Concept.FLAGGED_FOR_REVIEW.getUids())
				.getNativeId());
		statusPopupTypes
				.add(AceConfig.getVodb().getId(
						ArchitectonicAuxiliary.Concept.LIMITED.getUids())
						.getNativeId());
		statusPopupTypes.add(AceConfig.getVodb().getId(
				ArchitectonicAuxiliary.Concept.PENDING_MOVE.getUids())
				.getNativeId());
		statusPopupTypes.add(AceConfig.getVodb().getId(
				ArchitectonicAuxiliary.Concept.CONSTANT.getUids())
				.getNativeId());
		statusPopupTypes.add(AceConfig.getVodb().getId(
				ArchitectonicAuxiliary.Concept.INACTIVE.getUids())
				.getNativeId());
		statusPopupTypes
				.add(AceConfig.getVodb().getId(
						ArchitectonicAuxiliary.Concept.RETIRED.getUids())
						.getNativeId());
		statusPopupTypes.add(AceConfig.getVodb().getId(
				ArchitectonicAuxiliary.Concept.DUPLICATE.getUids())
				.getNativeId());
		statusPopupTypes.add(AceConfig.getVodb().getId(
				ArchitectonicAuxiliary.Concept.OUTDATED.getUids())
				.getNativeId());
		statusPopupTypes.add(AceConfig.getVodb().getId(
				ArchitectonicAuxiliary.Concept.AMBIGUOUS.getUids())
				.getNativeId());
		statusPopupTypes.add(AceConfig.getVodb().getId(
				ArchitectonicAuxiliary.Concept.ERRONEOUS.getUids())
				.getNativeId());
		statusPopupTypes
				.add(AceConfig.getVodb().getId(
						ArchitectonicAuxiliary.Concept.LIMITED.getUids())
						.getNativeId());
		statusPopupTypes.add(AceConfig.getVodb().getId(
				ArchitectonicAuxiliary.Concept.INAPPROPRIATE.getUids())
				.getNativeId());
		statusPopupTypes.add(AceConfig.getVodb().getId(
				ArchitectonicAuxiliary.Concept.MOVED_ELSEWHERE.getUids())
				.getNativeId());
		statusPopupTypes.add(AceConfig.getVodb().getId(
				ArchitectonicAuxiliary.Concept.PENDING_MOVE.getUids())
				.getNativeId());
		af.setEditStatusTypePopup(statusPopupTypes);

		IntSet descPopupTypes = new IntSet();
		descPopupTypes.add(AceConfig.getVodb().getId(
				ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE
						.getUids()).getNativeId());
		descPopupTypes.add(AceConfig.getVodb().getId(
				ArchitectonicAuxiliary.Concept.PREFERRED_DESCRIPTION_TYPE
						.getUids()).getNativeId());
		descPopupTypes.add(AceConfig.getVodb().getId(
				ArchitectonicAuxiliary.Concept.SYNONYM_DESCRIPTION_TYPE
						.getUids()).getNativeId());
		descPopupTypes.add(AceConfig.getVodb().getId(
				ArchitectonicAuxiliary.Concept.UNSPECIFIED_DESCRIPTION_TYPE
						.getUids()).getNativeId());
		descPopupTypes.add(AceConfig.getVodb()
				.getId(
						ArchitectonicAuxiliary.Concept.ENTRY_DESCRIPTION_TYPE
								.getUids()).getNativeId());
		descPopupTypes.add(AceConfig.getVodb().getId(
				ArchitectonicAuxiliary.Concept.XHTML_DEF.getUids())
				.getNativeId());
		af.setEditDescTypePopup(descPopupTypes);

		IntSet relCharacteristic = new IntSet();
		relCharacteristic.add(AceConfig.getVodb().getId(
				ArchitectonicAuxiliary.Concept.STATED_RELATIONSHIP.getUids())
				.getNativeId());
		relCharacteristic.add(AceConfig.getVodb().getId(
				ArchitectonicAuxiliary.Concept.INFERRED_RELATIONSHIP.getUids())
				.getNativeId());
		relCharacteristic.add(AceConfig.getVodb().getId(
				ArchitectonicAuxiliary.Concept.QUALIFIER_CHARACTERISTIC
						.getUids()).getNativeId());
		relCharacteristic.add(AceConfig.getVodb().getId(
				ArchitectonicAuxiliary.Concept.HISTORICAL_CHARACTERISTIC
						.getUids()).getNativeId());
		relCharacteristic.add(AceConfig.getVodb().getId(
				ArchitectonicAuxiliary.Concept.ADDITIONAL_CHARACTERISTIC
						.getUids()).getNativeId());
		af.setEditRelCharacteristicPopup(relCharacteristic);

		IntSet relRefinabilty = new IntSet();
		relRefinabilty.add(AceConfig.getVodb()
				.getId(
						ArchitectonicAuxiliary.Concept.MANDATORY_REFINABILITY
								.getUids()).getNativeId());
		relRefinabilty.add(AceConfig.getVodb().getId(
				ArchitectonicAuxiliary.Concept.OPTIONAL_REFINABILITY.getUids())
				.getNativeId());
		relRefinabilty.add(AceConfig.getVodb().getId(
				ArchitectonicAuxiliary.Concept.NOT_REFINABLE.getUids())
				.getNativeId());
		af.setEditRelRefinabiltyPopup(relRefinabilty);

		IntSet relTypes = new IntSet();
		relTypes.add(AceConfig.getVodb().getId(
				ArchitectonicAuxiliary.Concept.IS_A_REL.getUids())
				.getNativeId());

		if (AceConfig.getVodb().getId(SNOMED.Concept.IS_A.getUids()) != null) {
			relTypes.add(AceConfig.getVodb().getId(SNOMED.Concept.IS_A.getUids()).getNativeId());
		}
		af.setEditRelTypePopup(relTypes);

		IntSet roots = new IntSet();
		roots.add(AceConfig.getVodb().getId(
				ArchitectonicAuxiliary.Concept.ARCHITECTONIC_ROOT_CONCEPT
						.getUids()).getNativeId());
		roots.add(AceConfig.getVodb().getId(
				ArchitectonicAuxiliary.Concept.STATUS
						.getUids()).getNativeId());
		roots.add(AceConfig.getVodb().getId(
				ArchitectonicAuxiliary.Concept.DESCRIPTION_TYPE
						.getUids()).getNativeId());
		addIfNotNull(roots, SNOMED.Concept.ROOT);
		addIfNotNull(roots, DocumentAuxiliary.Concept.DOCUMENT_AUXILIARY);
		addIfNotNull(roots, RefsetAuxiliary.Concept.REFSET_AUXILIARY);
		addIfNotNull(roots, HL7.Concept.HL7);
		addIfNotNull(roots, QueueType.Concept.QUEUE_TYPE);
		af.setRoots(roots);

		IntSet allowedStatus = new IntSet();
		allowedStatus.add(AceConfig.getVodb().getId(
				ArchitectonicAuxiliary.Concept.ACTIVE.getUids()).getNativeId());
		allowedStatus
				.add(AceConfig.getVodb().getId(
						ArchitectonicAuxiliary.Concept.CURRENT.getUids())
						.getNativeId());
		allowedStatus.add(AceConfig.getVodb().getId(
				ArchitectonicAuxiliary.Concept.FLAGGED_FOR_REVIEW.getUids())
				.getNativeId());
		allowedStatus
				.add(AceConfig.getVodb().getId(
						ArchitectonicAuxiliary.Concept.LIMITED.getUids())
						.getNativeId());
		allowedStatus.add(AceConfig.getVodb().getId(
				ArchitectonicAuxiliary.Concept.PENDING_MOVE.getUids())
				.getNativeId());
		allowedStatus.add(AceConfig.getVodb().getId(
				ArchitectonicAuxiliary.Concept.CONFLICTING.getUids())
				.getNativeId());
		allowedStatus.add(AceConfig.getVodb().getId(
				ArchitectonicAuxiliary.Concept.CONSTANT.getUids())
				.getNativeId());
		allowedStatus.add(AceConfig.getVodb().getId(
				ArchitectonicAuxiliary.Concept.CONCEPT_RETIRED.getUids())
				.getNativeId());
		allowedStatus.add(AceConfig.getVodb().getId(
				ArchitectonicAuxiliary.Concept.FLAGGED_POTENTIAL_DESC_STYLE_ERROR.getUids())
				.getNativeId());
		allowedStatus.add(AceConfig.getVodb().getId(
				ArchitectonicAuxiliary.Concept.FLAGGED_POTENTIAL_DUPLICATE.getUids())
				.getNativeId());
		allowedStatus.add(AceConfig.getVodb().getId(
				ArchitectonicAuxiliary.Concept.FLAGGED_POTENTIAL_REL_ERROR.getUids())
				.getNativeId());
		af.setAllowedStatus(allowedStatus);

		IntSet destRelTypes = new IntSet();
		destRelTypes.add(AceConfig.getVodb().getId(
				ArchitectonicAuxiliary.Concept.IS_A_REL.getUids())
				.getNativeId());
		if (AceConfig.getVodb().getId(SNOMED.Concept.IS_A.getUids()) != null) {
			destRelTypes.add(AceConfig.getVodb().getId(SNOMED.Concept.IS_A.getUids())
					.getNativeId());
		}
		
		af.setDestRelTypes(destRelTypes);

		IntSet sourceRelTypes = new IntSet();
		af.setSourceRelTypes(sourceRelTypes);

		IntSet descTypes = new IntSet();
		descTypes.add(AceConfig.getVodb().getId(
				ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE
						.getUids()).getNativeId());
		descTypes.add(AceConfig.getVodb().getId(
				ArchitectonicAuxiliary.Concept.PREFERRED_DESCRIPTION_TYPE
						.getUids()).getNativeId());
		descTypes.add(AceConfig.getVodb().getId(
				ArchitectonicAuxiliary.Concept.SYNONYM_DESCRIPTION_TYPE
						.getUids()).getNativeId());
		descTypes.add(AceConfig.getVodb().getId(
				ArchitectonicAuxiliary.Concept.DESCRIPTION_TYPE.getUids())
				.getNativeId());
		descTypes.add(AceConfig.getVodb().getId(
				ArchitectonicAuxiliary.Concept.XHTML_DEF.getUids())
				.getNativeId());
		af.setDescTypes(descTypes);

		IntSet inferredViewTypes = new IntSet();
		inferredViewTypes.add(AceConfig.getVodb().getId(
				ArchitectonicAuxiliary.Concept.INFERRED_RELATIONSHIP.getUids())
				.getNativeId());
		af.setInferredViewTypes(inferredViewTypes);

		IntSet statedViewTypes = new IntSet();
		statedViewTypes.add(AceConfig.getVodb().getId(
				ArchitectonicAuxiliary.Concept.STATED_RELATIONSHIP.getUids())
				.getNativeId());
		statedViewTypes.add(AceConfig.getVodb().getId(
				ArchitectonicAuxiliary.Concept.DEFINING_CHARACTERISTIC
						.getUids()).getNativeId());
		statedViewTypes.add(AceConfig.getVodb().getId(
				ArchitectonicAuxiliary.Concept.ADDITIONAL_CHARACTERISTIC
						.getUids()).getNativeId());
		statedViewTypes.add(AceConfig.getVodb().getId(
				ArchitectonicAuxiliary.Concept.HISTORICAL_CHARACTERISTIC
						.getUids()).getNativeId());
		statedViewTypes.add(AceConfig.getVodb().getId(
				ArchitectonicAuxiliary.Concept.QUALIFIER_CHARACTERISTIC
						.getUids()).getNativeId());
		af.setStatedViewTypes(statedViewTypes);

		af.setDefaultDescriptionType(ConceptBean.get(AceConfig.getVodb().getId(
				ArchitectonicAuxiliary.Concept.SYNONYM_DESCRIPTION_TYPE
						.getUids()).getNativeId()));
		af.setDefaultRelationshipCharacteristic(ConceptBean.get(AceConfig.getVodb()
				.getId(
						ArchitectonicAuxiliary.Concept.STATED_RELATIONSHIP
								.getUids()).getNativeId()));
		af.setDefaultRelationshipRefinability(ConceptBean.get(AceConfig.getVodb()
				.getId(
						ArchitectonicAuxiliary.Concept.OPTIONAL_REFINABILITY
								.getUids()).getNativeId()));
		af.setDefaultRelationshipType(ConceptBean.get(AceConfig.getVodb().getId(
				ArchitectonicAuxiliary.Concept.IS_A_REL.getUids())
				.getNativeId()));
		af
				.setDefaultStatus(ConceptBean.get(AceConfig.getVodb().getId(
						ArchitectonicAuxiliary.Concept.ACTIVE.getUids())
						.getNativeId()));

		af
				.getTreeDescPreferenceList()
				.add(
						AceConfig.getVodb()
								.getId(
										ArchitectonicAuxiliary.Concept.PREFERRED_DESCRIPTION_TYPE
												.getUids()).getNativeId());
		af
				.getTreeDescPreferenceList()
				.add(
						AceConfig.getVodb()
								.getId(
										ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE
												.getUids()).getNativeId());

		af
				.getShortLabelDescPreferenceList()
				.add(
						AceConfig.getVodb()
								.getId(
										ArchitectonicAuxiliary.Concept.PREFERRED_DESCRIPTION_TYPE
												.getUids()).getNativeId());
		af
				.getShortLabelDescPreferenceList()
				.add(
						AceConfig.getVodb()
								.getId(
										ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE
												.getUids()).getNativeId());

		af
				.getLongLabelDescPreferenceList()
				.add(
						AceConfig.getVodb()
								.getId(
										ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE
												.getUids()).getNativeId());
		af
				.getLongLabelDescPreferenceList()
				.add(
						AceConfig.getVodb()
								.getId(
										ArchitectonicAuxiliary.Concept.PREFERRED_DESCRIPTION_TYPE
												.getUids()).getNativeId());

		af
				.getTableDescPreferenceList()
				.add(
						AceConfig.getVodb()
								.getId(
										ArchitectonicAuxiliary.Concept.PREFERRED_DESCRIPTION_TYPE
												.getUids()).getNativeId());
		af
				.getTableDescPreferenceList()
				.add(
						AceConfig.getVodb()
								.getId(
										ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE
												.getUids()).getNativeId());

		af.setDefaultStatus(ConceptBean
				.get(ArchitectonicAuxiliary.Concept.CURRENT.getUids()));
		af
				.setDefaultDescriptionType(ConceptBean
						.get(ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE
								.getUids()));

		af.setDefaultRelationshipType(ConceptBean
				.get(ArchitectonicAuxiliary.Concept.IS_A_REL.getUids()));
		af.setDefaultRelationshipCharacteristic(ConceptBean
				.get(ArchitectonicAuxiliary.Concept.DEFINING_CHARACTERISTIC
						.getUids()));
		af.setDefaultRelationshipRefinability(ConceptBean
				.get(ArchitectonicAuxiliary.Concept.OPTIONAL_REFINABILITY
						.getUids()));

		af
				.setSvnRepository("https://ace-demo.aceworkspace.net/svn/ace-demo/trunk/dev/change-sets");
		af.setSvnWorkingCopy("target/change-sets");
		if (config.getUsername() == null) {
			af.setChangeSetWriterFileName("nullUser."
					+ UUID.randomUUID().toString() + ".jcs");
		} else {
			af.setChangeSetWriterFileName(config.getUsername() + "."
					+ UUID.randomUUID().toString() + ".jcs");
		}

		af.getChangeSetWriters().add(
				new BinaryChangeSetWriter(new File(af.getSvnWorkingCopy(), af
						.getChangeSetWriterFileName()), new File(af
						.getSvnWorkingCopy(), "."
						+ af.getChangeSetWriterFileName())));

  		af.getAddressesList().add("va.user1.editor");
  		af.getAddressesList().add("va.user1.assignmentManager");
 		af.getAddressesList().add("kp.user2.editor");
 		af.getAddressesList().add("kp.user3.editor");
 		af.getAddressesList().add("va.user4.editor");
		config.aceFrames.add(af);
		configFile.getParentFile().mkdirs();
		FileOutputStream fos = new FileOutputStream(configFile);
		ObjectOutputStream oos = new ObjectOutputStream(fos);
		oos.writeObject(config);
		oos.close();
	}

	private static void addIfNotNull(IntSet roots, I_ConceptualizeUniversally concept) throws TerminologyException, IOException {
		if (AceConfig.getVodb().getId(concept.getUids()) != null) {
			roots.add(AceConfig.getVodb().getId(concept.getUids()).getNativeId());
		}
	}

	public void addPropertyChangeListener(PropertyChangeListener listener) {
		changeSupport.addPropertyChangeListener(listener);
	}

	public void addPropertyChangeListener(String propertyName,
			PropertyChangeListener listener) {
		changeSupport.addPropertyChangeListener(propertyName, listener);
	}

	public void removePropertyChangeListener(PropertyChangeListener listener) {
		changeSupport.removePropertyChangeListener(listener);
	}

	public void removePropertyChangeListener(String propertyName,
			PropertyChangeListener listener) {
		changeSupport.removePropertyChangeListener(propertyName, listener);
	}

	public void addVetoableChangeListener(String propertyName,
			VetoableChangeListener listener) {
		vetoSupport.addVetoableChangeListener(propertyName, listener);
	}

	public void addVetoableChangeListener(VetoableChangeListener listener) {
		vetoSupport.addVetoableChangeListener(listener);
	}

	public void removeVetoableChangeListener(String propertyName,
			VetoableChangeListener listener) {
		vetoSupport.removeVetoableChangeListener(propertyName, listener);
	}

	public void removeVetoableChangeListener(VetoableChangeListener listener) {
		vetoSupport.removeVetoableChangeListener(listener);
	}

	public boolean isDbCreated() {

		File[] dbFiles = dbFolder.listFiles(new FileFilter() {

			public boolean accept(File pathname) {
				return pathname.getName().endsWith(".jdb");
			}

		});
		return (dbFiles != null && dbFiles.length > 0);
	}

	public static void extractMavenLib(AceConfig config) throws IOException {
		URL dbUrl = AceConfig.class.getClassLoader().getResource("locator.txt");

		AceLog.getAppLog().info(" url: " + dbUrl);
		String[] pathParts = dbUrl.getPath().split("!");
		String[] fileProtocolParts = pathParts[0].split(":");

		File srcJarFile = new File(fileProtocolParts[1].replace("foundation",
				"ace-bdb").replace("dwfa", "jehri"));
		File targetDir = config.dbFolder.getParentFile();
		AceLog.getAppLog().info("Jar file: " + srcJarFile);
		if (targetDir.exists()
				&& targetDir.lastModified() == srcJarFile.lastModified()) {
			AceLog.getAppLog().info("ace-db is current...");
		} else {
			AceLog.getAppLog().info("ace-db needs update...");
			targetDir.mkdirs();
			AceLog.getAppLog().info(
					"Now extracting into: " + targetDir.getCanonicalPath());
			JarExtractor.execute(srcJarFile, targetDir);
			targetDir.setLastModified(srcJarFile.lastModified());
		}
	}

	public String getLoggerRiverConfigFile() {
		return loggerRiverConfigFile;
	}

	public void setLoggerRiverConfigFile(String loggerConfigFile) {
		this.loggerRiverConfigFile = loggerConfigFile;
	}

	public String getAceRiverConfigFile() {
		if (aceRiverConfigFile == null) {
			aceRiverConfigFile = DEFAULT_ACE_CONFIG_FILE;
		}
		return aceRiverConfigFile;
	}

	public void setAceRiverConfigFile(String aceRiverConfigFile) {
		this.aceRiverConfigFile = aceRiverConfigFile;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		Object old = this.password;
		this.password = password;
		this.changeSupport.firePropertyChange("password", old, password);
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		Object old = this.username;
		this.username = username;
		this.changeSupport.firePropertyChange("username", old, username);
	}

	public static VodbEnv getVodb() {
		return (VodbEnv) LocalVersionedTerminology.get();
	}

}
