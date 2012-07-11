package org.ihtsdo.cs.econcept.workflow;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.logging.Level;

import org.dwfa.ace.log.AceLog;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.tk.dto.concept.component.refex.TkRefexAbstractMember;

public abstract class AbstractWfChangeSetFilter implements I_WfChangeSetScrubber {
	// Have single Logfile set containing single collection with one log file per filter 
	// regardless of number of instantiations of the filter
	protected String filePath;
	protected static Map<String,BufferedWriter> logFiles = null;
	private final boolean loggingOn = false;

	// Not logging-specific variables
	protected HashSet<TkRefexAbstractMember<?>>  wfMembersToProcess = new HashSet<TkRefexAbstractMember<?>>();
	protected HashSet<TkRefexAbstractMember<?>>  wfMembersToIgnore = new HashSet<TkRefexAbstractMember<?>>();

	
	AbstractWfChangeSetFilter(String fileName) {
		if (loggingOn) {
			try {
				filePath = fileName;
	
				if (logFiles == null) {
					logFiles = new HashMap<String,BufferedWriter>();
				}
				
				if (!logFiles.containsKey(fileName)) {
					BufferedWriter logFile = new BufferedWriter(new FileWriter(fileName));
					logFile.append("Error file for: " + filePath);
					logFile.newLine();
					logFile.newLine();
					logFile.flush();
					logFiles.put(fileName, logFile);
				}
			} catch (Exception e) {
				AceLog.getAppLog().log(Level.WARNING, "Unable to open log file: " + filePath);
			}
		}
	}
		
	// Logging methods below
	protected void printError(TkRefexAbstractMember<?> member) {
		if (loggingOn) {
			try {
				logFiles.get(filePath).append("Error Member: " + member);
				logFiles.get(filePath).newLine();
				logFiles.get(filePath).flush();
			} catch (Exception e) {
				AceLog.getAppLog().log(Level.WARNING, "Unable to add member to log file: " + this.getClass().getCanonicalName());
			}
		}
	}
	
	protected void printError(String val) {
		if (loggingOn) {
			try {
				logFiles.get(filePath).append(val);
				logFiles.get(filePath).newLine();
				logFiles.get(filePath).flush();
			} catch (Exception e) {
				AceLog.getAppLog().log(Level.WARNING, "Unable to add member to log file: " + this.getClass().getCanonicalName());
			}
		}
	}
	
	protected void closeErrorFile() {
		if (loggingOn) {
			try {
				logFiles.get(filePath).flush();
				logFiles.get(filePath).close();
			} catch (Exception e) {
				AceLog.getAppLog().log(Level.WARNING, "Unable to close log file");
			}
		}
	}
}
