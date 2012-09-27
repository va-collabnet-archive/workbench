package org.ihtsdo.xml.controllers;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.dwfa.ace.api.DatabaseSetupConfig;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;

public class I_TermFactoryCreator {

	private static final Logger log = Logger.getLogger(I_TermFactoryCreator.class.getName());
	
	I_TermFactory tf = null;
	private DatabaseSetupConfig dbSetupConfig;
	File vodbDirectory;
	
	//Settable by a calling method
	/**
	 * The path to the database
	 */
	String vodbDirectoryS = null;
	
	/**
	 * True if the database is readonly.
	 */
	boolean readOnly = false;

	/**
	 * Default Size of cache used by the database.
	 */
	Long cacheSize = 600000000L;
	
	public I_TermFactoryCreator(String vodbDirectoryS) {
		super();
		this.vodbDirectoryS = vodbDirectoryS;
		
	}

	public String getVodbDirectoryS() {
		return vodbDirectoryS;
	}

	public void setVodbDirectoryS(String vodbDirectoryS) {
		this.vodbDirectoryS = vodbDirectoryS;
	}

	public File getVodbDirectory() {
		return vodbDirectory;
	}

	public void setVodbDirectory(File vodbDirectory) {
		this.vodbDirectory = vodbDirectory;
	}

	public boolean getReadOnly() {
		return readOnly;
	}

	public void setReadOnly(boolean readOnly) {
		this.readOnly = readOnly;
	}

	public Long getCacheSize() {
		return cacheSize;
	}

	public void setCacheSize(Long cacheSize) {
		this.cacheSize = cacheSize;
	}

	public I_TermFactory getTf() throws Exception {
		if(tf == null){
			setupDB();
		}
		return tf;
	}

	public void setTf(I_TermFactory tf) {
		this.tf = tf;
	}
	
	public void setupDB() throws Exception {
		if (vodbDirectoryS != null && vodbDirectoryS.length() > 0) {
			if (dbSetupConfig == null) {
				dbSetupConfig = new DatabaseSetupConfig();
			}
			try {
				if (vodbDirectory == null) {
					vodbDirectory = new File(vodbDirectoryS);
				}
			} catch (Exception e) {
				log.log(Level.SEVERE,"Error I_TermFactoryCreator.setupDB trying to create the vodbDirectory. Exception = ",e);
				e.printStackTrace();
				throw new Exception(e);
			}

			try {
				Terms.createFactory(vodbDirectory,readOnly, cacheSize, dbSetupConfig);
				tf = Terms.get();
				if (tf == null) {
					log.severe("NO TERM Factory Found");
					throw new Exception("NO TERM Factory Found");
				}
				try {log.severe("Num concepts = " + tf.getConceptCount());
				} catch (IOException e1) {throw new Exception(e1);}
			} catch (Exception e) {
				throw new Exception(e);
			}
		} else {
			log.severe("NO DBPATH Set");
			throw new Exception("NO DBPATH Set");
		}
	}
	
}
