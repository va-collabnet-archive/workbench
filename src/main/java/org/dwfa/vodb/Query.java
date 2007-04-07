package org.dwfa.vodb;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.regex.Pattern;

import org.dwfa.ace.AceLog;
import org.dwfa.ace.api.I_DescriptionVersioned;
import org.dwfa.vodb.bind.ThinDescVersionedBinding;
import org.dwfa.vodb.types.ThinDescVersioned;

import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.je.Cursor;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationStatus;
import com.sleepycat.je.SecondaryCursor;

public class Query {
	private static Date start;
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		start = new Date();
		VodbEnv vodb = new VodbEnv();
		AceLog.getLog().info("Opened env.");
		printElapsedTime();
		try {
			ThinDescVersionedBinding descBinder = new ThinDescVersionedBinding();
			ConceptIdKeyForDescCreator descForConceptKeyCreator = 
				new ConceptIdKeyForDescCreator(descBinder);
			vodb.setup(new File("berkeley-db"), true, 600000000L);
			AceLog.getLog().info("Setup env.");
			printElapsedTime();
			doQuery(vodb, descBinder, "SNOMED CT Concept");
			doQuery(vodb, descBinder, "Architectonic Auxillary");
			doQuery(vodb, descBinder, "SNOMED CT Concept");
			doQuickQuery(vodb, descBinder, "SNOMED");
			doConceptQuery(vodb, descBinder, descForConceptKeyCreator, "SNOMED CT Concept");
			doConceptQuery(vodb, descBinder, descForConceptKeyCreator, "SNOMED CT Concept");
			
			vodb.close();
			AceLog.getLog().info("closed env.");
			printElapsedTime();
		} catch (DatabaseException ex) {
			AceLog.getLog().alertAndLogException(ex);
		}


	}
	private static void doQuery(VodbEnv vodb, ThinDescVersionedBinding descBinder, String queryString) throws DatabaseException {
		Date oldStart = start;
		start = new Date();
		int descCount = 0;
		int matchCount = 0;
		int failCount = 0;
		TupleBinding intBinder = TupleBinding.getPrimitiveBinding(Integer.class);
		Cursor descC = vodb.getDescDb().openCursor(null, null);
		AceLog.getLog().info("------------------------------------------");
		AceLog.getLog().info("Started query cursor for: " + queryString);
		DatabaseEntry descKey = new DatabaseEntry();
		DatabaseEntry descValue = new DatabaseEntry();
		while (descC.getNext(descKey, descValue, LockMode.DEFAULT) == OperationStatus.SUCCESS) {
			descCount++;
			int descKeyInt = (Integer) intBinder.entryToObject(descKey);
			I_DescriptionVersioned vdesc = (I_DescriptionVersioned) descBinder.entryToObject(descValue);
			if (vdesc.matches(Pattern.compile(queryString))) {
				matchCount++;
				AceLog.getLog().info("descKeyInt: " + descKeyInt);
				AceLog.getLog().info(vdesc.toString());
			} else {
				failCount++;
			}
		}
		AceLog.getLog().info("descCount: " + descCount);
		AceLog.getLog().info("matchCount: " + matchCount);
		AceLog.getLog().info("failCount: " + failCount);
		AceLog.getLog().info("finished query.");
		printElapsedTime();
		AceLog.getLog().info("------------------------------------------");
		descC.close();
		start = oldStart;
	}

	private static void doQuickQuery(VodbEnv vodb, ThinDescVersionedBinding descBinder, String queryString) throws DatabaseException {
		Date oldStart = start;
		start = new Date();
		int descCount = 0;
		int matchCount = 0;
		int failCount = 0;
		Cursor descC = vodb.getDescDb().openCursor(null, null);
		AceLog.getLog().info("------------------------------------------");
		AceLog.getLog().info("Started quick query cursor for: " + queryString);
		DatabaseEntry descKey = new DatabaseEntry();
		DatabaseEntry descValue = new DatabaseEntry();
		while (descC.getNext(descKey, descValue, LockMode.DEFAULT) == OperationStatus.SUCCESS) {
			descCount++;
			/*
			ThinDescVersioned vdesc = (ThinDescVersioned) descBinder.entryToObject(descValue);
			if (vdesc.matches(queryString)) {
				matchCount++;
				AceLog.getLog().info(vdesc);
			} else {
				failCount++;
			}
			*/
		}
		AceLog.getLog().info("descCount: " + descCount);
		AceLog.getLog().info("matchCount: " + matchCount);
		AceLog.getLog().info("failCount: " + failCount);
		AceLog.getLog().info("finished  quick query.");
		printElapsedTime();
		AceLog.getLog().info("------------------------------------------");
		descC.close();
		start = oldStart;
	}

	private static void doConceptQuery(VodbEnv vodb, 
			ThinDescVersionedBinding descBinder, 
			ConceptIdKeyForDescCreator descForConceptKeyCreator,
			String queryString) throws DatabaseException {
		Date oldStart = start;
		start = new Date();
		int descCount = 0;
		int matchCount = 0;
		int failCount = 0;
		Cursor descC = vodb.getDescDb().openCursor(null, null);
		AceLog.getLog().info("------------------------------------------");
		AceLog.getLog().info("Started do concept query cursor for: " + queryString);
		DatabaseEntry descKey = new DatabaseEntry();
		DatabaseEntry descValue = new DatabaseEntry();
		while (descC.getNext(descKey, descValue, LockMode.DEFAULT) == OperationStatus.SUCCESS) {
			descCount++;
			
			I_DescriptionVersioned descFromTextSearch = (I_DescriptionVersioned) descBinder.entryToObject(descValue);
			if (descFromTextSearch.matches(Pattern.compile(queryString))) {
				matchCount++;
				AceLog.getLog().info("   ---------------------------------------");
				AceLog.getLog().info(descFromTextSearch.toString());
				AceLog.getLog().info("   ---------------------------------------");
				
				DatabaseEntry secondaryKey = 
			        new DatabaseEntry();
				
				descForConceptKeyCreator.createSecondaryKey(Integer.MIN_VALUE, descFromTextSearch.getConceptId(), secondaryKey);

			    DatabaseEntry foundData = new DatabaseEntry();

			    SecondaryCursor mySecCursor = 
			    	vodb.getConceptDescMap().openSecondaryCursor(null, null);

			    OperationStatus retVal = mySecCursor.getSearchKeyRange(secondaryKey, 
			                                                      foundData, 
			                                                      LockMode.DEFAULT);
				Collection<ThinDescVersioned> matches = new ArrayList<ThinDescVersioned>();				
			    while (retVal == OperationStatus.SUCCESS) {
			    	ThinDescVersioned descFromConceptId = (ThinDescVersioned) descBinder.entryToObject(foundData);
			    	if (descFromConceptId.getConceptId() == descFromTextSearch.getConceptId()) {
			    		matches.add(descFromConceptId);
			    	} else {
			    		break;
			    	}
			        retVal = mySecCursor.getNext(secondaryKey, foundData, LockMode.DEFAULT);
			    } 		
				mySecCursor.close();
				AceLog.getLog().info("++++++++ Concept Query Result(s) ++++++++++");
				for (I_DescriptionVersioned desc: matches) {
					AceLog.getLog().info(desc.toString());
				}
				AceLog.getLog().info("+++++++++++++++++++++++++++++++++++++++++++");
			} else {
				failCount++;
			}
		}
		AceLog.getLog().info("descCount: " + descCount);
		AceLog.getLog().info("matchCount: " + matchCount);
		AceLog.getLog().info("failCount: " + failCount);
		AceLog.getLog().info("finished  quick query.");
		printElapsedTime();
		AceLog.getLog().info("------------------------------------------");
		descC.close();
		start = oldStart;
	}

	private static void printElapsedTime() {
		Date end = new Date();
		long elapsed = end.getTime() - start.getTime();
		elapsed = elapsed / 1000;
		AceLog.getLog().info("Elapsed sec: " + elapsed);
		elapsed = elapsed / 60;
		AceLog.getLog().info("Elapsed min: " + elapsed);
		AceLog.getLog().info(end.toString());
	}

}
