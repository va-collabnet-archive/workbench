package org.ihtsdo.rf2.impl;

import java.io.BufferedWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseFactory;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderFactory;
import org.drools.builder.ResourceType;
import org.drools.definition.KnowledgePackage;
import org.drools.io.ResourceFactory;
import org.dwfa.ace.api.I_AmTermComponent;
import org.dwfa.ace.api.I_ConceptAttributeTuple;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_DescriptionVersioned;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IdPart;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_ManageContradiction;
import org.dwfa.ace.api.I_RelTuple;
import org.dwfa.ace.api.I_RelVersioned;
import org.dwfa.ace.api.I_ShowActivity;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.PositionSetReadOnly;
import org.dwfa.ace.api.Terms;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.cement.SNOMED;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.rf2.constant.I_Constants;
import org.ihtsdo.rf2.core.factory.RF2ConceptFactory;
import org.ihtsdo.rf2.util.Config;
import org.ihtsdo.rf2.util.ExportUtil;
import org.ihtsdo.tk.api.NidSet;
import org.ihtsdo.tk.api.NidSetBI;
import org.ihtsdo.tk.api.PositionBI;
import org.ihtsdo.tk.api.Precedence;
import org.ihtsdo.tk.api.RelAssertionType;
import org.ihtsdo.tk.api.coordinate.PositionSet;

public abstract class RF2IDImpl {

	private static Config config;

	private static Logger logger = Logger.getLogger(RF2IDImpl.class);

	
	public static Config getConfig() {
		return config;
	}

	public static void setConfig(Config config) {
		RF2IDImpl.config = config;
	}

	public String getTimeFormat() {
		return ExportUtil.TIMEFORMAT;
	}

	

	public RF2IDImpl(Config config) {

		RF2IDImpl.config = config;

		
	}

	

	public I_ConfigAceFrame getAceConfig() {
		return ExportUtil.getAceConfig();
	}

	

	/*
	 * public static String getModuleID(String snomedIntegerId) { return ExportUtil.getModuleID(snomedIntegerId); }
	 */
	public void closeFileWriter(BufferedWriter bw) throws IOException {
		if (bw != null)
			bw.close();
	}

	

	public String getSctId(int nid, int pathNid) throws IOException, TerminologyException {
		return ExportUtil.getSctId(nid, pathNid);
	}

	// get the description id for the given UUID
	public String getSCTId(Config config, UUID uuid) throws Exception {
		return ExportUtil.getSCTId(config, uuid);
	}

	public String getConceptId(I_GetConceptData concept, int snomedCorePathNid) throws IOException, TerminologyException {
		return ExportUtil.getConceptId(concept, snomedCorePathNid);
	}

	

	public abstract void generateIdentifier() throws IOException;

	
}
