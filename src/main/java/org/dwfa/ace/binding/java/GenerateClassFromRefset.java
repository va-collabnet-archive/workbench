package org.dwfa.ace.binding.java;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_ProcessConcepts;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.api.ebr.I_ThinExtByRefVersioned;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.cement.RefsetAuxiliary.Concept;

/**
 * 
 * Generate ConceptSpec Java file from a given Refset
 * 
 */
public class GenerateClassFromRefset {

	/**
	 * The name of the RefSet.
	 */
	private String refsetName;

	/**
	 * The Uuid of the RefSet.
	 */
	private String refsetUuid;

	/**
	 * The Java package name.
	 */
	private String packageName;

	/**
	 * The Java class name.
	 */
	private String className;

	/**
	 * The Java file output location.
	 */
	private File outputDirectory;

	public String getRefsetName() {
		return refsetName;
	}

	public void setRefsetName(String refsetName) {
		this.refsetName = refsetName;
	}

	public String getRefsetUuid() {
		return refsetUuid;
	}

	public void setRefsetUuid(String refsetUuid) {
		this.refsetUuid = refsetUuid;
	}

	public String getPackageName() {
		return packageName;
	}

	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public File getOutputDirectory() {
		return outputDirectory;
	}

	public void setOutputDirectory(File outputDirectory) {
		this.outputDirectory = outputDirectory;
	}

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
		// Iterator<I_GetConceptData> cons = termFactory.getConceptIterator();
		// while (cons.hasNext()) {
		// I_GetConceptData c = cons.next();
		// if (c.getInitialText().equals(refsetName))
		// return c;
		// }
		// return null;
		I_GetConceptData con = termFactory.getConcept(new UUID[] { UUID
				.fromString(this.refsetUuid) });
		if (!this.refsetName.equals(con.getInitialText()))
			throw new Exception("refsetName != name from refsetUuid");
		return con;
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

	public void run() throws Exception {
		I_GetConceptData refset_con = getRefsetConcept();
		// buildRefset();
		String dir = outputDirectory + File.separator
				+ packageName.replace(".", File.separator) + File.separator;
		new File(dir).mkdirs();
		PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(dir
				+ className + ".java")));
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
	}

}
