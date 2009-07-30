package org.dwfa.mojo.diff;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.dwfa.ace.api.I_ConceptAttributePart;
import org.dwfa.ace.api.I_ConceptAttributeVersioned;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_DescriptionPart;
import org.dwfa.ace.api.I_DescriptionVersioned;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_Path;
import org.dwfa.ace.api.I_Position;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPart;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartConcept;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartConceptConceptString;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartConceptString;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartString;
import org.dwfa.ace.api.ebr.I_ThinExtByRefVersioned;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.cement.SNOMED;
import org.dwfa.cement.ArchitectonicAuxiliary.Concept;
import org.dwfa.tapi.TerminologyException;

/**
 * Generate a Refset for all concepts in the Concept enum
 * 
 * @goal version-diff
 * 
 * @phase generate-resources
 * 
 * @requiresDependencyResolution compile
 */
public class VersionDiff extends AbstractMojo {

	/**
	 * The uuid of the path.
	 * 
	 * @parameter
	 */
	private String path_uuid;

	/**
	 * The id of v1.
	 * 
	 * @parameter
	 */
	private Integer v1_id;

	/**
	 * The id of v2.
	 * 
	 * @parameter
	 */
	private Integer v2_id;

	/**
	 * Set to true to include added concepts.
	 * 
	 * @parameter default-value=true
	 */
	private boolean added_concepts;

	/**
	 * Set to true to include deleted concepts.
	 * 
	 * @parameter default-value=true
	 */
	private boolean deleted_concepts;

	/**
	 * Set to true to include concepts with changed status.
	 * 
	 * @parameter default-value=true
	 */
	private boolean changed_concept_status;

	/**
	 * Optional list of concept status to filter v1
	 * 
	 * @parameter
	 */
	private List<String> v1_concept_status;

	private List<Integer> v1_concept_status_int;

	/**
	 * Optional list of concept status to filter v2
	 * 
	 * @parameter
	 */
	private List<String> v2_concept_status;

	private List<Integer> v2_concept_status_int;

	/**
	 * Set to true to include concepts with changed defined
	 * 
	 * @parameter default-value=false
	 */
	private boolean changed_defined;

	private int config;

	private int added_concept_change;

	private int deleted_concept_change;

	private int concept_status_change;

	private int defined_change;

	/*
	 * Creates the refset concept
	 */
	protected I_GetConceptData createRefsetConcept() throws Exception {
		I_TermFactory tf = LocalVersionedTerminology.get();
		I_GetConceptData fully_specified_description_type = tf
				.getConcept(ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE
						.getUids());
		I_GetConceptData preferred_description_type = tf
				.getConcept(ArchitectonicAuxiliary.Concept.PREFERRED_DESCRIPTION_TYPE
						.getUids());
		I_ConfigAceFrame config = tf.newAceFrameConfig();
		I_Path path = tf
				.getPath(ArchitectonicAuxiliary.Concept.ARCHITECTONIC_BRANCH
						.getUids());
		config.addEditingPath(path);
		config.setDefaultStatus(tf
				.getConcept(ArchitectonicAuxiliary.Concept.ACTIVE.getUids()));
		UUID uuid = UUID.randomUUID();
		I_GetConceptData newConcept = tf.newConcept(uuid, false, config);
		String name = "Compare of " + v1_id + " " + v2_id + " @ "
				+ System.currentTimeMillis();
		// Install the FSN
		tf.newDescription(UUID.randomUUID(), newConcept, "en", name,
				fully_specified_description_type, config);
		// Install the preferred term
		tf.newDescription(UUID.randomUUID(), newConcept, "en", name,
				preferred_description_type, config);
		tf
				.newRelationship(
						UUID.randomUUID(),
						newConcept,
						tf.getConcept(ArchitectonicAuxiliary.Concept.IS_A_REL
								.getUids()),
						tf.getConcept(RefsetAuxiliary.Concept.REFSET_IDENTITY
								.getUids()),
						tf
								.getConcept(ArchitectonicAuxiliary.Concept.DEFINING_CHARACTERISTIC
										.getUids()),
						tf
								.getConcept(ArchitectonicAuxiliary.Concept.NOT_REFINABLE
										.getUids()),
						tf.getConcept(ArchitectonicAuxiliary.Concept.ACTIVE
								.getUids()), 0, config);
		tf
				.newRelationship(
						UUID.randomUUID(),
						newConcept,
						tf.getConcept(RefsetAuxiliary.Concept.REFSET_TYPE_REL
								.getUids()),
						// tf.getConcept(RefsetAuxiliary.Concept.CONCEPT_EXTENSION
						// .getUids()),
						tf
								.getConcept(RefsetAuxiliary.Concept.CONCEPT_CONCEPT_STRING_EXTENSION
										.getUids()),
						tf
								.getConcept(ArchitectonicAuxiliary.Concept.DEFINING_CHARACTERISTIC
										.getUids()),
						tf
								.getConcept(ArchitectonicAuxiliary.Concept.NOT_REFINABLE
										.getUids()),
						tf.getConcept(ArchitectonicAuxiliary.Concept.ACTIVE
								.getUids()), 0, config);
		tf.commit();
		return newConcept;
	}

	protected I_GetConceptData createChangeTypeConcept(String name)
			throws Exception {
		I_TermFactory tf = LocalVersionedTerminology.get();
		I_GetConceptData fully_specified_description_type = tf
				.getConcept(ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE
						.getUids());
		I_GetConceptData preferred_description_type = tf
				.getConcept(ArchitectonicAuxiliary.Concept.PREFERRED_DESCRIPTION_TYPE
						.getUids());
		I_ConfigAceFrame config = tf.newAceFrameConfig();
		I_Path path = tf
				.getPath(ArchitectonicAuxiliary.Concept.ARCHITECTONIC_BRANCH
						.getUids());
		config.addEditingPath(path);
		config.setDefaultStatus(tf
				.getConcept(ArchitectonicAuxiliary.Concept.ACTIVE.getUids()));
		UUID uuid = UUID.randomUUID();
		I_GetConceptData newConcept = tf.newConcept(uuid, false, config);
		// Install the FSN
		tf.newDescription(UUID.randomUUID(), newConcept, "en", name,
				fully_specified_description_type, config);
		// Install the preferred term
		tf.newDescription(UUID.randomUUID(), newConcept, "en", name,
				preferred_description_type, config);
		tf
				.newRelationship(
						UUID.randomUUID(),
						newConcept,
						tf.getConcept(ArchitectonicAuxiliary.Concept.IS_A_REL
								.getUids()),
						tf
								.getConcept(ArchitectonicAuxiliary.Concept.ARCHITECTONIC_ROOT_CONCEPT
										.getUids()),
						tf
								.getConcept(ArchitectonicAuxiliary.Concept.DEFINING_CHARACTERISTIC
										.getUids()),
						tf
								.getConcept(ArchitectonicAuxiliary.Concept.NOT_REFINABLE
										.getUids()),
						tf.getConcept(ArchitectonicAuxiliary.Concept.ACTIVE
								.getUids()), 0, config);
		tf.commit();
		return newConcept;
	}

	/*
	 * Adds a concept to a refset
	 */
	private void addToRefset(I_TermFactory tf, int refsetId, int conceptId,
			int changeId, String comment) throws Exception {
		I_GetConceptData include_individual = tf
				.getConcept(RefsetAuxiliary.Concept.INCLUDE_INDIVIDUAL
						.getUids());
		// System.out.println("Include: " +
		// include_individual.getUids().get(0));
		int typeId = include_individual.getConceptId();
		I_GetConceptData active_status = tf
				.getConcept(ArchitectonicAuxiliary.Concept.ACTIVE.getUids());
		int statusId = active_status.getConceptId();
		int memberId = tf.uuidToNativeWithGeneration(UUID.randomUUID(),
				ArchitectonicAuxiliary.Concept.UNSPECIFIED_UUID.localize()
						.getNid(), tf.getPaths(), Integer.MAX_VALUE);
		I_ThinExtByRefVersioned newExtension = tf.newExtension(refsetId,
				memberId, conceptId, typeId);
		I_ThinExtByRefPartConceptConceptString ext = tf
				.newConceptConceptStringExtensionPart();
		ext.setC1id(conceptId);
		ext.setC2id(changeId);
		ext.setStr(comment);
		I_GetConceptData path = tf
				.getConcept(ArchitectonicAuxiliary.Concept.ARCHITECTONIC_BRANCH
						.getUids());
		// System.out.println("Path: " + path.getUids().get(0));
		ext.setPathId(path.getConceptId());
		ext.setStatusId(statusId);
		ext.setVersion(Integer.MAX_VALUE);
		newExtension.addVersion(ext);
		tf.addUncommitted(newExtension);
	}

	/*
	 *
	 */
	private void diff() throws Exception {
		I_TermFactory tf = LocalVersionedTerminology.get();
		I_GetConceptData refset = createRefsetConcept();
		getLog().info("Refset: " + refset.getInitialText());
		getLog().info("Refset: " + refset.getUids().get(0));
		addToRefset(tf, refset.getConceptId(), refset.getConceptId(),
				this.config, "v1_id = " + this.v1_id);
		addToRefset(tf, refset.getConceptId(), refset.getConceptId(),
				this.config, "v2_id = " + this.v2_id);
		addToRefset(tf, refset.getConceptId(), refset.getConceptId(),
				this.config, "added_concepts = " + this.added_concepts);
		addToRefset(tf, refset.getConceptId(), refset.getConceptId(),
				this.config, "deleted_concepts = " + this.deleted_concepts);
		addToRefset(tf, refset.getConceptId(), refset.getConceptId(),
				this.config, "changed_concept_status = "
						+ this.changed_concept_status);
		this.v1_concept_status_int = new ArrayList<Integer>();
		for (String str : this.v1_concept_status) {
			I_GetConceptData con = tf.getConcept(Arrays.asList(UUID
					.fromString(str)));
			if (con == null) {
				getLog().info("Can't find " + str);
				continue;
			}
			this.v1_concept_status_int.add(con.getConceptId());
			addToRefset(tf, refset.getConceptId(), refset.getConceptId(),
					this.config, "v1_concept_status = " + str + " "
							+ con.getInitialText());
		}
		this.v2_concept_status_int = new ArrayList<Integer>();
		for (String str : this.v2_concept_status) {
			I_GetConceptData con = tf.getConcept(Arrays.asList(UUID
					.fromString(str)));
			if (con == null) {
				getLog().info("Can't find " + str);
				continue;
			}
			this.v2_concept_status_int.add(con.getConceptId());
			addToRefset(tf, refset.getConceptId(), refset.getConceptId(),
					this.config, "v2_concept_status = " + str + " "
							+ con.getInitialText());
		}
		for (Concept c : Arrays.asList(ArchitectonicAuxiliary.Concept.CURRENT,
				ArchitectonicAuxiliary.Concept.LIMITED,
				ArchitectonicAuxiliary.Concept.PENDING_MOVE,
				ArchitectonicAuxiliary.Concept.RETIRED,
				ArchitectonicAuxiliary.Concept.DUPLICATE,
				ArchitectonicAuxiliary.Concept.OUTDATED,
				ArchitectonicAuxiliary.Concept.AMBIGUOUS,
				ArchitectonicAuxiliary.Concept.ERRONEOUS,
				ArchitectonicAuxiliary.Concept.MOVED_ELSEWHERE)) {
			getLog()
					.info(
							"Status: "
									+ tf.getConcept(c.getUids()).getUids().get(
											0)
									+ " "
									+ tf.getConcept(c.getUids())
											.getInitialText());
		}
		addToRefset(tf, refset.getConceptId(), refset.getConceptId(),
				this.config, "changed_defined = " + this.changed_defined);
		I_Path path = tf.getPath(Arrays.asList(UUID.fromString(path_uuid)));
		I_Position pos1 = tf.newPosition(path, v1_id);
		I_Position pos2 = tf.newPosition(path, v2_id);
		int only1 = 0;
		int only2 = 0;
		int changed = 0;
		int concepts = 0;
		for (Iterator<I_GetConceptData> i = tf.getConceptIterator(); i
				.hasNext();) {
			concepts++;
			if (concepts % 10000 == 0)
				getLog().info("concepts " + concepts);
			I_GetConceptData c = i.next();
			I_ConceptAttributePart a1 = null;
			I_ConceptAttributePart a2 = null;
			for (I_ConceptAttributePart a : c.getConceptAttributes()
					.getVersions()) {
				// Must be on the path
				if (a.getPathId() != path.getConceptId())
					continue;
				// Find the greatest version <= the one of interest
				if (a.getVersion() <= v1_id
						&& (a1 == null || a1.getVersion() < a.getVersion()))
					a1 = a;
				if (a.getVersion() <= v2_id
						&& (a2 == null || a2.getVersion() < a.getVersion()))
					a2 = a;
			}
			if (this.added_concepts && a1 == null && a2 != null) {
				addToRefset(tf, refset.getConceptId(), c.getConceptId(),
						this.added_concept_change, "Added Concept");
				// getLog().info("Only in 2 " + c.getInitialText());
				only2++;
			}
			if (this.deleted_concepts && a1 != null && a2 == null) {
				addToRefset(tf, refset.getConceptId(), c.getConceptId(),
						this.deleted_concept_change, "Deleted Concept");
				// getLog().info("Only in 1 " + c.getInitialText());
				only1++;
			}
			if (a1 != null && a2 != null && a1.getVersion() != a2.getVersion()) {
				// Something changed
				changed++;
				if (this.changed_concept_status
						&& a1.getStatusId() != a2.getStatusId()
						&& (this.v1_concept_status.size() == 0 || this.v1_concept_status_int
								.contains(a1.getStatusId()))
						&& (this.v2_concept_status.size() == 0 || this.v2_concept_status_int
								.contains(a2.getStatusId()))) {
					addToRefset(tf, refset.getConceptId(), c.getConceptId(),
							this.concept_status_change, tf.getConcept(
									a1.getStatusId()).getInitialText()
									+ " -> "
									+ tf.getConcept(a2.getStatusId())
											.getInitialText());
				}
				if (this.changed_defined && a1.isDefined() != a2.isDefined()) {
					addToRefset(tf, refset.getConceptId(), c.getConceptId(),
							this.defined_change, a1.isDefined() + " -> "
									+ a2.isDefined());
				}
			}
		}
		getLog().info("concepts " + concepts);
		getLog().info("only1 " + only1);
		getLog().info("only2 " + only2);
		getLog().info("changed " + changed);
		tf.commit();
		this.listDiff(refset);
	}

	private void listDiff(I_GetConceptData refset) throws Exception {
		getLog().info("diff list");
		PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(
				"diff.txt")));
		I_TermFactory tf = LocalVersionedTerminology.get();
		int diffs = 0;
		for (I_ThinExtByRefVersioned mem : tf.getRefsetExtensionMembers(refset
				.getConceptId())) {
			diffs++;
			if (diffs % 1000 == 0)
				getLog().info("diffs " + diffs);
			I_GetConceptData mem_con = tf.getConcept(mem.getComponentId());
			I_ThinExtByRefPart p = mem.getVersions().get(0);
			if (p instanceof I_ThinExtByRefPartConceptConceptString) {
				I_ThinExtByRefPartConceptConceptString pccs = (I_ThinExtByRefPartConceptConceptString) p;
				out.println(tf.getConcept(pccs.getC2id()).getInitialText()
						+ "\t" + pccs.getStr() + "\t"
						+ tf.getConcept(pccs.getC1id()).getInitialText());
			} else {
				getLog().info("Wrong type: " + mem_con.getInitialText());
			}
		}
		getLog().info("diffs " + diffs);
		out.close();
	}

	// Status: 2faa9261-8fb2-11db-b606-0800200c9a66 current (active status type)

	// Status: 4bc081d8-9f64-3a89-a668-d11ca031979b limited

	// Status: 11c24184-4d8a-3cd3-bc30-bb0aa4c76e93 pending move (active status
	// type)

	// Status: e1956e7b-08b4-3ad0-ab02-b411869f1c09 retired

	// Status: cbe19851-49f7-32f7-bb27-2d24be0e77e8 duplicate

	// Status: ad6b6532-0cb7-35d0-be04-47a9e4634ed8 outdated (inactive status
	// type)

	// Status: 3b397a0a-b510-391b-bd2e-5dd168c092ba ambiguous (inactive status
	// type)

	// Status: a09cc39f-2c01-3563-84cc-bad7d7fb597f erroneous (inactive status
	// type)

	// Status: 76367831-522f-3250-83a4-8609ab298436 moved elsewhere (inactive
	// status type)

	// SNOMED Clinical Terms version: 20080731 [R] (July 2008 Release)

	// -612920153

	// SNOMED Clinical Terms version: 20090131 [R] (January 2009 Release)

	// -597018953

	private void listVersions() throws Exception {
		I_TermFactory tf = LocalVersionedTerminology.get();
		I_GetConceptData c = tf.getConcept(SNOMED.Concept.ROOT.getUids());
		System.out.println(c.getInitialText());
		I_ConceptAttributeVersioned cv = c.getConceptAttributes();
		for (I_ConceptAttributePart cvp : cv.getVersions()) {
			System.out.println("Attr: " + cvp.getVersion());
		}
		I_GetConceptData syn_type = tf
				.getConcept(ArchitectonicAuxiliary.Concept.SYNONYM_DESCRIPTION_TYPE
						.getUids());
		for (I_DescriptionVersioned cd : c.getDescriptions()) {
			for (I_DescriptionPart cvp : cd.getVersions()) {
				if (cvp.getTypeId() == syn_type.getConceptId()
						&& cvp.getText().contains("version")) {
					System.out.println("Version: " + cvp.getText());
					System.out.println("         " + cvp.getVersion());
				}
			}
		}
	}

	private void listPaths() throws Exception {
		I_TermFactory tf = LocalVersionedTerminology.get();
		for (I_Path path : tf.getPaths()) {
			I_GetConceptData path_con = tf.getConcept(path.getConceptId());
			System.out.println("Path: " + path_con.getInitialText());
			System.out.println("      " + path_con.getUids().get(0));
			for (I_Position position : path.getOrigins()) {
				System.out.println("Version: " + position.getVersion());
				I_GetConceptData position_con = tf.getConcept(position
						.getPath().getConceptId());
				System.out.println("         " + position_con.getInitialText());
				System.out.println("         " + position_con.getUids().get(0));
			}
		}
	}

	/*
	 * 
	 * Example use:
	 * 
	 * <br>&lt;execution&gt;
	 * 
	 * <br>&lt;id&gt;version-diff&lt;/id&gt;
	 * 
	 * <br>&lt;phase&gt;generate-sources&lt;/phase&gt;
	 * 
	 * <br>&lt;goals&gt;
	 * 
	 * <br>&lt;goal&gt;version-diff&lt;/goal&gt;
	 * 
	 * <br>&lt;/goals&gt;
	 * 
	 * <br>&lt;configuration&gt;
	 * 
	 * <br>&lt;v1&gt;v1-id&lt;v1&gt;
	 * 
	 * <br>&lt;v2&gt;v2-id&lt;v2&gt;
	 * 
	 * <br>&lt;/configuration&gt;
	 * 
	 * <br>&lt;/execution&gt;
	 */
	public void execute() throws MojoExecutionException, MojoFailureException {
		try {
			I_TermFactory tf = LocalVersionedTerminology.get();

			this.listVersions();

			this.listPaths();

			this.config = this.createChangeTypeConcept("Configuration")
					.getConceptId();
			this.added_concept_change = this.createChangeTypeConcept(
					"Added Concept").getConceptId();
			this.deleted_concept_change = this.createChangeTypeConcept(
					"Deleted Concept").getConceptId();
			this.concept_status_change = this.createChangeTypeConcept(
					"Changed Concept Status").getConceptId();
			this.defined_change = this.createChangeTypeConcept(
					"Changed Defined").getConceptId();

			this.diff();
		} catch (Exception e) {
			// e.printStackTrace();
			throw new MojoFailureException(e.getLocalizedMessage(), e);
		}
	}
}
