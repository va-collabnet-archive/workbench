package org.ihtsdo.mojo.release;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.maven.plugin.AbstractMojo;
import org.dwfa.util.id.Type5UuidFactory;
import org.ihtsdo.idgeneration.IdAssignmentImpl;

/**
 * @goal create-relids-check-descriptions
 */
public class TemporaryTest extends AbstractMojo {

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
	private String variantFilePath;

	/**
	 * Location of the build directory.
	 * 
	 * @parameter expression="${project.build.directory}"
	 * @required
	 */
	private String releaseFolder;

	private BufferedWriter mappingBw;

	private static IdAssignmentImpl idGen;
	private static HashMap<String, UUID> extSctidUUID = new HashMap<String, UUID>();
	private static HashMap<String, String> ukset;
	private static HashMap<String, String> usset;
	private static final String FSN = "900000000000003001";
	private static final String GB_REFSET_ID = "900000000000508004";
	private static final String US_REFSET_ID = "900000000000509007";

	public void execute() {
		String endpointURL = "http://mgr.servers.aceworkspace.net:50042/axis2/services/id_generator";
		String username = "termmed";
		String password = "termmed";
		//
		idGen = new IdAssignmentImpl(endpointURL, username, password);
		//
		File folder = new File(releaseFolder);
		//
		processRecursivly(folder);
		processDescriptions();

		File mappingFile = new File(targetDirectory, "new_descriptions_sctid_uudi_mapping.txt");
		OutputStreamWriter mappingOsw;
		try {
			mappingOsw = new OutputStreamWriter(new FileOutputStream(mappingFile));
			mappingBw = new BufferedWriter(mappingOsw);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		createLanguageRefset(folder);

	}

	private void createLanguageRefset(File file) {
		try {
			if (file.isDirectory()) {
				File[] subfiles = file.listFiles();
				for (File file2 : subfiles) {
					createLanguageRefset(file2);
				}
			} else {
				if (file.getName().contains("Description")) {
					BufferedReader br = null;
					BufferedWriter langRefsetWriter = null;

					File outputFolder = new File(file.getParent());
					outputFolder.mkdirs();

					// NEW LANGUAGE REFSET FILE
					File outputFile = new File(outputFolder, "new_" + "langrefset.txt");
					System.out.println("Creating " + outputFile.getName());

					FileOutputStream lfos = new FileOutputStream(outputFile);

					// NEW DESCRIPTION FILE REMOVING LAST COLUMN
					File descFile = new File(outputFolder, "new_" + file.getName());
					System.out.println("Creating " + descFile.getName());

					FileOutputStream newDescFos = new FileOutputStream(descFile);
					BufferedWriter descBw = null;

					try {
						System.out.println();
						System.out.println();
						System.out.println("PROCESING " + file.getName());
						System.out.println();
						InputStreamReader fis = new InputStreamReader(new FileInputStream(file), "UTF-8");
						br = new BufferedReader(fis);

						OutputStreamWriter ukOsw = new OutputStreamWriter(newDescFos, "UTF-8");
						descBw = new BufferedWriter(ukOsw);

						descBw.write("id	effectiveTime	active	moduleId	conceptId	languageCode	typeId	term	caseSignificanceId");
						descBw.newLine();

						OutputStreamWriter dosw = new OutputStreamWriter(lfos, "UTF-8");
						langRefsetWriter = new BufferedWriter(dosw);

						String firstLine = br.readLine();
						langRefsetWriter.write("id	effectiveTime	active	moduleId	refsetId	referencedComponentId	acceptabilityId");
						langRefsetWriter.newLine();
						// ukBw.write("id	effectiveTime	active	moduleId	refsetId	referencedComponentId	acceptabilityId");
						// ukBw.newLine();
						while (br.ready()) {
							// Increment concept id count
							String line = br.readLine();
							String[] part = line.split("\\t", -1);

							// Writing new description file without the last
							// column
							for (int i = 0; i < part.length; i++) {
								if (i <= part.length - 2) {
									if (i < part.length - 2) {
										descBw.write(part[i] + "\t");
									} else if (i == part.length - 2) {
										descBw.write(part[i]);
									}
								}
							}

							String[] langRefsetLine = convertDescLineToLangLine(part);
							for (String string : langRefsetLine) {
								langRefsetWriter.write(string);
								langRefsetWriter.newLine();
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
					} finally {
						try {
							br.close();
							langRefsetWriter.close();
							file.delete();
							descFile.renameTo(new File(outputFile.getName().replaceAll("new_", "")));
						} catch (Exception e) {
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void processDescriptions() {
		File variants = new File(variantFilePath);
		BufferedReader br = null;
		try {
			InputStreamReader fis = new InputStreamReader(new FileInputStream(variants), "UTF-8");
			br = new BufferedReader(fis);
			br.readLine();
			ukset = new HashMap<String, String>();
			usset = new HashMap<String, String>();
			while (br.ready()) {
				String line = br.readLine();
				String[] splited = line.split("\\|");
				if (splited.length > 1) {
					ukset.put(splited[0], splited[1]);
					usset.put(splited[1], splited[0]);
				}
			}

			File folder = new File(releaseFolder);

			processDescriptionsRecursivly(folder);

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				br.close();
			} catch (Exception e) {
			}
		}
	}

	private void processDescriptionsRecursivly(File file) {
		try {
			if (file.isDirectory()) {
				File[] subfiles = file.listFiles();
				for (File file2 : subfiles) {
					processDescriptionsRecursivly(file2);
				}
			} else {
				if (file.getName().contains("Description")) {
					BufferedReader br = null;
					BufferedWriter newDescriptions = null;

					File outputFolder = new File(file.getParent());
					outputFolder.mkdirs();

					// NEW DESCRIPTIONS FILE
					File outputFile = new File(outputFolder, "new_" + file.getName());
					System.out.println("Creating " + outputFile.getName());

					FileOutputStream dfos = new FileOutputStream(outputFile);

					try {
						System.out.println();
						System.out.println();
						System.out.println("PROCESING " + file.getName());
						System.out.println();
						InputStreamReader fis = new InputStreamReader(new FileInputStream(file), "UTF-8");
						br = new BufferedReader(fis);

						OutputStreamWriter dosw = new OutputStreamWriter(dfos, "UTF-8");
						newDescriptions = new BufferedWriter(dosw);

						String firstLine = br.readLine();
						newDescriptions.write(firstLine + "\tnewlagrefset");
						newDescriptions.newLine();
						HashMap<String, Integer> conceptIdCount = new HashMap<String, Integer>();
						while (br.ready()) {
							// Increment concept id count
							String line = br.readLine();
							line = line.replaceAll("en-GB", "en");
							String[] part = line.split("\\t", -1);
							if (conceptIdCount.containsKey(part[4])) {
								Integer count = conceptIdCount.get(part[4]);
								count = count + 1;
								conceptIdCount.put(part[4], count);
							} else {
								conceptIdCount.put(part[4], 1);
							}

							if (part[6].equals(FSN)) {
								Set<String> ukkeyset = ukset.keySet();
								String description = part[7];
								String usDesk = description;
								for (String string : ukkeyset) {
									Pattern p = Pattern.compile("\\b" + string + "\\b");
									Matcher m = p.matcher(usDesk);
									if (m.find()) {
										System.out.println("FSN contains uk text : " + usDesk);
										usDesk = m.replaceAll(ukset.get(string));
										System.out.println("FSN converted us form: " + usDesk);
									}
								}
								newDescriptions.write(line.replaceAll(part[7], usDesk));
								newDescriptions.write(US_REFSET_ID);
								newDescriptions.newLine();
							} else {

								String description = part[7];
								String ukDesk = description;
								String usDesk = description;

								Set<String> uskeyset = usset.keySet();

								boolean found = false;
								UUID generatedUuid = Type5UuidFactory.get(part[0] + part[4]);
								Long sctid = idGen.createSCTID(generatedUuid, 0, "1", "20120731", "Whatever", "194721000142105");
								mappingBw.write(generatedUuid + "\t" + sctid.toString());
								mappingBw.newLine();
								for (String string : uskeyset) {
									Pattern p = Pattern.compile("\\b" + string + "\\b");
									Matcher m = p.matcher(ukDesk);
									if (m.find()) {
										found = true;
										// DESCRIPTION CONTAINS US WORD
										System.out.println("\nDescription contains us word");
										ukDesk = m.replaceAll(usset.get(string));
										System.out.println("writing original line: " + line + "\t" + US_REFSET_ID);
										newDescriptions.write(line + "\t" + US_REFSET_ID);
										newDescriptions.newLine();
										if (!ukDesk.equals(line)) {
											String newLine = line.replaceAll(part[7], ukDesk);
											System.out.println("writing new line     : " + newLine.replaceAll(part[0], sctid.toString()) + "\t" + GB_REFSET_ID);
											newDescriptions.write(newLine.replaceAll(part[0], sctid.toString()) + "\t" + GB_REFSET_ID);
											newDescriptions.newLine();
										}
									}
								}

								Set<String> ukkeyset = ukset.keySet();
								for (String string : ukkeyset) {
									Pattern p = Pattern.compile("\\b" + string + "\\b");
									Matcher m = p.matcher(usDesk);
									if (m.find()) {
										found = true;
										// DESCRIPTION CONTAINS UK WORD
										System.out.println("\n Description contains UK word");
										System.out.println("writing original line: " + line + "\t" + GB_REFSET_ID);
										newDescriptions.write(line + "\t" + GB_REFSET_ID);
										newDescriptions.newLine();
										usDesk = m.replaceAll(ukset.get(string));
										if (!usDesk.equals(line)) {
											String newLine = line.replaceAll(part[7], usDesk);
											newDescriptions.write(newLine.replaceAll(part[0], sctid.toString()) + "\t" + US_REFSET_ID);
											System.out.println("writing new line     : " + newLine.replaceAll(part[0], sctid.toString()) + "\t" + US_REFSET_ID);
											newDescriptions.newLine();
										}
									}
								}

								if (!found) {
									newDescriptions.write(line + "\t" + US_REFSET_ID);
									newDescriptions.newLine();
								}
							}
						}
						Set<String> concepts = conceptIdCount.keySet();
						for (String string : concepts) {
							if (conceptIdCount.get(string).intValue() != 2) {
								System.out.println(string);
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
					} finally {
						try {
							br.close();
							newDescriptions.close();
							file.delete();
							outputFile.renameTo(new File(outputFile.getName().replaceAll("new_", "")));
						} catch (Exception e) {
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static String[] convertDescLineToLangLine(String[] part) {
		try {
			UUID langId = Type5UuidFactory.get(part[9] + part[0]);
			if (part[6].equals(FSN)) {
				return new String[] { langId.toString() + "\t" + part[1] + "\t" + "1" + "\t" + part[3] + "\t" + part[9] + "\t" + part[4] + "\t" + "900000000000548007",
						langId.toString() + "\t" + part[1] + "\t" + "1" + "\t" + part[3] + "\t" + GB_REFSET_ID + "\t" + part[4] + "\t" + "900000000000548007" };

			} else {
				return new String[] { langId.toString() + "\t" + part[1] + "\t" + "1" + "\t" + part[3] + "\t" + part[9] + "\t" + part[4] + "\t" + "900000000000548007" };
			}

		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	private void processRecursivly(File file) {
		try {
			if (file.isDirectory()) {
				File[] subfiles = file.listFiles();
				for (File file2 : subfiles) {
					processRecursivly(file2);
				}
			} else {
				if (file.getName().contains("Relationship")) {
					BufferedReader br = null;
					BufferedWriter bw = null;
					BufferedWriter mapperBw = null;

					File outputFolder = new File(file.getParent());
					outputFolder.mkdirs();

					File outputFile = new File(outputFolder, "new_" + file.getName());
					System.out.println("Creating " + outputFile.getName());

					try {
						InputStreamReader fis = new InputStreamReader(new FileInputStream(file), "UTF-8");
						br = new BufferedReader(fis);

						FileOutputStream fos = new FileOutputStream(outputFile);
						OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8");
						bw = new BufferedWriter(osw);

						FileOutputStream mfos = new FileOutputStream(new File(targetDirectory, "NEWMapping.txt"));
						OutputStreamWriter mosw = new OutputStreamWriter(mfos, "UTF-8");
						mapperBw = new BufferedWriter(mosw);

						FileInputStream mfis = new FileInputStream(new File(targetDirectory, "Mapping.txt"));
						InputStreamReader ddd = new InputStreamReader(mfis);
						BufferedReader mbr = new BufferedReader(ddd);

						while (mbr.ready()) {
							mapperBw.write(mbr.readLine());
							mapperBw.newLine();
						}
						mbr.close();

						String firstLine = br.readLine();
						bw.write(firstLine);
						bw.newLine();

						System.out.println("creating all uuids ");
						while (br.ready()) {
							String line = br.readLine();
							String[] part = line.split("\\t", -1);
							UUID uuid = Type5UuidFactory.get(part[4] + part[5] + part[7] + part[6]);
							extSctidUUID.put(part[0], uuid);
						}
						System.out.println("Created " + extSctidUUID.size() + " UUIDS");
						br.close();

						HashMap<UUID, Long> uuidNewSctids = new HashMap<UUID, Long>();

						Set<String> uuidset = extSctidUUID.keySet();
						List<UUID> uuidList = new ArrayList<UUID>();
						String partition = "";
						for (String extSctid : uuidset) {
							partition = extSctid.substring(extSctid.length() - 2, extSctid.length() - 1);
							uuidList.add(extSctidUUID.get(extSctid));
							if (uuidList.size() > 1000) {
								System.out.println("Getting 1000 sctids");
								HashMap<UUID, Long> sctids = idGen.createSCTIDList(uuidList, 0, partition, "20120731", "Whatever", "194721000142105");
								uuidNewSctids.putAll(sctids);
								uuidList = null;
								uuidList = new ArrayList<UUID>();
							}
						}
						if (uuidList.size() <= 1000) {
							System.out.println("Getting 1000 sctids");
							HashMap<UUID, Long> sctids = idGen.createSCTIDList(uuidList, 0, partition, "20120731", "Whatever", "194721000142105");
							uuidNewSctids.putAll(sctids);
							uuidList = null;
						}

						// Iteramos de vuelta el archivo para generar ya el
						// nuevo
						// con el new sctid
						fis = new InputStreamReader(new FileInputStream(file), "UTF-8");
						br = new BufferedReader(fis);
						br.readLine();
						System.out.println("Processing original file again to enerate the new result and mapping");
						while (br.ready()) {
							String line = br.readLine();
							String[] part = line.split("\\t", -1);

							String extSctid = part[0];
							UUID uuid = extSctidUUID.get(extSctid);
							Long newSctId = uuidNewSctids.get(uuid);

							bw.write(line.replace(extSctid, newSctId.toString()));
							bw.newLine();
							mapperBw.write(uuid + "\t" + extSctid + "\t" + newSctId);
							mapperBw.newLine();
						}
						mapperBw.close();
						bw.close();
					} catch (Exception e) {
						e.printStackTrace();
					} finally {
						try {
							br.close();
							bw.close();
							file.delete();
							outputFile.renameTo(new File(outputFile.getName().replaceAll("new_", "")));
						} catch (Exception e) {
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
