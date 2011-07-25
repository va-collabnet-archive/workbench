package org.ihtsdo.rf2.refset.factory;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.dwfa.ace.api.Terms;
import org.ihtsdo.rf2.constant.I_Constants;
import org.ihtsdo.rf2.factory.RF2AbstractFactory;
import org.ihtsdo.rf2.model.Refset;
import org.ihtsdo.rf2.refset.impl.RF2DescriptionReferencesImpl;
import org.ihtsdo.rf2.refset.impl.RF2HistoricalAssociationImpl;
import org.ihtsdo.rf2.refset.impl.RF2ConceptReferencesImpl;
import org.ihtsdo.rf2.util.Config;

/**
 * Title: RF2HistoricalAssociationRefsetFactory Description: Creating Historical Association Refset Specific methods Copyright: Copyright (c) 2010 Company: IHTSDO
 * 
 * @author Varsha Parekh
 * @version 1.0
 */


//This refset contains Historical References and Other references Refset.

public class RF2HistoricalAssociationRefsetFactory extends RF2AbstractFactory {

	private static Logger logger = Logger.getLogger(RF2HistoricalAssociationRefsetFactory.class);

	public RF2HistoricalAssociationRefsetFactory(Config config) {
		super(config);
	}

	public void export() {
		
		try {
			logger.info("Started Historical Association Refset Export ...");

			RF2HistoricalAssociationImpl iterator = new RF2HistoricalAssociationImpl(getConfig());

			Terms.get().iterateConcepts(iterator);

			logger.info("Finished Historical Association Refset Export.");

			logger.info("Started Concept References Refset Export ...");
			
			ArrayList<Refset> refsets = new ArrayList<Refset>();

			// alternative references refset
			Refset refset = new Refset();
			refset.setName("Alternative References Refset");
			refset.setId(I_Constants.ALTERNATIVE_REFERENCES_REFSET_ID);
			refset.setTermAuxUID(I_Constants.ALTERNATIVE_REFERENCES_REFSET_UID_TERM_AUX);
			refset.setUID(I_Constants.ALTERNATIVE_REFERENCES_REFSET_UID);			
			refsets.add(refset);

			// replaced references refset
			refset = new Refset();
			refset.setName("Replaced References Refset");
			refset.setId(I_Constants.REPLACED_REFERENCES_REFSET_ID);
			refset.setTermAuxUID(I_Constants.REPLACED_REFERENCES_REFSET_UID_TERM_AUX);
			refset.setUID(I_Constants.REPLACED_REFERENCES_REFSET_UID);
			refsets.add(refset);

			// duplicate references refset
			refset = new Refset();
			refset.setName("Duplicate References Refset");
			refset.setId(I_Constants.DUPLICATE_REFERENCES_REFSET_ID);
			refset.setTermAuxUID(I_Constants.DUPLICATE_REFERENCES_REFSET_UID_TERM_AUX);
			refset.setUID(I_Constants.DUPLICATE_REFERENCES_REFSET_UID);
			refsets.add(refset);

			// moved from references refset
			refset = new Refset();
			refset.setName("Moved From References Refset");
			refset.setId(I_Constants.MOVED_FROM_REFERENCES_REFSET_ID);
			refset.setTermAuxUID(I_Constants.MOVED_FROM_REFERENCES_REFSET_UID_TERM_AUX);
			refset.setUID(I_Constants.MOVED_FROM_REFERENCES_REFSET_UID);
			refsets.add(refset);

			// moved to references refset
			refset = new Refset();
			refset.setName("Moved To References Refset");
			refset.setId(I_Constants.MOVED_TO_REFERENCES_REFSET_ID);
			refset.setTermAuxUID(I_Constants.MOVED_TO_REFERENCES_REFSET_UID_TERM_AUX);
			refset.setUID(I_Constants.MOVED_TO_REFERENCES_REFSET_UID);
			refsets.add(refset);

			// similar references refset
			refset = new Refset();
			refset.setName("Similar References Refset");
			refset.setId(I_Constants.SIMILAR_REFERENCES_REFSET_ID);
			refset.setTermAuxUID(I_Constants.SIMILAR_REFERENCES_REFSET_UID_TERM_AUX);
			refset.setUID(I_Constants.SIMILAR_REFERENCES_REFSET_UID);
			refsets.add(refset);
			
			// was a references refset
			refset = new Refset();
			refset.setName("WAS A association reference set");
			refset.setId(I_Constants.WAS_A_REFERENCES_REFSET_ID);
			refset.setTermAuxUID(I_Constants.WAS_A_REFERENCES_REFSET_UID);
			refset.setUID(I_Constants.WAS_A_REFERENCES_REFSET_UID);
			refsets.add(refset);

			// POSSIBLY EQUIVALENT TO association reference set
			refset = new Refset();
			refset.setName("POSSIBLY EQUIVALENT TO association reference set");
			refset.setId(I_Constants.POSSIBLY_EQUIVALENT_REFERENCES_REFSET_ID);
			refset.setTermAuxUID(I_Constants.POSSIBLY_EQUIVALENT_REFERENCES_REFSET_UID);
			refset.setUID(I_Constants.POSSIBLY_EQUIVALENT_REFERENCES_REFSET_UID);
			refsets.add(refset);
			

			for (int i = 0; i < refsets.size(); i++) {
				logger.info("Started " + refsets.get(i).getName() + " ...");
				RF2ConceptReferencesImpl referencesIterator = new RF2ConceptReferencesImpl(getConfig(), refsets.get(i));
				Terms.get().iterateConcepts(referencesIterator);
				logger.info("Finished " + refsets.get(i).getName() + ".");
			}

			logger.info("Finished Concept References Refset Export.");
			
			logger.info("Started Description References Refset Export ...");
			
			refsets = new ArrayList<Refset>();

			// refers references refset
			refset = new Refset();
			refset.setName("Refers References Refset");
			refset.setId(I_Constants.REFERS_REFERENCES_REFSET_ID);
			refset.setTermAuxUID(I_Constants.REFERS_REFERENCES_REFSET_UID_TERM_AUX);
			refset.setUID(I_Constants.REFERS_REFERENCES_REFSET_UID);
			refsets.add(refset);

			for (int i = 0; i < refsets.size(); i++) {
				logger.info("Started " + refsets.get(i).getName() + " ...");
				RF2DescriptionReferencesImpl referencesIterator = new RF2DescriptionReferencesImpl(getConfig(), refsets.get(i));
				Terms.get().iterateConcepts(referencesIterator);
				logger.info("Finished " + refsets.get(i).getName() + ".");
			}
			
			logger.info("Finished Description References Refset Export.");

			closeExportFileWriter();

			
			
		} catch (IOException e) {
			logger.error(e.getMessage());
		} catch (Exception e) {
			logger.error(e.getMessage());
		}		
		
	}
}
