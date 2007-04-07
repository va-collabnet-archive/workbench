package org.dwfa.ace.config;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.beans.VetoableChangeListener;
import java.beans.VetoableChangeSupport;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
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

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.dwfa.ace.ACE;
import org.dwfa.ace.AceLog;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_Path;
import org.dwfa.ace.api.I_Position;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.cement.DocumentAuxiliary;
import org.dwfa.cement.HL7;
import org.dwfa.cement.QueueType;
import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.cement.SNOMED;
import org.dwfa.log.LogViewerFrame;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.io.FileIO;
import org.dwfa.util.io.JarExtractor;
import org.dwfa.vodb.VodbEnv;
import org.dwfa.vodb.protocol.ExtendedUrlStreamHandlerFactory;
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
    private static final int dataVersion = 3;
	public static VodbEnv vodb = new VodbEnv();
	private static String DEFAULT_LOGGER_CONFIG_FILE = "logViewer.config";
	private static String DEFAULT_ACE_CONFIG_FILE = "ace.config";
    
    private transient VetoableChangeSupport vetoSupport = new VetoableChangeSupport(this);
    private transient PropertyChangeSupport changeSupport = new PropertyChangeSupport(this);
    
    public List<AceFrameConfig> aceFrames = new ArrayList<AceFrameConfig>();
    private File dbFolder = new File("../test/berkeley-db");
    private String loggerRiverConfigFile  = DEFAULT_LOGGER_CONFIG_FILE;
    private String aceRiverConfigFile  = DEFAULT_ACE_CONFIG_FILE;
    private boolean readOnly = false;
    private Long cacheSize = null;

    public AceConfig() throws DatabaseException {
		super();
	}
    public AceConfig(File dbFolder) throws DatabaseException {
		super();
		this.dbFolder = dbFolder;
	}

	public AceConfig(File dbFolder, boolean readOnly) throws DatabaseException {
		super();
		this.dbFolder = dbFolder;
		this.readOnly = readOnly;
	}

	private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeObject(dbFolder);
        out.writeBoolean(readOnly);
        out.writeObject(cacheSize);
        out.writeObject(aceFrames);
        out.writeObject(loggerRiverConfigFile);
    }

    @SuppressWarnings("unchecked")
	private void readObject(java.io.ObjectInputStream in) throws IOException,
            ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion <= dataVersion) {
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
    				AceConfig.vodb.setup(dbFolder, readOnly, cacheSize);
    			} catch (DatabaseException e) {
    				IOException ioe = new IOException(e.getMessage());
    				ioe.initCause(e);
    				AceLog.getLog().alertAndLogException(e);
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
	public static void main(String[] args) {
		System.setProperty("apple.laf.useScreenMenuBar", "true");
		URL.setURLStreamHandlerFactory(new ExtendedUrlStreamHandlerFactory());
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
				
				AceLog.getLog().info("DB not created");
				int n = JOptionPane.showConfirmDialog(
					    new JFrame(),
					    "Would you like to extract the db from your maven repository?",
					    "DB does not exist",
					    JOptionPane.YES_NO_OPTION);
				if (n == JOptionPane.YES_OPTION) {
					extractMavenLib(config);
				} else {
					AceLog.getLog().info("Exiting, user did not want to extract the DB from maven.");
					return;
				}
			}
			File configFile = new File(fileStr);
			if (configFile.exists() == false) {
				setupAceConfig(config, configFile, 600000000L);
			} else {
				FileInputStream fis = new FileInputStream(configFile);
				ObjectInputStream ois = new ObjectInputStream(fis);
				config = (AceConfig) ois.readObject();
			}
			File logConfigFile = new File(configFile.getParent(), config.loggerRiverConfigFile);
			if (logConfigFile.exists() == false) {
	            URL logConfigUrl = AceConfig.class.getResource("/org/dwfa/resources/core/config/logViewer.config");
	            AceLog.getLog().info("Config file does not exist... " + logConfigUrl);
				InputStream is = logConfigUrl.openStream();
				FileOutputStream fos = new FileOutputStream(logConfigFile);
				FileIO.copyFile(is, fos, true);
				is.close();
			}
			new LogViewerFrame(new String[] { logConfigFile.getCanonicalPath() }, null);

			File aceRiverConfigFile = new File(configFile.getParent(), config.getAceRiverConfigFile());
			if (aceRiverConfigFile.exists() == false) {
	            URL configUrl = AceConfig.class.getResource("/org/dwfa/ace/config/ace.config");
	            AceLog.getLog().info("Config file does not exist... " + configUrl);
				InputStream is = configUrl.openStream();
				FileOutputStream fos = new FileOutputStream(aceRiverConfigFile);
				FileIO.copyFile(is, fos, true);
				is.close();
			}
			
			
			ACE.setAceConfig(config);
			for (I_ConfigAceFrame ace: config.aceFrames) {
				if (ace.isActive()) {
					AceFrame af = new AceFrame(new String[] { aceRiverConfigFile.getAbsolutePath() }, null, ace);
					af.setVisible(true);
				}
			}
		} catch (Exception e) {
			AceLog.getLog().alertAndLogException(e);
		}
	}
	public static void setupAceConfig(AceConfig config, File configFile, Long cacheSize) throws DatabaseException, ParseException, TerminologyException, IOException, FileNotFoundException {
		AceConfig.vodb.setup(config.dbFolder, config.readOnly, cacheSize);
		AceFrameConfig af = new AceFrameConfig();
		Set<I_Position> positions = new HashSet<I_Position>();
		for (I_Path p: Path.makeTestSnomedPaths(vodb)) {
			positions.add(new Position(Integer.MAX_VALUE, p));
		}
		af.setViewPositions(positions);
		
		IntSet statusPopupTypes = new IntSet();
		statusPopupTypes.add(vodb.getId(ArchitectonicAuxiliary.Concept.ACTIVE.getUids()).getNativeId());
		statusPopupTypes.add(vodb.getId(ArchitectonicAuxiliary.Concept.CURRENT.getUids()).getNativeId());
		statusPopupTypes.add(vodb.getId(ArchitectonicAuxiliary.Concept.LIMITED.getUids()).getNativeId());
		statusPopupTypes.add(vodb.getId(ArchitectonicAuxiliary.Concept.PENDING_MOVE.getUids()).getNativeId());
		statusPopupTypes.add(vodb.getId(ArchitectonicAuxiliary.Concept.CONSTANT.getUids()).getNativeId());
		statusPopupTypes.add(vodb.getId(ArchitectonicAuxiliary.Concept.INACTIVE.getUids()).getNativeId());
		statusPopupTypes.add(vodb.getId(ArchitectonicAuxiliary.Concept.RETIRED.getUids()).getNativeId());
		statusPopupTypes.add(vodb.getId(ArchitectonicAuxiliary.Concept.DUPLICATE.getUids()).getNativeId());
		statusPopupTypes.add(vodb.getId(ArchitectonicAuxiliary.Concept.OUTDATED.getUids()).getNativeId());
		statusPopupTypes.add(vodb.getId(ArchitectonicAuxiliary.Concept.AMBIGUOUS.getUids()).getNativeId());
		statusPopupTypes.add(vodb.getId(ArchitectonicAuxiliary.Concept.ERRONEOUS.getUids()).getNativeId());
		statusPopupTypes.add(vodb.getId(ArchitectonicAuxiliary.Concept.LIMITED.getUids()).getNativeId());
		statusPopupTypes.add(vodb.getId(ArchitectonicAuxiliary.Concept.INAPPROPRIATE.getUids()).getNativeId());
		statusPopupTypes.add(vodb.getId(ArchitectonicAuxiliary.Concept.MOVED_ELSEWHERE.getUids()).getNativeId());
		statusPopupTypes.add(vodb.getId(ArchitectonicAuxiliary.Concept.PENDING_MOVE.getUids()).getNativeId());
		af.setEditStatusTypePopup(statusPopupTypes);

		IntSet descPopupTypes = new IntSet();
		descPopupTypes.add(vodb.getId(ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.getUids()).getNativeId());
		descPopupTypes.add(vodb.getId(ArchitectonicAuxiliary.Concept.PREFERRED_DESCRIPTION_TYPE.getUids()).getNativeId());
		descPopupTypes.add(vodb.getId(ArchitectonicAuxiliary.Concept.SYNONYM_DESCRIPTION_TYPE.getUids()).getNativeId());
		descPopupTypes.add(vodb.getId(ArchitectonicAuxiliary.Concept.UNSPECIFIED_DESCRIPTION_TYPE.getUids()).getNativeId());
		descPopupTypes.add(vodb.getId(ArchitectonicAuxiliary.Concept.ENTRY_DESCRIPTION_TYPE.getUids()).getNativeId());
		descPopupTypes.add(vodb.getId(ArchitectonicAuxiliary.Concept.XHTML_DEF.getUids()).getNativeId());
		af.setEditDescTypePopup(descPopupTypes);
		
		IntSet relCharacteristic = new IntSet();
		relCharacteristic.add(vodb.getId(ArchitectonicAuxiliary.Concept.STATED_RELATIONSHIP.getUids()).getNativeId());
		relCharacteristic.add(vodb.getId(ArchitectonicAuxiliary.Concept.INFERRED_RELATIONSHIP.getUids()).getNativeId());
		relCharacteristic.add(vodb.getId(ArchitectonicAuxiliary.Concept.QUALIFIER_CHARACTERISTIC.getUids()).getNativeId());
		relCharacteristic.add(vodb.getId(ArchitectonicAuxiliary.Concept.HISTORICAL_CHARACTERISTIC.getUids()).getNativeId());
		relCharacteristic.add(vodb.getId(ArchitectonicAuxiliary.Concept.ADDITIONAL_CHARACTERISTIC.getUids()).getNativeId());
		af.setEditRelCharacteristicPopup(relCharacteristic);
		
		IntSet relRefinabilty = new IntSet();
		relRefinabilty.add(vodb.getId(ArchitectonicAuxiliary.Concept.MANDATORY_REFINABILITY.getUids()).getNativeId());
		relRefinabilty.add(vodb.getId(ArchitectonicAuxiliary.Concept.OPTIONAL_REFINABILITY.getUids()).getNativeId());
		relRefinabilty.add(vodb.getId(ArchitectonicAuxiliary.Concept.NOT_REFINABLE.getUids()).getNativeId());
		af.setEditRelRefinabiltyPopup(relRefinabilty);
		
		IntSet relTypes = new IntSet();
		relTypes.add(vodb.getId(ArchitectonicAuxiliary.Concept.IS_A_REL.getUids()).getNativeId());
		relTypes.add(vodb.getId(SNOMED.Concept.IS_A.getUids()).getNativeId());
		af.setEditRelTypePopup(relTypes);
		
		IntSet roots = new IntSet();
		roots.add(vodb.getId(ArchitectonicAuxiliary.Concept.ARCHITECTONIC_ROOT_CONCEPT.getUids()).getNativeId());
		roots.add(vodb.getId(SNOMED.Concept.ROOT.getUids()).getNativeId());
		roots.add(vodb.getId(DocumentAuxiliary.Concept.DOCUMENT_AUXILIARY.getUids()).getNativeId());
		roots.add(vodb.getId(RefsetAuxiliary.Concept.REFSET_AUXILIARY.getUids()).getNativeId());
		roots.add(vodb.getId(HL7.Concept.HL7.getUids()).getNativeId());
		roots.add(vodb.getId(QueueType.Concept.QUEUE_TYPE.getUids()).getNativeId());
		af.setRoots(roots);
		
		IntSet allowedStatus = new IntSet();
		allowedStatus.add(vodb.getId(ArchitectonicAuxiliary.Concept.ACTIVE.getUids()).getNativeId());
		allowedStatus.add(vodb.getId(ArchitectonicAuxiliary.Concept.CURRENT.getUids()).getNativeId());
		allowedStatus.add(vodb.getId(ArchitectonicAuxiliary.Concept.LIMITED.getUids()).getNativeId());
		allowedStatus.add(vodb.getId(ArchitectonicAuxiliary.Concept.PENDING_MOVE.getUids()).getNativeId());
		allowedStatus.add(vodb.getId(ArchitectonicAuxiliary.Concept.CONFLICTING.getUids()).getNativeId());
		allowedStatus.add(vodb.getId(ArchitectonicAuxiliary.Concept.CONSTANT.getUids()).getNativeId());
		allowedStatus.add(vodb.getId(ArchitectonicAuxiliary.Concept.CONCEPT_RETIRED.getUids()).getNativeId());
		af.setAllowedStatus(allowedStatus);
		
		
		IntSet destRelTypes = new IntSet();
		destRelTypes.add(vodb.getId(ArchitectonicAuxiliary.Concept.IS_A_REL.getUids()).getNativeId());
		destRelTypes.add(vodb.getId(SNOMED.Concept.IS_A.getUids()).getNativeId());
		af.setDestRelTypes(destRelTypes);
		
		IntSet sourceRelTypes = new IntSet();
		af.setSourceRelTypes(sourceRelTypes);
		
		IntSet descTypes = new IntSet();
		descTypes.add(vodb.getId(ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.getUids()).getNativeId());
		descTypes.add(vodb.getId(ArchitectonicAuxiliary.Concept.PREFERRED_DESCRIPTION_TYPE.getUids()).getNativeId());
		descTypes.add(vodb.getId(ArchitectonicAuxiliary.Concept.SYNONYM_DESCRIPTION_TYPE.getUids()).getNativeId());
		descTypes.add(vodb.getId(ArchitectonicAuxiliary.Concept.DESCRIPTION_TYPE.getUids()).getNativeId());
		descTypes.add(vodb.getId(ArchitectonicAuxiliary.Concept.XHTML_DEF.getUids()).getNativeId());
		af.setDescTypes(descTypes);
		
		IntSet inferredViewTypes = new IntSet();
		inferredViewTypes.add(vodb.getId(ArchitectonicAuxiliary.Concept.INFERRED_RELATIONSHIP.getUids()).getNativeId());
		af.setInferredViewTypes(inferredViewTypes);
		
		IntSet statedViewTypes = new IntSet();
		statedViewTypes.add(vodb.getId(ArchitectonicAuxiliary.Concept.STATED_RELATIONSHIP.getUids()).getNativeId());
		statedViewTypes.add(vodb.getId(ArchitectonicAuxiliary.Concept.DEFINING_CHARACTERISTIC.getUids()).getNativeId());
		statedViewTypes.add(vodb.getId(ArchitectonicAuxiliary.Concept.ADDITIONAL_CHARACTERISTIC.getUids()).getNativeId());
		statedViewTypes.add(vodb.getId(ArchitectonicAuxiliary.Concept.HISTORICAL_CHARACTERISTIC.getUids()).getNativeId());
		statedViewTypes.add(vodb.getId(ArchitectonicAuxiliary.Concept.QUALIFIER_CHARACTERISTIC.getUids()).getNativeId());
		af.setStatedViewTypes(statedViewTypes);
		
		af.setDefaultDescriptionType(ConceptBean.get(AceConfig.vodb.getId(ArchitectonicAuxiliary.Concept.SYNONYM_DESCRIPTION_TYPE.getUids()).getNativeId()));
		af.setDefaultRelationshipCharacteristic(ConceptBean.get(AceConfig.vodb.getId(ArchitectonicAuxiliary.Concept.STATED_RELATIONSHIP.getUids()).getNativeId()));
		af.setDefaultRelationshipRefinability(ConceptBean.get(AceConfig.vodb.getId(ArchitectonicAuxiliary.Concept.OPTIONAL_REFINABILITY.getUids()).getNativeId()));
		af.setDefaultRelationshipType(ConceptBean.get(AceConfig.vodb.getId(ArchitectonicAuxiliary.Concept.IS_A_REL.getUids()).getNativeId()));
		af.setDefaultStatus(ConceptBean.get(AceConfig.vodb.getId(ArchitectonicAuxiliary.Concept.ACTIVE.getUids()).getNativeId()));
		
		af.getTreeDescPreferenceList().add(vodb.getId(ArchitectonicAuxiliary.Concept.PREFERRED_DESCRIPTION_TYPE.getUids()).getNativeId());
		af.getTreeDescPreferenceList().add(vodb.getId(ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.getUids()).getNativeId());

		
		af.getShortLabelDescPreferenceList().add(vodb.getId(ArchitectonicAuxiliary.Concept.PREFERRED_DESCRIPTION_TYPE.getUids()).getNativeId());
		af.getShortLabelDescPreferenceList().add(vodb.getId(ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.getUids()).getNativeId());

		af.getLongLabelDescPreferenceList().add(vodb.getId(ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.getUids()).getNativeId());
		af.getLongLabelDescPreferenceList().add(vodb.getId(ArchitectonicAuxiliary.Concept.PREFERRED_DESCRIPTION_TYPE.getUids()).getNativeId());

		af.getTableDescPreferenceList().add(vodb.getId(ArchitectonicAuxiliary.Concept.PREFERRED_DESCRIPTION_TYPE.getUids()).getNativeId());
		af.getTableDescPreferenceList().add(vodb.getId(ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.getUids()).getNativeId());			
		
		af.setDefaultStatus(ConceptBean.get(ArchitectonicAuxiliary.Concept.CURRENT.getUids()));
		af.setDefaultDescriptionType(ConceptBean.get(ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.getUids()));
		
		af.setDefaultRelationshipType(ConceptBean.get(ArchitectonicAuxiliary.Concept.IS_A_REL.getUids()));
		af.setDefaultRelationshipCharacteristic(ConceptBean.get(ArchitectonicAuxiliary.Concept.DEFINING_CHARACTERISTIC.getUids()));
		af.setDefaultRelationshipRefinability(ConceptBean.get(ArchitectonicAuxiliary.Concept.MANDATORY_REFINABILITY.getUids()));

		config.aceFrames.add(af);
		configFile.getParentFile().mkdirs();
		FileOutputStream fos = new FileOutputStream(configFile);
		ObjectOutputStream oos = new ObjectOutputStream(fos);
		oos.writeObject(config);
		oos.close();
	}

	public void addPropertyChangeListener(PropertyChangeListener listener) {
		changeSupport.addPropertyChangeListener(listener);
	}

	public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
		changeSupport.addPropertyChangeListener(propertyName, listener);
	}

	public void removePropertyChangeListener(PropertyChangeListener listener) {
		changeSupport.removePropertyChangeListener(listener);
	}

	public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
		changeSupport.removePropertyChangeListener(propertyName, listener);
	}

	public void addVetoableChangeListener(String propertyName, VetoableChangeListener listener) {
		vetoSupport.addVetoableChangeListener(propertyName, listener);
	}

	public void addVetoableChangeListener(VetoableChangeListener listener) {
		vetoSupport.addVetoableChangeListener(listener);
	}

	public void removeVetoableChangeListener(String propertyName, VetoableChangeListener listener) {
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
		
		AceLog.getLog().info(" url: " + dbUrl);
		String[] pathParts = dbUrl.getPath().split("!");
		String[] fileProtocolParts = pathParts[0].split(":");
		
		File srcJarFile = new File(fileProtocolParts[1].replace("foundation", "ace-bdb").replace("dwfa", "jehri"));
		File targetDir = config.dbFolder.getParentFile();
		AceLog.getLog().info("Jar file: " + srcJarFile);
		if (targetDir.exists() && targetDir.lastModified() == srcJarFile.lastModified()) {
			AceLog.getLog().info("ace-db is current...");
		} else {
			AceLog.getLog().info("ace-db needs update...");
			targetDir.mkdirs();
			AceLog.getLog().info("Now extracting into: " + targetDir.getCanonicalPath());
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

    
}
