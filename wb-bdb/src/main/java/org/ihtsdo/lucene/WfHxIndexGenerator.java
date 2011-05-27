package org.ihtsdo.lucene;
 
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.util.Collection;
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
import org.apache.lucene.document.NumericField;
import org.apache.lucene.index.IndexWriter;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.api.ebr.I_ExtendByRef;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartStr;
import org.dwfa.ace.log.AceLog;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.concept.Concept;
import org.ihtsdo.workflow.WorkflowHistoryJavaBean;
import org.ihtsdo.workflow.WorkflowHistoryRefsetSearcher;
import org.ihtsdo.workflow.refset.history.WorkflowHistoryRefset;
import org.ihtsdo.workflow.refset.utilities.WorkflowHelper;

public class WfHxIndexGenerator extends IndexGenerator {
	private int memberCounter = 0;
	private int refsetId = 0;
    private int feedbackInterval = 100;
	private static File inputFile = null;
    private static Set<WorkflowHistoryJavaBean> wfHxJavaBeansToWrite  = new HashSet<WorkflowHistoryJavaBean>();
    private static Map<UUID, WorkflowLuceneSearchResult> lastBeanInWfMap = new HashMap<UUID, WorkflowLuceneSearchResult>();
    private static SortedSet<String> semanticTags = null;
    
	public WfHxIndexGenerator(IndexWriter writer) throws IOException, ParseException {
		super(writer);
		WorkflowLuceneSearchResult vals = null;
	
		try {
        	initializeSemTags();

        	WorkflowHistoryRefsetSearcher searcher = new WorkflowHistoryRefsetSearcher();
	    	int searcherId = searcher.getRefsetId();
			this.refsetId = searcherId;
	    	
			lastBeanInWfMap.clear();

			if (inputFile != null) {
				String currentWfId = new String();
				String line = null;
				String[] curLastRow = null;
				
	        	BufferedReader reader = new BufferedReader(new FileReader(inputFile));    	

	        	while ((line = reader.readLine()) != null)
	        	{
	        		if (line.trim().length() == 0) {
	        			continue;
	        		}
	        		
					String[] row = line.split("\t");
					String wfId = row[WorkflowHelper.workflowIdPosition];
					
					if (curLastRow != null) {
						if (!currentWfId.equals(wfId)) {
							vals = new WorkflowLuceneSearchResult(curLastRow);
							lastBeanInWfMap.put(UUID.fromString(currentWfId), vals);

							curLastRow = row;
							currentWfId = wfId;
						} else {
							
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

	            vals = new WorkflowLuceneSearchResult(curLastRow);
	            lastBeanInWfMap.put(UUID.fromString(currentWfId), vals);
	        } else {
				WorkflowHistoryRefset refset = new WorkflowHistoryRefset();
		        Collection<? extends I_ExtendByRef> members = Terms.get().getRefsetExtensionMembers(searcherId);
				System.out.println("About to process: " + members.size() + " values");
				
				for (I_ExtendByRef row : members) {
			    	UUID wfId = UUID.fromString(refset.getWorkflowIdAsString(((I_ExtendByRefPartStr)row).getStringValue()));
			    	
			    	if (!lastBeanInWfMap.containsKey(wfId)) {
			    		WorkflowHistoryJavaBean latestBean = searcher.getLatestBeanForWorkflowId(row.getComponentNid(), wfId);
			    		vals = new WorkflowLuceneSearchResult(latestBean);
			    		
			    		lastBeanInWfMap.put(wfId, vals);
			    	}
		        }
			}
			
		} catch (TerminologyException e) {
		    AceLog.getAppLog().log(Level.WARNING, "Lucene Creation Issues on bean: " + vals.toString() + " with error: " + e.getMessage());
		}
	}

	public static void initializeSemTags() {
		if (semanticTags == null) {
			try {
				I_GetConceptData parentSemTagConcept = Terms.get().getConcept(ArchitectonicAuxiliary.Concept.SEMTAGS_ROOT.getPrimoridalUid());
		    	Set<I_GetConceptData> semTagConcepts = WorkflowHelper.getChildren(parentSemTagConcept);
		    	semanticTags = new TreeSet<String>();
		    	
		    	for (I_GetConceptData con : semTagConcepts) {
		    		// FSN & Pref Term Same Tag
		    		String semTag = con.getDescriptions().iterator().next().getText();
		    		
		    		semTag = WorkflowHelper.parseSpaces(semTag);
		
		    		semanticTags.add(semTag);
		    	}
			} catch (Exception e) {
			    AceLog.getAppLog().info("Error initializing semantic tags for search with error,: " + e.getMessage());
			}
		}
	}
	
	private void updateResults() {
    	if (++memberCounter % feedbackInterval == 0) {
            System.out.print(".");
            if (++lineCounter > 80) {
                lineCounter = 0;
                System.out.println();
                System.out.print("members:" + memberCounter);
            }
        }
	}

	@Override
    public void processConceptData(Concept concept) throws Exception {
		AceLog.getAppLog().log(Level.WARNING, "Do not use I_ProcessConcept mechanism to load WfHx into Lucene");
    }

    public void initializeWfHxLucene() throws Exception {
    	
    	Collection<? extends I_ExtendByRef> members = Terms.get().getRefsetExtensionMembers(refsetId);
    	
    	if (memberCounter == 0) {
            System.out.print("WfHx Lucene Import: ");
    	}
    	
        for ( I_ExtendByRef row : members) {
        	// ADD TO Lucene Doc

        	writer.addDocument(createDoc(row));
        	updateResults();
        }
    }
    
    public static Document createDoc(I_ExtendByRef row)
    	throws IOException 
    {
		Document doc = new Document();
		WorkflowHistoryJavaBean bean = null;
		try {
			bean = WorkflowHelper.populateWorkflowHistoryJavaBean(row);
			WorkflowLuceneSearchResult vals = lastBeanInWfMap.get(bean.getWorkflowId());
			doc = createDoc(bean, vals);
		} catch (Exception e) {
		    AceLog.getAppLog().log(Level.WARNING, "Lucene Creation Issues on bean: " + bean.toString() + " with error: " + e.getMessage());
		    e.printStackTrace();
		}

		return doc;
    }

	public static Document createDoc(WorkflowHistoryJavaBean bean, WorkflowLuceneSearchResult lastBeanInWf) {
		Document doc = new Document();
		
		// Refset Member Id (nid) for Index
		doc.add(new Field("memberId", Integer.toString(bean.getRxMemberId()), Field.Store.YES, Field.Index.NO));

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
		doc.add(new Field("semTag", parsePotentialSemTag(bean.getFSN()), Field.Store.YES, Field.Index.NOT_ANALYZED));
		
		// Keep Time
		doc.add(new NumericField("time").setLongValue(bean.getWorkflowTime()));

		// Last Action, State, Path, Modeler in Workflow
		doc.add(new Field("lastAction", lastBeanInWf.action, Field.Store.YES, Field.Index.NOT_ANALYZED));
		doc.add(new Field("lastState", lastBeanInWf.state, Field.Store.YES, Field.Index.NOT_ANALYZED));
		doc.add(new Field("lastModeler", lastBeanInWf.modeler, Field.Store.YES, Field.Index.NOT_ANALYZED));
		doc.add(new Field("lastTime", Long.toString(lastBeanInWf.time), Field.Store.YES, Field.Index.NO));

		wfHxJavaBeansToWrite.add(bean);
		
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

	public static Set<WorkflowHistoryJavaBean> getMemberNids() {
    	return wfHxJavaBeansToWrite;
    }

	public static void setSourceInputFile(File wfHxInputFile) {
		inputFile = wfHxInputFile;
	}
	
	public WorkflowLuceneSearchResult createLastWfIdLucVals(WorkflowHistoryJavaBean bean) {
		return new WorkflowLuceneSearchResult(bean);
	}

}
