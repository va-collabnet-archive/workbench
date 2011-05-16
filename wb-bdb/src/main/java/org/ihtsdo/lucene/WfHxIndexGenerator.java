package org.ihtsdo.lucene;
 
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;

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
	private static File isFromInputFile;
    private static Set<WorkflowHistoryJavaBean> wfHxJavaBeansToWrite = Collections.synchronizedSet(new HashSet<WorkflowHistoryJavaBean>());
    private static Map<UUID, WorkflowLuceneSearchResult> lastBeanInWfMap = Collections.synchronizedMap(new HashMap<UUID, WorkflowLuceneSearchResult>());
    private static SortedSet<String> semanticTags = new TreeSet<String>();

	public WfHxIndexGenerator(IndexWriter writer) throws IOException, ParseException {
		super(writer);
	
		try {
    		I_GetConceptData parentSemTagConcept = Terms.get().getConcept(ArchitectonicAuxiliary.Concept.SEMTAGS_ROOT.getPrimoridalUid());
        	Set<I_GetConceptData> semTagConcepts = WorkflowHelper.getChildren(parentSemTagConcept);

        	for (I_GetConceptData con : semTagConcepts) {
        		// FSN & Pref Term Same Tag
        		String semTag = con.getDescriptions().iterator().next().getText();
        		
        		semanticTags.add(semTag);
        	}

        	WorkflowHistoryRefsetSearcher searcher = new WorkflowHistoryRefsetSearcher();
	    	WorkflowHistoryRefset refset = new WorkflowHistoryRefset();
	    	int refsetId = WorkflowHistoryRefset.getRefsetId();
	    	int searcherId = WorkflowHistoryRefsetSearcher.getRefsetId();
	    	
	    	// @TODO: Don't forget to remove
	        Set<? extends I_ExtendByRef> membersSearcher = Collections.synchronizedSet(new HashSet<I_ExtendByRef>(Terms.get().getRefsetExtensionMembers(searcherId)));
	        Set<? extends I_ExtendByRef> membersRefset = Collections.synchronizedSet(new HashSet<I_ExtendByRef>(Terms.get().getRefsetExtensionMembers(refsetId)));
	        Set<? extends I_ExtendByRef> members;
			
			if (membersSearcher.size() > 0) { 
				this.refsetId = searcherId;
				members = membersSearcher;
			} else {
				this.refsetId = refsetId;
				members = membersRefset; 
			}
			
			System.out.println("About to process: " + members.size() + " values");
			lastBeanInWfMap.clear();
			if (isFromInputFile != null) {
				String currentWfId = new String();
				String[] curLastRow = null;
				Scanner inputFile = new Scanner(isFromInputFile);
				
				while (inputFile.hasNext()) {
					String[] row = ((String)inputFile.nextLine()).split("\t");
					String wfId = row[WorkflowHelper.workflowIdPosition];
					
					if (curLastRow != null) {
						if (!currentWfId.equals(wfId)) {
							WorkflowLuceneSearchResult vals = new WorkflowLuceneSearchResult(curLastRow);
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
			} else {
				for (I_ExtendByRef row : members) {
			    	UUID wfId = UUID.fromString(refset.getWorkflowIdAsString(((I_ExtendByRefPartStr)row).getStringValue()));
			    	
			    	if (!lastBeanInWfMap.containsKey(wfId)) {
			    		WorkflowHistoryJavaBean latestBean = searcher.getLatestBeanForWorkflowId(row.getComponentNid(), wfId);
			    		WorkflowLuceneSearchResult vals = new WorkflowLuceneSearchResult(latestBean);
			    		
			    		lastBeanInWfMap.put(wfId, vals);
			    	}
		        }
			}
			
		} catch (TerminologyException e) {
		    AceLog.getAppLog().info("Lucene Creation Issues: " + e.getMessage());
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
    	throw new Exception("Do not use I_ProcessConcept mechanism to load WfHx into Lucene");
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

		try {
			WorkflowHistoryJavaBean bean = WorkflowHelper.createWfHxJavaBean(row);
			
			doc = createDoc(bean, lastBeanInWfMap.get(bean.getWorkflowId()));
		} catch (Exception e) {
		    AceLog.getAppLog().info("Lucene Creation Issues: " + e.getMessage());
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
		isFromInputFile = wfHxInputFile;
	}
	
	public WorkflowLuceneSearchResult createLastWfIdLucVals(WorkflowHistoryJavaBean bean) {
		return new WorkflowLuceneSearchResult(bean);
	}

}
