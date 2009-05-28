package org.dwfa.mojo;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_ProcessConcepts;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.api.ebr.I_ThinExtByRefVersioned;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.cement.RefsetAuxiliary.Concept;

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
	 * The name of the RefSet.
	 * 
	 * @parameter
	 */
	private String refsetName;

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
		Iterator<I_GetConceptData> cons = termFactory.getConceptIterator();
		while (cons.hasNext()) {
			I_GetConceptData c = cons.next();
			if (c.getInitialText().equals(refsetName))
				return c;
		}
		return null;
	}

	public List<I_GetConceptData> getRefsetConcepts(I_GetConceptData con)
			throws Exception {
		I_TermFactory termFactory = LocalVersionedTerminology.get();
		List<I_GetConceptData> ret = new ArrayList<I_GetConceptData>();
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
	// <refsetName>spec</refsetName>
	// </configuration>
	// </execution>

	public void execute() throws MojoExecutionException, MojoFailureException {
		try {
			I_GetConceptData refset_con = getRefsetConcept();
			// buildRefset();
			String dir = outputDirectory + File.separator
					+ packageName.replace(".", File.separator) + File.separator;
			getLog().info("dir:" + dir);
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
			out.println("// " + refset_con.getUids().get(0));
			out.println("// " + refsetName);
			out.println("public class " + className);
			out.println("{");
			out.println();
			Set<String> var_names = new HashSet<String>();
			for (I_GetConceptData con : getRefsetConcepts(refset_con)) {
				String con_name = con.getInitialText();
				getLog().info("Writing spec for concept: " + con_name);
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
