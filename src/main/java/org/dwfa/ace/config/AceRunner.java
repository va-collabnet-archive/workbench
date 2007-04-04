package org.dwfa.ace.config;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Arrays;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import net.jini.config.Configuration;
import net.jini.config.ConfigurationProvider;

import org.dwfa.ace.ACE;
import org.dwfa.ace.AceLog;
import org.dwfa.ace.api.I_ConfigAceFrame;

import com.sun.jini.start.LifeCycle;

public class AceRunner {

	@SuppressWarnings("unused")
	private String[] args;

	@SuppressWarnings("unused")
	private LifeCycle lc;

	protected Configuration config;

	public AceRunner(String[] args, LifeCycle lc)
			throws Exception {
		this.args = args;
		this.lc = lc;
		String argsStr;
		if (args == null) {
			argsStr = "null";
		} else {
			argsStr = Arrays.asList(args).toString();
		}
		AceLog.info("\n*******************\n\n"
				+ "Starting service with config file args: " + argsStr
				+ "\n\n******************\n");
		config = ConfigurationProvider.getInstance(args, getClass()
				.getClassLoader());
		File aceConfigFile = (File) config.getEntry(this.getClass().getName(),
				"aceConfigFile", File.class, new File(
						"src/main/config/config.ace"));
		AceConfig aceConfig;
		if (aceConfigFile.exists()) {
			ObjectInputStream ois = new ObjectInputStream(
					new BufferedInputStream(new FileInputStream(aceConfigFile)));
			aceConfig = (AceConfig) ois.readObject();
			setupDatabase(aceConfig);
		} else {
			File dbFolder = (File) config.getEntry(this.getClass().getName(),
					"dbFolder", File.class, new File("target/berkeley-db"));
			Long cacheSize = (Long) config.getEntry(this.getClass().getName(),
					"cacheSize", Long.class, null);
			AceLog.info("Cache size in config file: " + cacheSize);
			aceConfig = new AceConfig(dbFolder);
			setupDatabase(aceConfig);
			AceConfig.setupAceConfig(aceConfig, aceConfigFile, cacheSize);
		}
		ACE.setAceConfig(aceConfig);
		for (I_ConfigAceFrame ace: aceConfig.aceFrames) {
			if (ace.isActive()) {
				AceFrame af = new AceFrame(args, lc, ace);
				af.setVisible(true);
			}
		}
	}

	private void setupDatabase(AceConfig aceConfig) throws IOException {
		if (aceConfig.isDbCreated() == false) {
			int n = JOptionPane.showConfirmDialog(
				    new JFrame(),
				    "Would you like to extract the db from your maven repository?",
				    "DB does not exist",
				    JOptionPane.YES_NO_OPTION);
			if (n == JOptionPane.YES_OPTION) {
				AceConfig.extractMavenLib(aceConfig);
			} else {
				AceLog.info("Exiting, user did not want to extract the DB from maven.");
				return;
			}
		}
	}
}
