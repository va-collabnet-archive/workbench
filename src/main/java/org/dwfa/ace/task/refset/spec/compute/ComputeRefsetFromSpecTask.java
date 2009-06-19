package org.dwfa.ace.task.refset.spec.compute;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import javax.swing.JList;
import javax.swing.tree.DefaultMutableTreeNode;

import org.apache.lucene.queryParser.ParseException;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_ModelTerminologyList;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPart;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartConceptConcept;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartConceptConceptConcept;
import org.dwfa.ace.api.ebr.I_ThinExtByRefTuple;
import org.dwfa.ace.api.ebr.I_ThinExtByRefVersioned;
import org.dwfa.ace.task.ProcessAttachmentKeys;
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
	I_TermFactory termFactory;

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

			configFrame = (I_ConfigAceFrame) worker
				.readAttachement(WorkerAttachmentKeys.ACE_FRAME_CONFIG
					.name());

			I_GetConceptData refsetSpec = configFrame.getRefsetSpecInSpecEditor();
			if (refsetSpec == null) {
				throw new TaskFailedException("Refset spec is null.");
			}

			termFactory = LocalVersionedTerminology.get();
			JList conceptList = configFrame.getBatchConceptList();
			I_ModelTerminologyList model = (I_ModelTerminologyList) conceptList.getModel();
			
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
			boolean useAndQualifier = false;
			RefsetSpecQuery query = new RefsetSpecQuery(useAndQualifier);
			query = processNode(root, query);

			Iterator<I_GetConceptData> conceptIterator = termFactory.getConceptIterator();
			int conceptCount = 0;
			HashSet<I_GetConceptData> results = new HashSet<I_GetConceptData>();

			// 1. iterate over each concept and run query against it (this will also execute any sub-queries)
			// 2. any concepts that meet the query criteria are added to results list
			while (conceptIterator.hasNext()) {
				I_GetConceptData currentConcept = conceptIterator.next();
				if (query.execute(currentConcept)) {
					results.add(currentConcept);
				}
				conceptCount++;
			}

			getLogger().info("Number of member refset members: " + results.size());
			getLogger().info("Total number of concepts processed: " + conceptCount);
			
			// add results to batch list for review 
			for (I_GetConceptData result : results) {
				model.addElement(result);
			}
			
			// set properties for use later in BP
			// the refset we are adding to
	        process.setProperty(ProcessAttachmentKeys.WORKING_REFSET.getAttachmentKey(), refsetSpec);

		    // the value to be given to the new concept extension 
	        process.setProperty(ProcessAttachmentKeys.I_GET_CONCEPT_DATA.getAttachmentKey(), refsetSpec); 
		    

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
	private RefsetSpecQuery processNode(DefaultMutableTreeNode node, RefsetSpecQuery query) 
		throws IOException, TerminologyException, ParseException {
		
		if (query == null) {
			throw new TerminologyException("Invalid refset spec : null query item used.");
		}
		
		int childCount = node.getChildCount();
		
		for (int i = 0; i < childCount; i++) {
			
			DefaultMutableTreeNode childNode = (DefaultMutableTreeNode) node.getChildAt(i);
			
			// determine type of current child
			I_ThinExtByRefVersioned currExt = (I_ThinExtByRefVersioned) childNode.getUserObject();

			List<I_ThinExtByRefTuple> extensions = 
				currExt.getTuples(configFrame.getAllowedStatus(), configFrame.getViewPositionSet(), true);
			I_ThinExtByRefPart thinPart = extensions.get(0).getPart();	
			
			if (thinPart instanceof I_ThinExtByRefPartConceptConceptConcept) {
				
				// structural query e.g. true : is-concept : height
				I_ThinExtByRefPartConceptConceptConcept part = 
					(I_ThinExtByRefPartConceptConceptConcept) thinPart;

				getLogger().info(">>>>>>>>>>>>>>>");
				getLogger().info(termFactory.getConcept(part.getC1id()).getInitialText());
				getLogger().info(termFactory.getConcept(part.getC2id()).getInitialText());
				getLogger().info(termFactory.getConcept(part.getC3id()).getInitialText());

				I_GetConceptData c1 = termFactory.getConcept(part.getC1id());
				I_GetConceptData c2 = termFactory.getConcept(part.getC2id());
				I_GetConceptData c3 = termFactory.getConcept(part.getC3id());

				boolean negate = getNegation(c1);

				query.addStatement(negate, c2, c3); 

			} else if (thinPart instanceof I_ThinExtByRefPartConceptConcept) {
				
				// logical OR or AND e.g. true : and
				I_ThinExtByRefPartConceptConcept part = 
					(I_ThinExtByRefPartConceptConcept) thinPart;

				getLogger().info(">>>>>>>>>>>>>>>");
				getLogger().info(termFactory.getConcept(part.getC1id()).getInitialText());
				getLogger().info(termFactory.getConcept(part.getC2id()).getInitialText());

				I_GetConceptData c1 = termFactory.getConcept(part.getC1id());
				I_GetConceptData c2 = termFactory.getConcept(part.getC2id());

				boolean negate = getNegation(c1);
				
				// add subquery
				RefsetSpecQuery subquery = query.addSubquery(useAndQualifier(c2));
				
				// process each grandchild
				if (!childNode.isLeaf()) {
					processNode(childNode, subquery);
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
	 * Calculates if the "AND" qualifier is to be used. In This case, the concept passed in 
	 * will be "refset AND grouping". Otherwise, if "refset OR grouping" is passed in, 
	 * the calling statement will use "OR". Other inputs are invalid.
	 * @param c2
	 * @return
	 * @throws TerminologyException
	 * @throws IOException
	 */
	private boolean useAndQualifier(I_GetConceptData c2) throws TerminologyException, IOException {
		if (c2.equals(termFactory.getConcept(RefsetAuxiliary.Concept.REFSET_AND_GROUPING.getUids()))) {
			return true;
		} else if (c2.equals(termFactory.getConcept(RefsetAuxiliary.Concept.REFSET_OR_GROUPING.getUids()))) {
			return false;
		}  else {
			throw new TerminologyException("Neither AND or OR grouping specifieid.");
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
