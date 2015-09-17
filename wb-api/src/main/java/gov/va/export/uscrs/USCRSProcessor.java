package gov.va.export.uscrs;

import gov.va.export.uscrs.USCRSBatchTemplate.PICKLIST_Source_Terminology;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.dwfa.cement.ArchitectonicAuxiliary;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.ComponentVersionBI;
import org.ihtsdo.tk.api.ContradictionException;
import org.ihtsdo.tk.api.NidSetBI;
import org.ihtsdo.tk.api.concept.ConceptChronicleBI;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.api.description.DescriptionChronicleBI;
import org.ihtsdo.tk.api.description.DescriptionVersionBI;
import org.ihtsdo.tk.api.id.IdBI;
import org.ihtsdo.tk.api.id.LongIdBI;
import org.ihtsdo.tk.api.refex.RefexChronicleBI;
import org.ihtsdo.tk.api.refex.RefexVersionBI;
import org.ihtsdo.tk.api.refex.type_nid.RefexNidVersionBI;
import org.ihtsdo.tk.binding.snomed.RefsetAux;
import org.ihtsdo.tk.binding.snomed.Snomed;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRf1;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRf2;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRfx;
import org.ihtsdo.tk.spec.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class USCRSProcessor {
	private static final Logger LOG = LoggerFactory.getLogger(USCRSProcessor.class);

	private static USCRSBatchTemplate bt;
	private static UscrsContentRequestTrackingInfo info;
	static int snomedRootNid;
	static int namespace;
	static ViewCoordinate vc;
	static ViewCoordinate vcAllStatus;
	
	private static final int US_EXTENSION_MODULE_NID = -2144087550;
    private static final UUID SNOMED_ROOT_UUID = UUID.fromString("ee9ac5d2-a07c-3981-a57a-f7f26baf38d8");
	
    static Map<Integer, Integer> currentRequestMap = new HashMap<Integer, Integer>();
	static Set<Integer> newConceptRequestIds = new HashSet<Integer>();
	private static int globalRequestCounter = 1;
	

	static NidSetBI activeStatusNids;

	private static String UNASSIGNED_SCTID = "unassigned";

	USCRSProcessor() {
		newConceptRequestIds.clear();
		globalRequestCounter = 1;
		currentRequestMap.clear();
	}
	
	USCRSProcessor(USCRSBatchTemplate batchTemplate, 
				 UscrsContentRequestTrackingInfo trackingInfo, 
				 File outputFile, 
				 ViewCoordinate viewCoordinate, 
				 int ns) 
	throws IOException {
		bt = batchTemplate;
		bt.saveFile(outputFile);
		
		info = trackingInfo;		
		info.setIsSuccessful(true);
		info.setFile(outputFile);
		info.setDetail("Batch USCRS submission spreadsheet successfully created.");

		vc = viewCoordinate;
		activeStatusNids = vc.getAllowedStatusNids();
		vcAllStatus = new ViewCoordinate(vc);
		vcAllStatus.setAllowedStatusNids(null);

		namespace = ns;
		snomedRootNid = Ts.get().getNidForUuids(SNOMED_ROOT_UUID);
		
		newConceptRequestIds.clear();
		globalRequestCounter = 1;
		currentRequestMap.clear();
	}
	

	UscrsContentRequestTrackingInfo getInfo() {
		return info;
	}
	
	USCRSBatchTemplate getBt() {
		return bt;
	}
	
	String getTerminology(ConceptVersionBI con) throws Exception {
		try {
			int moduleNid = con.getModuleNid();
			
			if (currentRequestMap.containsKey(con.getConceptNid()))
			{
				return PICKLIST_Source_Terminology.Current_Batch_Requests.toString();
			}
	
			//If it was done on core or us extension - assume it was pre-existing.
			//TODO This isn't 100% safe, as the user may have used this module when they did
			//a previous submission - but at the moment, we don't have any way of knowing
			//what IDs were previously submitted - so we can't choose between on of these 
			//official constants, and "New Concept Request"
			if(moduleNid == Snomed.CORE_MODULE.getLenient().getNid()) {
				return PICKLIST_Source_Terminology.SNOMED_CT_International.toString();
			}
			else if (moduleNid == US_EXTENSION_MODULE_NID) {
				return PICKLIST_Source_Terminology.SNOMED_CT_National_US.toString();
			}
			//These, we know would be invalid
			else if (moduleNid == ArchitectonicAuxiliary.Concept.LOINC.localize().getNid()) {
				throw new Exception("Cannot export LOINC Terminology");
			}
			else if (moduleNid == ArchitectonicAuxiliary.Concept.RX_NORM.localize().getNid()) {
				throw new Exception("Cannot export RxNorm Terminology");
			}
			else if (!isChildOfSnomedRoot(con)) {
				throw new Exception("Cannot something that isn't part of the SCT hierarchy");
			}
			else
			{
				//The only thing we can do at this point, is assume it was a previously submitted
				//item.
				//TODO this isn't 100% safe - we need to have a permanent store of IDs that were 
				//previously submitted.
				return PICKLIST_Source_Terminology.New_Concept_Requests.toString();
			}
		} catch(EnumConstantNotPresentException ecnpe) {
			LOG.error("USCRS PICKLIST API Missing Justification Value " + ecnpe.constantName());
			return "";
		} catch(Exception e) {
			LOG.error("USCRS Justification Type Error");
			return "";
		}
	}
	
	static boolean isChildOfSnomedRoot(ComponentVersionBI cv) throws IOException, ContradictionException 
	{
		return  Ts.get().isKindOf(cv.getConceptNid(), snomedRootNid, vc);
	}
	
	private static String getSctId(ComponentVersionBI comp) throws IOException
	{
		for (IdBI id : comp.getAllIds()) {
			if (id.getAuthorityNid() == Ts.get().getNidForUuids(ArchitectonicAuxiliary.Concept.SNOMED_INT_ID.getUids())) 
			{
				Long retId = ((LongIdBI) id).getDenotation();
				return Long.toString(retId);
			}
		}
		
		return UNASSIGNED_SCTID ;
	}
	
	static String getCompSctId(ComponentVersionBI comp) throws IOException
	{
		return getSctId(comp);
	}
	
	static String getConSctId(ConceptVersionBI con) throws IOException
	{
		String retVal = getSctId(con);
		
		if (retVal.equals(UNASSIGNED_SCTID)) {
			if(!currentRequestMap.containsKey(con.getConceptNid())) {
				currentRequestMap.put(con.getConceptNid(), globalRequestCounter++);
			}
			return Integer.toString(currentRequestMap.get(con.getConceptNid()));
		}
		
		return retVal;
	}

	String getTopic(ConceptChronicleBI concept) throws Exception {
		String fsn = getFsn(concept);

		if (fsn.indexOf('(') != -1)
		{
			String st = fsn.substring(fsn.lastIndexOf('(') + 1, fsn.lastIndexOf(')'));
			return st;
		} else {
			return null;
		}
	}
	

	String getFsn(ConceptChronicleBI concept) throws ValidationException, IOException {
		if (concept.getDescriptions() != null) 
		{
			for (DescriptionChronicleBI desc : concept.getDescriptions()) 
			{
				int versionCount = desc.getVersions().size();
				DescriptionVersionBI<?> descVer = desc.getVersions().toArray(new DescriptionVersionBI[versionCount])[versionCount - 1];
				if (descVer.getTypeNid() == SnomedMetadataRf2.FULLY_SPECIFIED_NAME_RF2.getLenient().getNid() 
						|| descVer.getTypeNid() == SnomedMetadataRf1.FULLY_SPECIFIED_DESCRIPTION_TYPE.getLenient().getNid()) 
				{
					if (descVer.getStatusNid() ==  SnomedMetadataRfx.getSTATUS_CURRENT_NID()) 
					{
						return descVer.getText();
					}
				}
			}
		}
		return null;
	}


	/**
	 * Create a Note Cell, and print the row's NID and Description for testing. If testing is
	 *  disabled then print the value of alternateNote
	 * @param nid
	 * @param alternateNote if testing is disabled, print this instead
	 * @throws ContradictionException 
	 * @throws ValidationException 
	 */
	String getNote(ComponentVersionBI comp) throws IOException {
		return getNote(comp, "");
	}
	
	/**
	 * Create a Note Cell, and print the row's NID and Description for testing. If testing is
	 *  disabled then print the value of alternateNote
	 * @param nid
	 * @param alternateNote if testing is disabled, print this instead
	 * @return 
	 * @throws IOException 
	 * @throws ContradictionException 
	 * @throws ValidationException 
	 */
	String getNote(ComponentVersionBI comp, String alternateNote) throws IOException  {
		String sctId = getCompSctId(comp);
		
		if (!sctId.equals(UNASSIGNED_SCTID)) {
			return "SCT ID: " + getCompSctId(comp);
		} else {
			return "UUID: " + comp.getPrimUuid();
		}
	}
	
	String getJustification() {
		return "Developed as part of extension namespace " + namespace; 
	}

	public Map<Integer, Integer> getCurrentRequestMap() {
		return currentRequestMap;
	}

	public Set<Integer> getNewConceptRequestIds() {
		return newConceptRequestIds;
	}
	
	String getPreferredTerm(ConceptVersionBI con, ViewCoordinate vc) throws ContradictionException, IOException {
		for (DescriptionChronicleBI d : con.getDescriptions()) {
			DescriptionVersionBI<?> dv = d.getVersion(vc);
			
			if (isPreferredTerm(dv)) {
				return dv.getText(); 
			}
		}
		
		return null;
	}
	
    boolean isPreferredTerm(DescriptionVersionBI<?> desc) {
        try {
        	if (desc.getTypeNid() == SnomedMetadataRf2.SYNONYM_RF2.getLenient().getNid()) {
        		 Collection<? extends RefexChronicleBI<?>> annotations = desc.getAnnotations();
        		 
        		 for (RefexChronicleBI<?> annot : annotations) {
        			 // Is it in EN_US Refset?
        			 if (annot.getRefexNid() == RefsetAux.EN_US_REFEX.getLenient().getNid()) {
                        
        				 // Is it versionable?
        				 if (RefexVersionBI.class.isAssignableFrom(annot.getClass())) {
                            RefexVersionBI<?> rv = (RefexVersionBI<?>) annot;

                            // Is it a CidRefset Member?
                            if (RefexNidVersionBI.class.isAssignableFrom(rv.getClass())) {
                                int cnid = ((RefexNidVersionBI<?>) rv).getNid1();
                                
                                // Is the Cid Preferred?
                                if (cnid == SnomedMetadataRfx.getDESC_PREFERRED_NID()) {
                                    return true;
                                }
                            } else {
                                System.out.println("Can't convert: RefexCnidVersionBI:  " + rv);
                            }
                        } else {
                            System.out.println("Can't convert: RefexVersionBI:  " + annot);
                        }
                    }
                }
            }
        	
        } catch (IOException e) {
        	LOG.error("Error finding if desc is Preferred Term on : " + desc.getText() + "(" + desc.getNid() +")");
    	}

        return false;
    }

}
