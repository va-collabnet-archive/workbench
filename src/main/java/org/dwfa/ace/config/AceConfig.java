package org.dwfa.ace.config;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.beans.VetoableChangeListener;
import java.beans.VetoableChangeSupport;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.dwfa.ace.IntSet;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.cement.DocumentAuxiliary;
import org.dwfa.cement.HL7;
import org.dwfa.cement.QueueType;
import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.cement.SNOMED;
import org.dwfa.log.LogViewerFrame;
import org.dwfa.util.io.FileIO;
import org.dwfa.util.io.JarExtractor;
import org.dwfa.vodb.VodbEnv;
import org.dwfa.vodb.protocol.ExtendedUrlStreamHandlerFactory;
import org.dwfa.vodb.types.ConceptBean;
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
    private static final int dataVersion = 2;
	public static VodbEnv vodb = new VodbEnv();
	private static String DEFAULT_LOGGER_CONFIG_FILE = "logViewer.config";
    
    private transient VetoableChangeSupport vetoSupport = new VetoableChangeSupport(this);
    private transient PropertyChangeSupport changeSupport = new PropertyChangeSupport(this);
    
    public List<AceFrameConfig> aceFrames = new ArrayList<AceFrameConfig>();
    private File dbFolder = new File("../test/berkeley-db");
    private String loggerConfigFile  = DEFAULT_LOGGER_CONFIG_FILE;
    private boolean readOnly = false;

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
        out.writeObject(aceFrames);
        out.writeObject(loggerConfigFile);
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
    			try {
    				AceConfig.vodb.setup(dbFolder, readOnly);
    			} catch (DatabaseException e) {
    				IOException ioe = new IOException(e.getMessage());
    				ioe.initCause(e);
    				e.printStackTrace();
    			}
            	aceFrames = (List<AceFrameConfig>) in.readObject();
        	}
        	if (objDataVersion >= 2) {
        		loggerConfigFile = (String) in.readObject();
        	} else {
        		loggerConfigFile = DEFAULT_LOGGER_CONFIG_FILE;
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
				
				System.out.println("DB not created");
				int n = JOptionPane.showConfirmDialog(
					    new JFrame(),
					    "Would you like to extract the db from your maven repository?",
					    "DB does not exist",
					    JOptionPane.YES_NO_OPTION);
				if (n == JOptionPane.YES_OPTION) {
					extractMavenLib(config);
				} else {
					System.out.println("Exiting, user did not want to extract the DB from maven.");
					return;
				}
			}

			File configFile = new File(fileStr);
			if (configFile.exists() == false) {
				AceConfig.vodb.setup(config.dbFolder, config.readOnly);
				AceFrameConfig af = new AceFrameConfig();
				Set<Position> positions = new HashSet<Position>();
				for (Path p: Path.makeTestSnomedPaths(vodb)) {
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
			} else {
				FileInputStream fis = new FileInputStream(configFile);
				ObjectInputStream ois = new ObjectInputStream(fis);
				config = (AceConfig) ois.readObject();
			}
			File logConfigFile = new File(configFile.getParent(), config.loggerConfigFile);
			if (logConfigFile.exists() == false) {
	            URL logConfigUrl = AceConfig.class.getResource("/org/dwfa/resources/core/config/logViewer.config");
				System.out.println("Config file does not exist... " + logConfigUrl);
				InputStream is = logConfigUrl.openStream();
				FileOutputStream fos = new FileOutputStream(logConfigFile);
				FileIO.copyFile(is, fos, true);
				is.close();
			}
			new LogViewerFrame(new String[] { logConfigFile.getCanonicalPath() }, null);

			for (AceFrameConfig ace: config.aceFrames) {
				if (ace.isActive()) {
					AceFrame af = new AceFrame(null, null, config, ace);
					af.setVisible(true);
				}
			}
			
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}

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

	private static void extractMavenLib(AceConfig config) throws IOException {
		URL dbUrl = AceConfig.class.getClassLoader().getResource("locator.txt");
		
		System.out.println(" url: " + dbUrl);
		String[] pathParts = dbUrl.getPath().split("!");
		String[] fileProtocolParts = pathParts[0].split(":");
		
		File srcJarFile = new File(fileProtocolParts[1].replace("util", "ace-db").replace("dwfa", "jehri"));
		File targetDir = config.dbFolder.getParentFile();
		System.out.println("Jar file: " + srcJarFile);
		if (targetDir.exists() && targetDir.lastModified() == srcJarFile.lastModified()) {
			System.out.println("ace-db is current...");
		} else {
			System.out.println("ace-db needs update...");
			targetDir.mkdirs();
			System.out.println("Now extracting into: " + targetDir.getCanonicalPath());
			JarExtractor.execute(srcJarFile, targetDir);
			targetDir.setLastModified(srcJarFile.lastModified());
		}
	}
	public String getLoggerConfigFile() {
		return loggerConfigFile;
	}
	public void setLoggerConfigFile(String loggerConfigFile) {
		this.loggerConfigFile = loggerConfigFile;
	}

    
}
