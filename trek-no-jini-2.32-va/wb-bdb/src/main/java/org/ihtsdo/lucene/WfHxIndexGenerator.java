package org.ihtsdo.lucene;
 
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;
import java.util.logging.Level;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.LongField;
import org.apache.lucene.index.IndexWriter;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_ProcessConcepts;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.log.AceLog;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.ihtsdo.concept.Concept;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.workflow.WorkflowHistoryJavaBean;
import org.ihtsdo.workflow.WorkflowHistoryRefsetSearcher;
import org.ihtsdo.workflow.refset.utilities.WorkflowHelper;

public class WfHxIndexGenerator extends IndexGenerator {
	private ViewCoordinate viewCoord;
	private static File inputFile = null;
    private static Set<WorkflowHistoryJavaBean> wfHxJavaBeansToWrite  = new HashSet<WorkflowHistoryJavaBean>();
    private static Map<UUID, WorkflowLuceneSearchResult> lastBeanInWfMap = new HashMap<UUID, WorkflowLuceneSearchResult>();
    private static SortedSet<String> semanticTags = null;
    private HashSet<UUID> conceptsImported = new HashSet<UUID>();
    private int membersImported = 0;
    
	public WfHxIndexGenerator(IndexWriter writer, ViewCoordinate vc) throws IOException, ParseException {
		super(writer);
		WorkflowLuceneSearchResult vals = null;
		viewCoord = vc;
		
		try {
			// Prepare Generator
        	initializeSemTags(vc);
        	
	    	lastBeanInWfMap.clear();

			if (inputFile != null) {
				// Handle if have WfHx Refset file (wfHx.txt)
				String currentWfId = new String();
				String line = null;
				String[] curLastRow = null;
				
	        	BufferedReader reader = new BufferedReader(new FileReader(inputFile));    	

	        	WorkflowLuceneSearchResult.initializePossibleResults();
	        	
	        	// Read file
	        	while ((line = reader.readLine()) != null)
	        	{
	        		if (line.trim().length() == 0) {
	        			continue;
	        		}
	        		
					String[] row = line.split("\t");
					String wfId = row[WorkflowHelper.workflowIdPosition];
					
					if (curLastRow != null) {
						// Find latest row of WfId
						if (!currentWfId.equals(wfId)) {
							vals = new WorkflowLuceneSearchResult(curLastRow, viewCoord);
							lastBeanInWfMap.put(UUID.fromString(currentWfId), vals);

							curLastRow = row;
							currentWfId = wfId;
						} else {
							// Existing WfId
							String curLastRowTime = curLastRow[WorkflowHelper.refsetColumnTimeStampPosition];
							String curRowTime = row[WorkflowHelper.refsetColumnTimeStampPosition];
							
							long curLastTime = WorkflowHelper.format.parse(curLastRowTime).getTime();
							long curTime = WorkflowHelper.format.parse(curRowTime).getTime();
							
							if (curLastTime < curTime) {
								curLastRow = row;
							}
						}
					} else {
						curLastRow = row;
						currentWfId = wfId;
					}
				}

	            vals = new WorkflowLuceneSearchResult(curLastRow, viewCoord);
	            lastBeanInWfMap.put(UUID.fromString(currentWfId), vals);
	            inputFile = null;
	        } else {
				AceLog.getAppLog().log(Level.INFO, "About to process all " + Terms.get().getConceptCount() + " concepts and identify their wfHx based refset members");
	        	
				// Iterate over all concepts and identify both refset members and annotations
				WfHxRefsetMembersProcessor annotationProcessor = new WfHxRefsetMembersProcessor();
				Terms.get().iterateConcepts(annotationProcessor);

				AceLog.getAppLog().log(Level.INFO, "WfHx Initialization completed having imported wfHx members into Lucene");
				AceLog.getAppLog().log(Level.INFO, "Unique concepts imported: " + conceptsImported.size() + " with " + membersImported + " unique members");
			}
		} catch (Exception e) {
			if (vals != null) {
				AceLog.getAppLog().log(Level.WARNING, "Lucene Creation Issues #3 on bean: " + vals.toString() + " with error: " + e.getMessage());
			} else {
				AceLog.getAppLog().log(Level.WARNING, "Lucene Creation Issues #3 on bean with null vals with error: " + e.getMessage());
			}
		}
	}

	static void initializeSemTags(ViewCoordinate vc) {
		if (semanticTags == null) {
			try {
				I_GetConceptData parentSemTagConcept = Terms.get().getConcept(ArchitectonicAuxiliary.Concept.SEMTAGS_ROOT.getPrimoridalUid());				
				semanticTags = new TreeSet<String>();

				if (vc != null) {
					Set<ConceptVersionBI> semTagConcepts = WorkflowHelper.getChildren(parentSemTagConcept.getVersion(vc));

			    	for (ConceptVersionBI con : semTagConcepts) {
			    		String semTag = con.getDescriptionFullySpecified().getText();
			    		
			    		semTag = WorkflowHelper.parseSpaces(semTag);
			
			    		semanticTags.add(semTag);
			    	}
				} else {
					Set<I_GetConceptData> semTagConcepts = WorkflowHelper.getChildren(parentSemTagConcept);
					
			    	for (I_GetConceptData con : semTagConcepts) {
			    		String semTag = con.getInitialText();
			    		
			    		semTag = WorkflowHelper.parseSpaces(semTag);
			
			    		semanticTags.add(semTag);
			    	}
				}					
			} catch (Exception e) {
			    AceLog.getAppLog().info("Error initializing semantic tags for wf lucene index generator with error,: " + e.getMessage());
			}
		}
	}

	@Override
    public void processConceptData(Concept concept) throws Exception {
		AceLog.getAppLog().log(Level.WARNING, "Do not use I_ProcessConcept mechanism to load WfHx into Lucene");
    }

    public void initializeExistingWorkflow() {
		ViewCoordinate vc = null;
		
		try {
			if (Terms.get().getActiveAceFrameConfig() != null) {
				vc = Terms.get().getActiveAceFrameConfig().getViewCoordinate();
			}
			
			WfHxLuceneManager.writeToLuceneNoLock(wfHxJavaBeansToWrite, lastBeanInWfMap, vc);
			wfHxJavaBeansToWrite.clear();
			lastBeanInWfMap.clear();
		} catch (Exception e) {
			e.printStackTrace();	
		}
    }
    
	public static Document createDoc(WorkflowHistoryJavaBean bean, WorkflowLuceneSearchResult lastBeanInWf) {
		Document doc = new Document();
		
		// Refset Member Id (nid) for Index
		doc.add(new Field("memberId", Integer.toString(bean.getRefexMemberNid()), Field.Store.YES, Field.Index.NO));

		// ConceptId (UUID) so don't need to reference DB for Terms.get().getRefsetExtensionsForComponent() ... Therfore, analyzed
		doc.add(new Field("conceptId", bean.getConcept().toString(), Field.Store.YES, Field.Index.NO));
		doc.add(new Field("fsn", bean.getConcept().toString(), Field.Store.YES, Field.Index.NO));

		// WorkflowId (UUID) so don't need to create bean just to find it
		doc.add(new Field("workflowId", bean.getWorkflowId().toString(), Field.Store.YES, Field.Index.NOT_ANALYZED));
 
		// Action, State, Path, Modeler
		doc.add(new Field("action", bean.getAction().toString(), Field.Store.YES, Field.Index.NOT_ANALYZED));
		doc.add(new Field("state", bean.getState().toString(), Field.Store.YES, Field.Index.NOT_ANALYZED));
		doc.add(new Field("path", bean.getPath().toString(), Field.Store.YES, Field.Index.NOT_ANALYZED));
		doc.add(new Field("modeler", bean.getModeler().toString(), Field.Store.YES, Field.Index.NOT_ANALYZED));
		
		// Calculate from Sem-Tag
		doc.add(new Field("semTag", parsePotentialSemTag(bean.getFullySpecifiedName()), Field.Store.YES, Field.Index.NOT_ANALYZED));
		
		// Keep Time
		doc.add(new LongField("time", bean.getWorkflowTime(), Field.Store.YES));

		// Last Action, State, Path, Modeler in Workflow
		doc.add(new Field("lastAction", lastBeanInWf.action, Field.Store.YES, Field.Index.NOT_ANALYZED));
		doc.add(new Field("lastState", lastBeanInWf.state, Field.Store.YES, Field.Index.NOT_ANALYZED));
		doc.add(new Field("lastModeler", lastBeanInWf.modeler, Field.Store.YES, Field.Index.NOT_ANALYZED));
		doc.add(new Field("lastTime", Long.toString(lastBeanInWf.time), Field.Store.YES, Field.Index.NO));

		return doc;
    }

	private static String parsePotentialSemTag(String fsn) {
		String semTag = WorkflowHelper.parseSemanticTag(fsn);
		if (semanticTags.contains(semTag)) {
			return semTag;
		} else {
			return "";
		}
	}
	public static void setSourceInputFile(File wfHxInputFile) {
		inputFile = wfHxInputFile;
	}
	
	
	
	
	private class WfHxRefsetMembersProcessor implements I_ProcessConcepts {

		int progressMilestone;
		int conceptsAdded = 0;
		int membersAdded = 0;

		WorkflowHistoryRefsetSearcher searcher = null;
		
		public WfHxRefsetMembersProcessor() {
	    	
			try {
				progressMilestone = Terms.get().getConceptCount() / 5;

				searcher = new WorkflowHistoryRefsetSearcher();
			} catch (Exception e ) {
				e.printStackTrace();
			}
		}
		
		@Override
		public void processConcept(I_GetConceptData concept) throws Exception {
	    	if (conceptsAdded++ % progressMilestone == 0) {
	    		AceLog.getAppLog().log(Level.INFO, "Have completed " + 2 * (conceptsAdded / progressMilestone) + "0% of the values");
	    	}
			
        	TreeSet<WorkflowHistoryJavaBean> allBeans = WorkflowHelper.getWfHxMembersAsBeans(concept);
        	membersAdded += allBeans.size();
			wfHxJavaBeansToWrite.addAll(allBeans);
    		conceptsImported.add(concept.getPrimUuid());

        	for (WorkflowHistoryJavaBean bean : allBeans) {
        		membersImported++;
        		UUID wfId = bean.getWorkflowId();

				if (!lastBeanInWfMap.containsKey(wfId)) {
		    		WorkflowHistoryJavaBean latestBean = searcher.getLatestBeanForWorkflowId(concept.getConceptNid(), wfId);
			    	
		    		if (latestBean == null) {
		    			latestBean = bean;
		    		}
		    		
		    		WorkflowLuceneSearchResult vals = new WorkflowLuceneSearchResult(latestBean);
		    		
		    		lastBeanInWfMap.put(wfId, vals);
		    	}
			}
		}
	}
}

