package org.ihtsdo.mojo.release;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.apache.lucene.document.Document;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.ihtsdo.idgeneration.IdAssignmentImpl;
import org.ihtsdo.lucene.SearchResult;
import org.ihtsdo.rf2.util.ExportUtil;

/**
 * @goal create-sctids
 */
public class TestMyTest extends AbstractMojo {

	/**
	 * Location of the build directory.
	 * 
	 * @parameter expression="${project.build.directory}"
	 * @required
	 */
	private File targetDirectory;

	/**
	 * Location of the build directory.
	 * 
	 * @parameter expression="${project.build.directory}"
	 * @required
	 */
	private String conceptFilePath;

	/**
	 * Location of the build directory.
	 * 
	 * @parameter expression="${project.build.directory}"
	 * @required
	 */
	private String releaseFolder;

	private static final I_TermFactory tf = Terms.get();
	public static int[] idCol = { -1 };
	public static HashMap<String, String> conceptIds = new HashMap<String, String>();
	private static IdAssignmentImpl idAssignment;

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		try {

			idAssignment = new IdAssignmentImpl("http://mgr.servers.aceworkspace.net:50042/axis2/services/id_generator", "termmed", "termmed");

			BufferedWriter bw = null;
			try {
				File outputFile = new File(targetDirectory, "Mapping.txt");
				FileOutputStream fos = new FileOutputStream(outputFile);
				OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8");
				bw = new BufferedWriter(osw);
				bw.write("UUID\textension id\tnew sctid");
				bw.newLine();
				createConceptsSctId(bw);

				File folder = new File(releaseFolder);

				recursiveUpdateIds(folder, bw);
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				try {
					bw.close();
				} catch (Exception e) {
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void createConceptsSctId(BufferedWriter bw) {
		File f = new File(conceptFilePath);
		BufferedReader br = null;
		try {
			InputStreamReader fis = new InputStreamReader(new FileInputStream(f), "UTF-8");
			br = new BufferedReader(fis);
			br.readLine();
			HashMap<UUID, String> snomediduuid = new HashMap<UUID, String>();
			while (br.ready()) {
				String line = br.readLine();
				String[] splitedLine = line.split("\\t", -1);
				processConcept(splitedLine, bw, snomediduuid);
				if (snomediduuid.size() > 1000) {
					processMilConcepts(bw, snomediduuid);
					snomediduuid = new HashMap<UUID, String>();
				}
			}
			if (snomediduuid.size() <= 1000) {
				processMilConcepts(bw, snomediduuid);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				br.close();
			} catch (Exception e) {
			}
		}

	}

	private static void processMilConcepts(BufferedWriter bw, HashMap<UUID, String> snomediduuid) throws Exception, IOException {
		Set<UUID> keyset = snomediduuid.keySet();
		List<UUID> uuidList = new ArrayList<UUID>();
		for (UUID uuid : keyset) {
			uuidList.add(uuid);
		}

		HashMap<UUID, Long> newSctId = idAssignment.createSCTIDList(uuidList, 0, "0", "20120731", "Whatever", "194721000142105");

		for (UUID uuid : keyset) {
			conceptIds.put(snomediduuid.get(uuid), newSctId.get(uuid).toString());
			bw.write(uuid + "\t" + snomediduuid.get(uuid) + "\t" + newSctId.get(uuid).toString());
			System.out.println(uuid + "\t" + snomediduuid.get(uuid) + "\t" + newSctId.get(uuid).toString());
			bw.newLine();
		}
	}

	private static void processConcept(String[] splitedLine, BufferedWriter bw, HashMap<UUID, String> snomediduuid) {
		// Individual creation for any component
		String conceptid = splitedLine[0];
		try {

			SearchResult results = tf.doLuceneSearch(conceptid);
			if (results.topDocs.scoreDocs.length > 0) {
				Document doc = results.searcher.doc(results.topDocs.scoreDocs[0].doc);
				int cnid = Integer.parseInt(doc.get("cnid"));
				I_GetConceptData concept = tf.getConcept(cnid);
				snomediduuid.put(concept.getUUIDs().get(0), conceptid.toString());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		String extensionId = "100161000142123";
		String partition = extensionId.substring(extensionId.length() - 3, extensionId.length() - 1);
		System.out.println(partition);
	}

	private static void recursiveUpdateIds(File currentFile, BufferedWriter mappingBr) {
		if (currentFile.isDirectory()) {
			File[] subfiles = currentFile.listFiles();
			for (File file : subfiles) {
				recursiveUpdateIds(file, mappingBr);
			}
		} else {
			BufferedReader br = null;
			BufferedWriter bw = null;
			File outputFile = new File(currentFile.getParent(), "new_" + currentFile.getName());

			FileOutputStream fos = null;
			OutputStreamWriter osw = null;
			try {
				InputStreamReader fis = new InputStreamReader(new FileInputStream(currentFile), "UTF-8");
				br = new BufferedReader(fis);
				
				fos = new FileOutputStream(outputFile);
				osw = new OutputStreamWriter(fos);
				bw = new BufferedWriter(osw);

				String firstLine = br.readLine();
				bw.write(firstLine);
				bw.newLine();
				idCol = getIdCol(firstLine);

				while (br.ready()) {
					String line = br.readLine();
					String[] splitedLine = line.split("\\t", -1);
					processLine(splitedLine, bw, mappingBr);
				}
				br.close();
				bw.close();
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				try {
					currentFile.delete();
					outputFile.renameTo(new File(outputFile.getName().replaceAll("new_", "")));
					br.close();
					bw.close();
				} catch (Exception e) {
				}
			}
		}

	}

	private static int[] getIdCol(String line) {
		if (line.contains("id	effectiveTime	active	moduleId	refsetId	referencedComponentId	valueId")) {
			return new int[] { 5 };
		} else if (line.contains("id	effectiveTime	active	moduleId	refsetId	referencedComponentId	acceptabilityId")) {
			return new int[] { 5 };
		} else if (line.contains("id	effectiveTime	active	moduleId	refsetId	referencedComponentId")) {
			return new int[] { 5 };
		} else if (line.contains("id	effectiveTime	active	moduleId	definitionStatusId")) {
			return new int[] { 0, 4 };
		} else if (line.contains("id	effectiveTime	active	moduleId	sourceId	destinationId	relationshipGroup	typeId	characteristicTypeId	modifierId")) {
			return new int[] { 0, 4, 5 };
		}
		return new int[] { 0 };
	}

	private static void processLine(String[] splitedLine, BufferedWriter bw, BufferedWriter mappingBr) {
		try {
			String[] resultLine = new String[splitedLine.length];
			for (int i = 0; i < splitedLine.length; i++) {
				resultLine[i] = splitedLine[i];
			}
			for (int index : idCol) {
				String extensionId = splitedLine[index];
				if (conceptIds.containsKey(extensionId)) {
					resultLine[index] = conceptIds.get(extensionId);
				} else {
					SearchResult results = tf.doLuceneSearch(extensionId);
					String partition = extensionId.substring(extensionId.length() - 2, extensionId.length() - 1);
					if (results.topDocs.scoreDocs.length > 0) {
						Document doc = results.searcher.doc(results.topDocs.scoreDocs[0].doc);
						int cnid = Integer.parseInt(doc.get("cnid"));
						I_GetConceptData concept = tf.getConcept(cnid);
						String cid = ExportUtil.getConceptId(concept);
						Long newSctId = idAssignment.createSCTID(concept.getUUIDs().get(0), 0, partition, "20120731", "Whatever", "194721000142105");
						conceptIds.put(cid, newSctId.toString());
						mappingBr.write(concept.getUUIDs().get(0) + "\t" + cid + "\t" + newSctId);
						mappingBr.newLine();
						resultLine[index] = newSctId.toString();
					}
				}
			}
			for (int i = 0; i < resultLine.length; i++) {
				bw.write(resultLine[i]);
				if (i < resultLine.length - 1) {
					bw.write("\t");
				} else {
					bw.newLine();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
