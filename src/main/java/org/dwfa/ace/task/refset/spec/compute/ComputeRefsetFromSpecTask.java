package org.dwfa.ace.task.refset.spec.compute;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;

import org.apache.lucene.queryParser.ParseException;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_ShowActivity;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPart;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartConceptConcept;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartConceptConceptConcept;
import org.dwfa.ace.api.ebr.I_ThinExtByRefTuple;
import org.dwfa.ace.api.ebr.I_ThinExtByRefVersioned;
import org.dwfa.ace.refset.MemberRefsetHelper;
import org.dwfa.ace.task.WorkerAttachmentKeys;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;

/**
 * Computes the members of a refset given a refset spec. This refset spec is the one 
 * currently displayed in the refset spec editing panel. The refset spec's 
 * "specifies refset" relationship indicates which member refset will be created.
 * @author Chrissy Hill
 *
 */
@BeanList(specs = { @Spec(directory = "tasks/refset/spec", type = BeanType.TASK_BEAN) })
public class ComputeRefsetFromSpecTask extends AbstractTask {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static final int dataVersion = 1;

	private I_ConfigAceFrame configFrame;
	private I_TermFactory termFactory;
	
	private final int REL = 1;
	private final int DESC = 2;
	private final int CONCEPT = 3;
	
	private boolean useMonitor = false;

	private void writeObject(ObjectOutputStream out) throws IOException {
		out.writeInt(dataVersion);
	}

	private void readObject(ObjectInputStream in) throws IOException,
		ClassNotFoundException {
		int objDataVersion = in.readInt();
		if (objDataVersion == dataVersion) {
			// Nothing to do
		} else {
			throw new IOException(
					"Can't handle dataversion: " + objDataVersion);
		}
	}

	public void complete(I_EncodeBusinessProcess process, I_Work worker)
		throws TaskFailedException {
		// Nothing to do
	}

	public Condition evaluate(I_EncodeBusinessProcess process, I_Work worker)
		throws TaskFailedException {

		try {
			long startTime = new Date().getTime();
			
			configFrame = (I_ConfigAceFrame) worker
				.readAttachement(WorkerAttachmentKeys.ACE_FRAME_CONFIG
					.name());

			I_GetConceptData refsetSpec = configFrame.getRefsetSpecInSpecEditor();
			if (refsetSpec == null) {
				throw new TaskFailedException("Refset spec is null.");
			}
			
			I_GetConceptData refset = configFrame.getRefsetInSpecEditor();
			if (refset == null) {
				throw new TaskFailedException("Refset is null.");
			}

			termFactory = LocalVersionedTerminology.get();
			I_GetConceptData normalMemberConcept = termFactory.getConcept(
					RefsetAuxiliary.Concept.NORMAL_MEMBER.getUids());
			int conceptsToProcess = termFactory.getConceptCount();
			I_ShowActivity computeRefsetActivityPanel = termFactory.newActivityPanel(
					true);
			computeRefsetActivityPanel.setMaximum(conceptsToProcess);
			computeRefsetActivityPanel.setStringPainted(true);
			computeRefsetActivityPanel.setValue(0);
			computeRefsetActivityPanel.setIndeterminate(false);
			computeRefsetActivityPanel.setProgressInfoUpper("Computing refset " 
					+ ": " + refset.getInitialText());
			computeRefsetActivityPanel.setProgressInfoLower(
					"<html>" 
					+ "1) Creating refset spec query.   " 
					+ "<font color='green'>Executing.<br><font color='black'>"
					
					+ "2) Executing refset spec query over database.   "
					+ "<font color='green'> <br><font color='black'>");
				
			// create tree object that corresponds to the database's refset spec
			List<I_ThinExtByRefVersioned> extensions = LocalVersionedTerminology.get().getAllExtensionsForComponent(
					refsetSpec.getConceptId(), true);
			HashMap<Integer, DefaultMutableTreeNode> extensionMap = new HashMap<Integer, 
					DefaultMutableTreeNode>();
			HashSet<Integer> fetchedComponents = new HashSet<Integer>();
			fetchedComponents.add(refsetSpec.getConceptId());
			addExtensionsToMap(extensions, extensionMap, fetchedComponents);		
			getLogger().info("Extension map: " + extensionMap);

			DefaultMutableTreeNode root = new DefaultMutableTreeNode(refsetSpec);
			for (DefaultMutableTreeNode extNode: extensionMap.values()) {
				I_ThinExtByRefVersioned ext = (I_ThinExtByRefVersioned) extNode.getUserObject();
				if (ext.getComponentId() == refsetSpec.getConceptId()) {
					root.add(extNode);
				} else {
					extensionMap.get(ext.getComponentId()).add(extNode);
				}
			}

			// create refset spec query
			I_GetConceptData orConcept = termFactory.getConcept(
					RefsetAuxiliary.Concept.REFSET_OR_GROUPING.getUids());
			RefsetSpecQuery query = new RefsetSpecQuery(orConcept);
			query = processNode(root, query, CONCEPT);
			
			computeRefsetActivityPanel.setProgressInfoLower(
					"<html>" 
					+ "1) Creating refset spec query.   " 
					+ "<font color='red'>COMPLETE.<br><font color='black'>"
					
					+ "2) Executing refset spec query over database.   "
					+ "<font color='green'>Executing.<br><font color='black'>"
					+ "     New members added : 0 <br>"
					+ "     Non-members cleaned : 0");

			Iterator<I_GetConceptData> conceptIterator = termFactory.getConceptIterator();
			int conceptsProcessed = 0;
			HashSet<I_GetConceptData> refsetMembers = new HashSet<I_GetConceptData>();
			HashSet<I_GetConceptData> nonRefsetMembers = new HashSet<I_GetConceptData>();
			int refsetMembersCount = 0;
			int nonRefsetMembersCount = 0;
 
			MemberRefsetHelper memberRefsetHelper = new MemberRefsetHelper(refset.getConceptId(), normalMemberConcept.getConceptId());

			// 1. iterate over each concept and run query against it (this will also execute any sub-queries)
			// 2. any concepts that meet the query criteria are added to results list
			getLogger().info("Start execution of refset spec : " + refsetSpec.getInitialText());
			while (conceptIterator.hasNext()) {
				
				I_GetConceptData currentConcept = conceptIterator.next();
				conceptsProcessed++;
				
				if (query.execute(currentConcept)) {
					refsetMembers.add(currentConcept);
				} else {
					nonRefsetMembers.add(currentConcept);
				}
				
				if (refsetMembers.size() > 50000) {
					// add them now
					memberRefsetHelper.addAllToRefset(refsetMembers, 
							"Adding new members to member refset", useMonitor);
					
					refsetMembersCount = refsetMembersCount 
							+ refsetMembers.size();
					refsetMembers = new HashSet<I_GetConceptData>();
				}	
				
				if (nonRefsetMembers.size() > 50000) {
					memberRefsetHelper.removeAllFromRefset(nonRefsetMembers, 
							"Cleaning up old members from member refset",
							useMonitor);
					nonRefsetMembersCount = nonRefsetMembersCount + nonRefsetMembers.size();
					nonRefsetMembers = new HashSet<I_GetConceptData>();
				}
				
				if (conceptsProcessed % 500 == 0) {
					computeRefsetActivityPanel.setProgressInfoLower(
									"<html>" 
									+ "1) Creating refset spec query.   " 
									+ "<font color='red'>COMPLETE.<br><font color='black'>"
									
									+ "2) Executing refset spec query over database.   "
									+ "<font color='green'>Executing.<br><font color='black'>"
									+ "     New members added : " + (refsetMembersCount + refsetMembers.size()) + "<br>"
									+ "     Non-members cleaned : " + (nonRefsetMembersCount + nonRefsetMembers.size()));
					computeRefsetActivityPanel.setValue(refsetMembersCount + nonRefsetMembersCount);
				}
			}
			
			computeRefsetActivityPanel.setProgressInfoLower(
					"<html>" 
					+ "1) Creating refset spec query.   " 
					+ "<font color='red'>COMPLETE.<br><font color='black'>"
					
					+ "2) Executing refset spec query over database.   "
					+ "<font color='red'>COMPLETE.<br><font color='black'>"
					+ "     New members added : " + (refsetMembersCount + refsetMembers.size()) + "<br>"
					+ "     Non-members cleaned : " + (nonRefsetMembersCount + nonRefsetMembers.size()) + "<br>"
					+ "     Finalising refset, please wait..."); 
			
			// add any remaining members
			// add concepts from list view to the refset
			// this will skip any that already exist as current members of the refset
			memberRefsetHelper.addAllToRefset(refsetMembers, 
					"Adding new members to member refset",
					useMonitor);
			
			// remaining parent refsets
			memberRefsetHelper.removeAllFromRefset(nonRefsetMembers, 
					"Cleaning up old members from member refset", 
					useMonitor);
			
			getLogger().info("Number of member refset members: " + refsetMembersCount);
			getLogger().info("Total number of concepts processed: " + conceptsProcessed);
			getLogger().info("End execution of refset spec : " + refsetSpec.getInitialText());

			long endTime = new Date().getTime();
			long minutes = (endTime - startTime) / 60000;
			long seconds = ((endTime - startTime) % 60000)/1000;
			computeRefsetActivityPanel.setProgressInfoLower(
					"<html>" 
					+ "1) Creating refset spec query.   " 
					+ "<font color='red'>COMPLETE.<br><font color='black'>"
					
					+ "2) Executing refset spec query over database.   "
					+ "<font color='red'>COMPLETE.<br><font color='black'>"
					+ "     New members added : " + (refsetMembersCount + refsetMembers.size()) + "<br>"
					+ "     Non-members cleaned : " + (nonRefsetMembersCount + nonRefsetMembers.size()) + "<br>"
					+ "     Total execution time: " + minutes + " minutes, " + seconds + " seconds.");
		//	computeRefsetActivityPanel.setValue(numberOfNHSConcepts);
			computeRefsetActivityPanel.complete();
			
			return Condition.CONTINUE;
		} catch (Exception ex) {
			throw new TaskFailedException(ex);
		}
	}

	/**
	 * Processes a node in our refset spec tree structure. For each child of the node, 
	 * we recursively determine the refset type (corresponds to a sub-query or statement).
	 * This includes checking any grandchildren, great-grandchildren etc.
	 * @param node The node to which the processing begins.
	 * @param query The query to add the processed node's information to.
	 * @return A query containing the updated node's information.
	 * @throws IOException
	 * @throws TerminologyException
	 * @throws ParseException
	 */
	private RefsetSpecQuery processNode(DefaultMutableTreeNode node, 
			RefsetSpecQuery query, int type) 
		throws IOException, TerminologyException, ParseException {
		
		if (query == null) {
			throw new TerminologyException("Invalid refset spec : null query item used.");
		}
		
		int childCount = node.getChildCount();
		
		for (int i = 0; i < childCount; i++) {
			
			DefaultMutableTreeNode childNode = (DefaultMutableTreeNode) node.getChildAt(i);
			
			// determine type of current child
			I_ThinExtByRefVersioned currExt = (I_ThinExtByRefVersioned) childNode.getUserObject();

			boolean addUncommitted = true;
			boolean returnConflictResolvedLatestState = true;
			List<I_ThinExtByRefTuple> extensions = currExt.getTuples(
						configFrame.getAllowedStatus(), 
						configFrame.getViewPositionSet(), addUncommitted, 
						returnConflictResolvedLatestState);
			I_ThinExtByRefPart thinPart = extensions.get(0).getPart();	
			
			if (thinPart instanceof I_ThinExtByRefPartConceptConceptConcept) {
				
				// structural query e.g. true : is-concept : height
				I_ThinExtByRefPartConceptConceptConcept part = 
					(I_ThinExtByRefPartConceptConceptConcept) thinPart;

				getLogger().info(">>>>>>>>>>>>>>>");
				getLogger().info(termFactory.getConcept(part.getC1id()).getInitialText());
				getLogger().info(termFactory.getConcept(part.getC2id()).getInitialText());
				getLogger().info(termFactory.getConcept(part.getC3id()).getInitialText());

				I_GetConceptData truthToken = termFactory.getConcept(part.getC1id());
				I_GetConceptData groupingToken = termFactory.getConcept(part.getC2id());
				I_GetConceptData constraint = termFactory.getConcept(part.getC3id());

				switch (type) {
					case (CONCEPT) :
						query.addConceptStatement(getNegation(truthToken), groupingToken, 
								constraint);
						break;
					case (DESC) :
						query.addDescStatement(getNegation(truthToken), groupingToken, 
								constraint);
						break;
					case (REL) :
						query.addRelStatement(getNegation(truthToken), groupingToken, 
								constraint);
						break;
					default:
						throw new TerminologyException("Unknown type: " + groupingToken.getInitialText());
				}
			} else if (thinPart instanceof I_ThinExtByRefPartConceptConcept) {
				
				// logical OR, AND, CONCEPT-CONTAINS-REL, or 
				// CONCEPT-CONTAINS-DESC.
				I_ThinExtByRefPartConceptConcept part = 
					(I_ThinExtByRefPartConceptConcept) thinPart;

				getLogger().info(">>>>>>>>>>>>>>>");
				getLogger().info(termFactory.getConcept(part.getC1id()).getInitialText());
				getLogger().info(termFactory.getConcept(part.getC2id()).getInitialText());

				I_GetConceptData truthToken = termFactory.getConcept(
						part.getC1id());
				I_GetConceptData groupingToken = termFactory.getConcept(
						part.getC2id());
				
				boolean negate = getNegation(truthToken);
				
				// add subquery
				RefsetSpecQuery subquery = query.addSubquery(groupingToken);
				
				int subtype = getType(groupingToken);
				if (subtype == -1) {
					subtype = type;
				}
				
				// process each grandchild
				if (!childNode.isLeaf()) {
					processNode(childNode, subquery, subtype);
				}
				if (negate) {
					subquery.negateQuery();
				}
			} else {
				getLogger().info("Unknown extension type");
			} 
		}
		return query;
	} 

	
	private int getType(I_GetConceptData groupingToken) throws TerminologyException, IOException {
		if (termFactory.getConcept(
				RefsetAuxiliary.Concept.CONCEPT_CONTAINS_REL_GROUPING.getUids()).equals(groupingToken)) {
			return REL;
		} else if (termFactory.getConcept(
				RefsetAuxiliary.Concept.CONCEPT_CONTAINS_DESC_GROUPING.getUids()).equals(groupingToken)) {
			return DESC;
		} else {
			return -1;
		}
	}


	/**
	 * Determines whether or not the associated statement or query needs to be negated.
	 * If negation is required, the concept passed in will be "boolean circle icon false".
	 * @param c1 The concept 
	 * @return
	 * @throws TerminologyException
	 * @throws IOException
	 */
	private boolean getNegation(I_GetConceptData c1) throws TerminologyException, IOException {
		if (c1.equals(termFactory.getConcept(RefsetAuxiliary.Concept.BOOLEAN_CIRCLE_ICONS_TRUE.getUids()))) {
			return false;
		} else if (c1.equals(termFactory.getConcept(RefsetAuxiliary.Concept.BOOLEAN_CIRCLE_ICONS_FALSE.getUids()))) {
			return true;
		} else {
			throw new TerminologyException ("Unable to recognise truth type: " 
					+ c1.getInitialText());
		}
	}

	/**
	 * Recursively adds extensions to a map - this is used to create a tree structure 
	 * representing the refset spec, prior to being converted to a query object.
	 * @param extensions 
	 * @param extensionMap
	 * @param fetchedComponents
	 * @throws IOException
	 */
	private void addExtensionsToMap(
			List<I_ThinExtByRefVersioned> extensions,
			HashMap<Integer, 
			DefaultMutableTreeNode> extensionMap, 
			HashSet<Integer> fetchedComponents) throws IOException {
		for (I_ThinExtByRefVersioned ext: extensions) {
			extensionMap.put(ext.getMemberId(), new DefaultMutableTreeNode(ext));
			if (fetchedComponents.contains(ext.getMemberId()) == false) {
				fetchedComponents.add(ext.getMemberId());
				addExtensionsToMap(LocalVersionedTerminology.get().getAllExtensionsForComponent(ext.getMemberId(), true), 
						extensionMap, fetchedComponents);		
			}
		}
	}

	public int[] getDataContainerIds() {
		return new int[] {};
	}

	public Collection<Condition> getConditions() {
		return AbstractTask.CONTINUE_CONDITION;
	}
}
