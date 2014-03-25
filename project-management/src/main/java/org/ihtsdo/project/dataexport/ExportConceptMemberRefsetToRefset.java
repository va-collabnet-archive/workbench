/*
 * Copyright (c) 2010 International Health Terminology Standards Development
 * Organisation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ihtsdo.project.dataexport;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_HelpRefsets;
import org.dwfa.ace.api.I_Identify;
import org.dwfa.ace.api.I_ProcessConcepts;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.PositionSetReadOnly;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.api.ebr.I_ExtendByRef;
import org.dwfa.ace.api.ebr.I_ExtendByRefPart;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartCid;
import org.dwfa.ace.refset.spec.I_HelpSpecRefset;
import org.dwfa.ace.task.refset.members.RefsetUtilImpl;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.ihtsdo.project.TerminologyProjectDAO;
import org.ihtsdo.project.refset.ConceptMembershipRefset;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.PathBI;
import org.ihtsdo.tk.api.PositionBI;
import org.ihtsdo.tk.api.PositionSet;
import org.ihtsdo.tk.api.PositionSetBI;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.api.refex.RefexChronicleBI;
import org.ihtsdo.tk.api.refex.RefexVersionBI;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRf2;

/**
 * The Class ExportConceptMemberRefsetToRefset.
 */
public class ExportConceptMemberRefsetToRefset {

    /**
     * The output file writer.
     */
    BufferedWriter outputFileWriter;
    /**
     * The report file writer.
     */
    BufferedWriter reportFileWriter;
    /**
     * The line count.
     */
    int lineCount;
    /**
     * The refset concept.
     */
    ConceptMembershipRefset refsetConcept;
    /**
     * The refset uuid.
     */
    UUID refsetUUID;
    /**
     * The refset helper.
     */
    I_HelpRefsets refsetHelper;
    /**
     * The term factory.
     */
    I_TermFactory termFactory;
    /**
     * The id.
     */
    I_Identify id;
    /**
     * The formatter.
     */
    SimpleDateFormat formatter;
    SimpleDateFormat formatter2;
    /**
     * The output file writer2.
     */
    private BufferedWriter outputFileWriter2;
	private BufferedWriter outputFileWriter3;

    /**
     * Instantiates a new export concept member refset to refset.
     * @param rf2Format 
     */
    public ExportConceptMemberRefsetToRefset() {
        termFactory = Terms.get();
        formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        formatter2 = new SimpleDateFormat("MMM dd, yyyy HH:mm:ss");
    }

    /**
     * Export file.
     *
     * @param exportFile the export file
     * @param exportFile2 the export file2
     * @param reportFile the report file
     * @param reportFile2, File reportFile3 
     * @param refsetConcept the refset concept
     * @param exportToCsv the export to csv
     * @return the long[]
     * @throws Exception the exception
     */
    public Long[] exportFile(File exportFile, File exportFile2, File exportFile3, File reportFile, I_GetConceptData refsetConcept, boolean exportToCsv) throws Exception {

        reportFileWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(reportFile), "UTF8"));
        outputFileWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(exportFile), "UTF8"));
        outputFileWriter2 = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(exportFile2), "UTF8"));
        outputFileWriter3 = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(exportFile3), "UTF8"));

        String begEnd;
        String sep;
        if (exportToCsv) {
            sep = "\",\"";
            begEnd = "\"";
        } else {
            sep = "\t";
            begEnd = "";
        }
        long UUIDlineCount = 0;
        long SCTIDlineCount = 0;
        String refsetId = refsetConcept.getUUIDs().iterator().next().toString();
        Collection<? extends I_ExtendByRef> extensions = Terms.get().getRefsetExtensionMembers(
                refsetConcept.getConceptNid());

        if (extensions.size() == 0) {
        	AnnotationProcesser processor = new AnnotationProcesser(refsetConcept.getConceptNid());
			Terms.get().iterateConcepts(processor);
			extensions = processor.getExtensions();
        }
        
        I_ConfigAceFrame config = termFactory.getActiveAceFrameConfig();

        RefsetUtilImpl rUtil = new RefsetUtilImpl();

        String refsetSCTID = rUtil.getSnomedId(refsetConcept.getNid(), Terms.get()).toString();
        try {
            Long.parseLong(refsetSCTID);
        } catch (NumberFormatException e) {

            reportFileWriter.append("The refset UUID " + refsetId
                    + " has not Snomed Concept ID, It will be replaced with its UUID." + "\r\n");

            refsetSCTID = refsetId;
        }
        I_HelpSpecRefset helper = termFactory.getSpecRefsetHelper(config);
        for (I_ExtendByRef ext : extensions) {

            long lastVersionTime = 0;
            I_ExtendByRefPart lastPart = null;
            for (I_ExtendByRefPart<?> loopTuple : ext.getTuples()) {
    			if (loopTuple.getTime() > lastVersionTime) {
    				lastVersionTime = loopTuple.getTime();
    				lastPart = loopTuple;
    			}
    		}

    		if (lastPart == null) {
    			continue;
    		}

            if (lastPart.getStatusNid() == ArchitectonicAuxiliary.Concept.RETIRED.localize().getNid() || 
        		lastPart.getStatusNid() == SnomedMetadataRf2.INACTIVE_VALUE_RF2.getLenient().getNid()) {
            	continue;
        	} else {
            	
//
//			List<? extends I_ExtendByRefVersion> tuples = ext.getTuples(helper.getCurrentStatusIntSet(), null, 
//					config.getPrecedence(),
//					config.getConflictResolutionStrategy());
//
//			if (tuples.size() > 0) {
//				I_ExtendByRefVersion thinTuple = tuples.get(0);

                //UUID file
                String id = ext.getUUIDs().iterator().next().toString();
                String effectiveTime = formatter.format(lastPart.getTime());
                String reportTime = formatter2.format(lastPart.getTime());
                String compoID = Terms.get().getConcept(ext.getComponentNid()).getUUIDs().iterator().next().toString();
                String moduleId = Terms.get().getConcept(lastPart.getPathNid()).getUids().iterator().next().toString();

                outputFileWriter.append(begEnd + id + sep + effectiveTime + sep + "1" + sep + moduleId + sep + refsetId + sep + compoID + begEnd
                        + "\r\n");

                UUIDlineCount++;

                //SCTID File
                String conceptId = rUtil.getSnomedId(ext.getComponentNid(), termFactory);
                boolean bSkip = false;
                try {
                    Long.parseLong(conceptId);
                } catch (NumberFormatException e) {
                    reportFileWriter.append("The concept UUID " + compoID + " has not Snomed Concept ID." + "\r\n");

                    bSkip = true;
                }
//                if (!bSkip) {
                	String sctId_id = null;
                    try {
                        sctId_id = rUtil.getSnomedId(ext.getNid(), termFactory);
                        try {
                            Long.parseLong(sctId_id);
                        } catch (NumberFormatException e) {
                            sctId_id = id;
                        }
                    } catch (NullPointerException e) {
                        sctId_id = id;
                    }
                    String sctId_moduleId = rUtil.getSnomedId(lastPart.getPathNid(), termFactory);
                    try {
                        Long.parseLong(sctId_moduleId);
                    } catch (NumberFormatException e) {
                        sctId_moduleId = moduleId;
                    }
                    if (!bSkip) {
                    	outputFileWriter2.append(begEnd + sctId_id + sep + effectiveTime + sep + "1" + sep + sctId_moduleId + sep + refsetSCTID + sep + conceptId + begEnd
                    			+ "\r\n");
                    } else {
                    	outputFileWriter2.append(begEnd + sctId_id + sep + effectiveTime + sep + "1" + sep + sctId_moduleId + sep + refsetSCTID + sep + compoID + begEnd
                    			+ "\r\n");
                    }
                    if (refsetConcept.getInitialText().contains("JIF Reactants")) {
                    	I_ExtendByRefPartCid concExt = (I_ExtendByRefPartCid)ext;
                    	
                	outputFileWriter3.append(begEnd + conceptId + sep + getDescForCon(conceptId, compoID, bSkip) + sep + getDescForCon(concExt.getC1id()) + sep + reportTime + begEnd + "\r\n");
                    } else {
                    	outputFileWriter3.append(begEnd + conceptId + sep + getDescForCon(conceptId, compoID, bSkip) + sep + reportTime + begEnd + "\r\n");
                    }
                    
                    outputFileWriter3.flush();
                    SCTIDlineCount++;

//                }
            }
        }
        reportFileWriter.append("Exported to UUID file " + exportFile.getName() + " : " + UUIDlineCount + " lines" + "\r\n");
        reportFileWriter.append("Exported to SCTID file " + exportFile2.getName() + " : " + SCTIDlineCount + " lines" + "\r\n");
        reportFileWriter.flush();
        reportFileWriter.close();
        outputFileWriter.flush();
        outputFileWriter.close();
        outputFileWriter2.flush();
        outputFileWriter2.close();
        outputFileWriter3.flush();
        outputFileWriter3.close();

        return new Long[]{UUIDlineCount, SCTIDlineCount};
    }
    
    private String getDescForCon(int nid) {
    	try {
	        I_GetConceptData con  = Terms.get().getConcept(nid);
        	return con.getInitialText();
    	} catch (Exception e) {
    		e.printStackTrace();
    		return "<BAD CON ID>";
    	}
	}

	private String getDescForCon(String sctId_id, String compoID, boolean bSkip) {
    	try {
    		Set<I_GetConceptData> cons = null;
    		if (!bSkip) {
    			cons = Terms.get().getConcept(sctId_id);
    		} else {
    			I_GetConceptData con = Terms.get().getConcept(UUID.fromString(compoID));
    			cons = new HashSet();
    			cons.add(con);
    		}
	        if (cons.size() != 1) {
	        	throw new Exception("Id: " + sctId_id + " could not be found");
	        } else {
	        	return cons.iterator().next().getInitialText();
	        }
    	} catch (Exception e) {
    		e.printStackTrace();
    		return "<BAD CON ID>";
    	}
	}

	private class AnnotationProcesser implements I_ProcessConcepts {
    	private List<I_ExtendByRef> extensions = new ArrayList<I_ExtendByRef>();
		private int refsetNid;
		private int rootNid;

        public AnnotationProcesser(int refsetNid) {
        		this.refsetNid = refsetNid;
        		try {
					rootNid = Ts.get().getConcept(UUID.fromString("ee9ac5d2-a07c-3981-a57a-f7f26baf38d8")).getConceptNid();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
        }

        public void processConcept(I_GetConceptData concept) throws Exception {
        	if (Ts.get().isKindOf(concept.getConceptNid(), rootNid, Terms.get().getActiveAceFrameConfig().getViewCoordinate())) {
	            for (RefexChronicleBI<?> annotation : concept.getAnnotations()) {
					if (annotation.getRefexNid() == refsetNid) {
	                    long lastVersionTime = 0;
	                    RefexVersionBI lastVersion = null;
	                    for (RefexVersionBI<?> loopTuple : annotation.getVersions()) {
	            			if (loopTuple.getTime() > lastVersionTime) {
	            				lastVersionTime = loopTuple.getTime();
	            				lastVersion = loopTuple;
	            			}
	            		}

	            		if (lastVersion == null) {
	            			continue;
	            		}

	                    if (lastVersion.getStatusNid() == ArchitectonicAuxiliary.Concept.RETIRED.localize().getNid() || 
                    		lastVersion.getStatusNid() == SnomedMetadataRf2.INACTIVE_VALUE_RF2.getLenient().getNid()) {
	                    	continue;
                    	}
	                    	
	                    
	                	extensions.add((I_ExtendByRef) annotation);
	        		}
	        	}
	        } 
        }
        
		public Collection<? extends I_ExtendByRef> getExtensions() {
			return extensions;
		}
    }

}