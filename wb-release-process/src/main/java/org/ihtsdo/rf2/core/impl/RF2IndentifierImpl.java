package org.ihtsdo.rf2.core.impl;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.dwfa.ace.api.I_ConceptAttributeTuple;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_ProcessConcepts;
import org.ihtsdo.rf2.constant.I_Constants;
import org.ihtsdo.rf2.impl.RF2AbstractImpl;
import org.ihtsdo.rf2.util.Config;
import org.ihtsdo.rf2.util.WriteUtil;
import org.ihtsdo.tk.api.Precedence;

/**
 * Title: RF2ConceptImpl Description: Iterating over all the concept in workbench and fetching all the components required by RF2 Concept File Copyright: Copyright (c) 2010 Company: IHTSDO
 * 
 * * @author Alejandro Rodriguez
 * @version 1.0
 */

public class RF2IndentifierImpl extends RF2AbstractImpl implements I_ProcessConcepts {

	private static Logger logger = Logger.getLogger(RF2IndentifierImpl.class);

	public RF2IndentifierImpl(Config config) {
		super(config);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.dwfa.ace.api.I_ProcessConcepts#processConcept(org.dwfa.ace.api. I_GetConceptData)
	 */

	@Override
	public void processConcept(I_GetConceptData concept) throws Exception {

		process(concept);

	}

	@Override
	public void export(I_GetConceptData concept, String conceptid) {
		
		//currently this file is empty so no logic is required
				
	}





}
