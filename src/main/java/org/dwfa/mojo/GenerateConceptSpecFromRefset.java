package org.dwfa.mojo;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;
import java.util.UUID;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_Path;
import org.dwfa.ace.api.I_ProcessConcepts;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.api.ebr.I_ThinExtByRefVersioned;
import org.dwfa.ace.refset.RefsetUtilities;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.cement.RefsetAuxiliary.Concept;
import org.dwfa.tapi.TerminologyException;

/**
 * Generate ConceptSpec Java file from a given Refset
 * 
 * @goal generate-concept-spec
 * 
 * @phase generate-resources
 * 
 * @requiresDependencyResolution compile
 */
public class GenerateConceptSpecFromRefset extends AbstractMojo {

	/**
	 * The UUID of the RefSet.
	 * 
	 * @parameter
	 */
	private String refSetUuid;

	/**
	 * The name of the RefSet.
	 * 
	 * @parameter
	 */
	private String refSetName;

	/**
	 * The Java package name.
	 * 
	 * @parameter
	 */
	private String packageName;

	/**
	 * The Java class name.
	 * 
	 * @parameter expression="ConceptSpecFromRefset"
	 */
	private String className;

	/**
	 * The Java file output location.
	 * 
	 * @parameter expression="${project.build.sourceDirectory}"
	 */
	private File outputDirectory;

	private List<I_GetConceptData> getListTrivial() throws Exception {
		I_TermFactory termFactory = LocalVersionedTerminology.get();
		for (Concept c : Concept.values()) {
			System.out.println(c.name());
		}
		return Arrays
				.asList(
						termFactory
								.getConcept(ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE
										.getUids()),
						termFactory
								.getConcept(ArchitectonicAuxiliary.Concept.PREFERRED_DESCRIPTION_TYPE
										.getUids()));
	}

	private List<I_GetConceptData> getListSpecial() throws Exception {
		I_TermFactory termFactory = LocalVersionedTerminology.get();
		final List<I_GetConceptData> ret = new ArrayList<I_GetConceptData>();
		termFactory.iterateConcepts(new I_ProcessConcepts() {
			public void processConcept(I_GetConceptData concept)
					throws Exception {
				if (concept.getInitialText().matches("^\\d.*")
						|| !concept
								.getInitialText()
								.matches(
										"(\\w|\\s|[()\\-\\+\\[\\]'\\.,\\?\\:\\;\\/\\%\\{\\}\\&\\^\\<\\>])*"))
					ret.add(concept);
			}
		});
		return ret;
	}

	private List<I_GetConceptData> getListUuid() throws Exception {
		I_TermFactory termFactory = LocalVersionedTerminology.get();
		// 253fe8f9-c9a5-473e-a5bc-566d20e85fc8
		I_GetConceptData con = termFactory.getConcept(Arrays.asList(UUID
				.fromString("253fe8f9-c9a5-473e-a5bc-566d20e85fc8")));
		return Arrays.asList(con);
	}

	private I_GetConceptData getRefsetConcept() throws Exception {
		I_TermFactory termFactory = LocalVersionedTerminology.get();
		I_GetConceptData con = termFactory.getConcept(Arrays.asList(UUID
				.fromString(refSetUuid)));
		if (con == null)
			throw new Exception("No concept for " + refSetUuid);
		if (!con.getInitialText().equals(refSetName))
			throw new Exception("Name mismatch: " + con.getInitialText()
					+ " <> " + refSetName);
		return con;
	}

	private class RefsetBuilder extends RefsetUtilities {

		public RefsetBuilder(I_TermFactory termFactory) {
			this.termFactory = termFactory;
			try {
				// for (I_Path p :termFactory.getPaths()) {
				// if (termFactory
				// .getConcept(ArchitectonicAuxiliary.Concept.SNOMED_CORE
				// .getUids()).getConceptId() == p.getConceptId()) {
				// System.out.println("Path: " + p);
				// this.pathConcept = p.
				// }
				this.pathConcept = termFactory
						.getConcept(ArchitectonicAuxiliary.Concept.TEST
								.getUids());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	private void buildRefset() throws Exception {
		I_TermFactory termFactory = LocalVersionedTerminology.get();
		I_GetConceptData refset = getRefsetConcept();
		I_GetConceptData include_individual = termFactory
				.getConcept(RefsetAuxiliary.Concept.INCLUDE_INDIVIDUAL
						.getUids());
		RefsetBuilder rb = new RefsetBuilder(termFactory);
		for (Concept c : Concept.values()) {
			System.out.println("Concept:" + c.name());
			I_GetConceptData member = termFactory.getConcept(c.getUids());
			System.out.println("Member: " + member.getUids().get(0));
			System.out.println("Include: "
					+ include_individual.getUids().get(0));
			System.out.println("Refset: " + refset.getUids().get(0));
			rb.addToMemberSet(member.getConceptId(), include_individual
					.getConceptId(), refset.getConceptId());
		}
	}

	public List<I_GetConceptData> getRefsetConcepts() throws Exception {
		I_TermFactory termFactory = LocalVersionedTerminology.get();
		List<I_GetConceptData> ret = new ArrayList<I_GetConceptData>();
		I_GetConceptData con = getRefsetConcept();
		for (I_ThinExtByRefVersioned mem : termFactory
				.getRefsetExtensionMembers(con.getConceptId())) {
			I_GetConceptData mem_con = termFactory.getConcept(mem
					.getComponentId());
			ret.add(mem_con);
		}
		return ret;
	}

	// <execution>
	// <id>generate-concept-spec</id>
	// <phase>generate-sources</phase>
	// <goals>
	// <goal>generate-concept-spec</goal>
	// </goals>
	// <configuration>
	// <packageName>org.ihtsdo</packageName>
	// <className>IhtsdoConceptSpec</className>
	// <refSetUuid>253fe8f9-c9a5-473e-a5bc-566d20e85fc8</refSetUuid>
	// <refSetName>spec</refSetName>
	// </configuration>
	// </execution>

	public void execute() throws MojoExecutionException, MojoFailureException {
		try {
			buildRefset();
			String dir = outputDirectory + File.separator
					+ packageName.replace(".", File.separator) + File.separator;
			System.out.println("dir:" + dir);
			new File(dir).mkdirs();
			PrintWriter out = new PrintWriter(new BufferedWriter(
					new FileWriter(dir + className + ".java")));
			out.println("package " + packageName + ";");
			out.println();
			out.println("import org.dwfa.tapi.spec.ConceptSpec;");
			out.println();
			// DateFormat df = DateFormat.getDateTimeInstance();
			// df.setTimeZone(TimeZone.getTimeZone("GMT"));
			out.println("// " + new Date().toGMTString());
			out.println("// " + refSetUuid);
			out.println("// " + refSetName);
			out.println("public class " + className);
			out.println("{");
			out.println();
			Set<String> var_names = new HashSet<String>();
			for (I_GetConceptData con : getRefsetConcepts()) {
				String con_name = con.getInitialText();
				System.out.println(con_name);
				String var_name = con_name.replaceAll("\\W", "_");
				var_name = var_name.toUpperCase();
				if (var_name.matches("^\\d.*"))
					var_name = "$" + var_name;
				{
					int i = 0;
					String orig_var_name = var_name;
					while (var_names.contains(var_name)) {
						var_name = orig_var_name + "$" + i;
						i++;
					}
				}
				var_names.add(var_name);
				String str_lit = con_name;
				str_lit = str_lit.replace("\\", "\\\\");
				str_lit = str_lit.replace("\"", "\\\"");
				out.println("// " + con_name);
				out.println("public static final ConceptSpec");
				out.println("\t" + var_name + " = new ConceptSpec(");
				out.println("\t\"" + str_lit + "\", \"" + con.getUids().get(0)
						+ "\");");
				out.println();
			}
			out.println("}");
			out.close();
		} catch (Exception e) {
			throw new MojoExecutionException(e.getLocalizedMessage(), e);
		}
	}
}
