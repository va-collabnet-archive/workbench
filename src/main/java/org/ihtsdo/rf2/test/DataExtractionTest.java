package org.ihtsdo.rf2.test;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import junit.framework.TestCase;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_DescriptionVersioned;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IdVersion;
import org.dwfa.ace.api.I_Identify;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.ihtsdo.rf2.core.factory.RF2ConceptFactory;
import org.ihtsdo.rf2.util.Config;
import org.ihtsdo.rf2.util.ExportUtil;
import org.ihtsdo.rf2.util.JAXBUtil;

public class DataExtractionTest extends TestCase {
	private ExportUtil _exportUtil;
	private RF2ConceptFactory _conceptExport;
	private static I_TermFactory _termfactory;
	String timeFormat = "yyyyMMdd"; // 20100305 Per SC no TZ only YMD
	SimpleDateFormat _dateFormat = new SimpleDateFormat(timeFormat);

	public void setUp() {
		try {

			Config config = JAXBUtil.getConfig("/org/ihtsdo/rf2/config/concept.xml");

			config.setReleaseDate("20100731");
			config.setFileExtension("txt");

			config.setOutputFolderName("export");
			config.setDroolsDrlFile("org/ihtsdo/rf2/core/drools/rules/ConceptReleaseRules.drl");

			RF2ConceptFactory factory = new RF2ConceptFactory(config);
			factory.export();

		} catch (Exception e) {
			System.err.println("Unable to connect to the berkeley database" + e.getMessage());
		}
	}

	public void tearDown() {

	}

	public void testCreate() {
		System.out.println("*** DataExtractionTests.testCreate enter.");
		// assertNotNull(_dataExtraction);
		// assertNotNull(_I_ProcessConceptsImpl);
	}

	public void testSetUp() {
		System.out.println("*** DataExtractionTests.testSetUp enter.");
		try {
			// _termfactory = _dataExtraction.setUp();
			// _dataExtraction.getConceptData();
			// String uuid= "ee9ac5d2-a07c-3981-a57a-f7f26baf38d8";
			// String uuid= "81eab277-6c7b-3b2f-b5a5-6effda091a73";
			// int nid = _dataExtraction.getNid(uuid);
			// System.out.println("nid for the concept is " + nid);
			// check if this concept is in snomedPath
			// boolean onSnomedPathFlag = _dataExtraction.isOnPath(-2147483077, nid);
			// System.out.println("onSnomedPathFlag " + onSnomedPathFlag);
			// _dataExtraction.getModuleID("900000000000533001");

		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}

	public void xtestGetAllConceptData() {
		System.out.println("*** DataExtractionTests.testGetAllConceptData enter.");
		try {
			_conceptExport.export();
			_termfactory.close();
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}

	
	public void xtestGetConfig() {
		System.out.println("*** DataExtractionTests.testGetConfig() enter.");
		I_ConfigAceFrame config = ExportUtil.getAceConfig();
		System.out.println("=======Admin Username==========" + config.getUsername());
		assertNotNull(config);
	}

	public void xtestIsOnPath(int onPath, int nid) {
		try {
			_exportUtil.isOnPath(onPath, nid);
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}
}
