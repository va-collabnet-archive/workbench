package org.dwfa.vodb;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StreamTokenizer;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.UUID;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import com.sleepycat.je.DatabaseException;

public abstract class ProcessAceFormatSources extends ProcessSources {

	private enum REFSET_FILES {
		BOOLEAN("boolean.refset"), CONCEPT("concept.refset"), CONINT(
				"conint.refset"), MEASUREMENT("measurement.refset"), INTEGER("integer.refset"), 
				LANGUAGE("language.refset");

		private String fileName;

		REFSET_FILES(String fileName) {
			this.fileName = fileName;
		}

		private File getFile(File rootDir) {
			return new File(rootDir, fileName);
		}
	};

	public ProcessAceFormatSources() throws DatabaseException {
		super(false);
	}

	public void execute(File constantDir) throws Exception {

		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

		for (File releaseDir : constantDir.listFiles(new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return name.startsWith(".") == false;
			}
		})) {
			getLog().info("Release directory: " + releaseDir.getName());
			Date releaseDate = dateFormat.parse(releaseDir.getName());

			addReleaseDate(releaseDate);

			for (File contentFile : releaseDir.listFiles(new FilenameFilter() {
				public boolean accept(File dir, String name) {
					return name.endsWith(".txt")
							&& (name.equals("ids.txt") == false);
				}
			})) {
				getLog().info("Content file: " + contentFile.getName());
				FileReader fr = new FileReader(contentFile);
				BufferedReader br = new BufferedReader(fr);
				if (contentFile.getName().startsWith("concepts")) {
					readConcepts(br, releaseDate, FORMAT.SNOMED);
				} else if (contentFile.getName().startsWith("descriptions")) {
					readDescriptions(br, releaseDate, FORMAT.SNOMED);
				} else if (contentFile.getName().startsWith("relationships")) {
					readRelationships(br, releaseDate, FORMAT.SNOMED);
				} else if (contentFile.getName().startsWith("illicit_words")) {
					getLog().info(
							"Found illicit_words file: "
									+ contentFile.getName());
					readIllicitWords(br);
				} else if (contentFile.getName().startsWith("licit_words")) {
					getLog().info(
							"Found licit_words file: " + contentFile.getName());
					readLicitWords(br);
				} else {
					getLog().info(
							"(1) Don't know what to do with file: "
									+ contentFile.getName());
				}
				br.close();
			}
		}
		// Do the id file last...
		File idFile = new File(constantDir, "ids.txt");
		if (idFile.exists()) {
			getLog().info("Id file: " + idFile.getName());
			FileReader fr = new FileReader(idFile);
			BufferedReader br = new BufferedReader(fr);
			readIds(br);
		}
		cleanup(null);
	}

	public void execute(JarFile constantJar) throws Exception {
		execute(constantJar, "org/jehri/cement/", FORMAT.SNOMED);
	}

	public static enum FORMAT {
		SNOMED, ACE
	};

	public void executeFromDir(File dataDir) throws Exception {
		FORMAT format = FORMAT.ACE;
		File[] dataFiles = dataDir.listFiles(new FileFilter() {
			public boolean accept(File f) {
				return f.getName().endsWith(".txt")
						&& (f.getName().equals("ids.txt") == false);
			}
		});
		for (File dataFile : dataFiles) {
			getLog().info(dataFile.getName());
			if (dataFile.getName().contains("concepts")) {
				Reader isr = new BufferedReader(new FileReader(dataFile));
				readConcepts(isr, null, format);
				isr.close();
			} else if (dataFile.getName().contains("descriptions")) {
				Reader isr = new BufferedReader(new FileReader(dataFile));
				readDescriptions(isr, null, format);
				isr.close();
			} else if (dataFile.getName().contains("relationships")) {
				Reader isr = new BufferedReader(new FileReader(dataFile));
				readRelationships(isr, null, format);
				isr.close();
			} else if (dataFile.getName().contains("illicit_words")) {
				getLog().info(
						"Found illicit_words jar entry: " + dataFile.getName());
				Reader isr = new BufferedReader(new FileReader(dataFile));
				readIllicitWords(isr);
			} else if (dataFile.getName().contains("licit_words")) {
				getLog().info(
						"Found licit_words jar entry: " + dataFile.getName());
				Reader isr = new BufferedReader(new FileReader(dataFile));
				readLicitWords(isr);
			} else {
				getLog().info(
						"(2) Don't know what to do with file: "
								+ dataFile.getName());
			}
		}

		for (REFSET_FILES rf : REFSET_FILES.values()) {
			if (rf.getFile(dataDir).exists()) {
				getLog().info("Refset file: " + rf);
				FileReader fr = new FileReader(rf.getFile(dataDir));
				BufferedReader br = new BufferedReader(fr);
				readRefset(br, rf);
				br.close();
			}
		}

		// Do the id file last...
		File idFile = new File(dataDir, "ids.txt");
		if (idFile.exists()) {
			getLog().info("Id file: " + idFile.getName());
			FileReader fr = new FileReader(idFile);
			BufferedReader br = new BufferedReader(fr);
			readIds(br);
		}
		cleanup(null);
	}

	private void readRefset(Reader r, REFSET_FILES rf) throws IOException,
			Exception {

		long start = System.currentTimeMillis();

		StreamTokenizer st = new StreamTokenizer(r);
		st.resetSyntax();
		st.wordChars('\u001F', '\u00FF');
		st.whitespaceChars('\t', '\t');
		st.eolIsSignificant(true);
		int members = 0;

		skipLineOne(st);
		int tokenType = st.nextToken();
		while (tokenType != StreamTokenizer.TT_EOF) {
			UUID refsetUuid = (UUID) getId(st);
			tokenType = st.nextToken();
			UUID memberUuid = (UUID) getId(st);
			tokenType = st.nextToken();
			UUID statusUuid = (UUID) getId(st);
			tokenType = st.nextToken();
			UUID componentUuid = (UUID) getId(st);
			tokenType = st.nextToken();
			Date statusDate = getDate(st);
			
			tokenType = st.nextToken();
			UUID pathUuid = (UUID) getId(st);
			
			finishMemberRead(rf, st, refsetUuid, memberUuid, statusUuid, componentUuid, statusDate, pathUuid);

			members++;
			
			tokenType = st.nextToken();

			// CR or LF
			if (tokenType == 13) { // is CR
				// LF
				tokenType = st.nextToken();
			}

			// Beginning of loop
			tokenType = st.nextToken();
			while (tokenType == 10) {
				tokenType = st.nextToken();
			}

		}
		getLog().info(
				"Process time: " + (System.currentTimeMillis() - start)
						+ " Parsed members: " + members);
	}

	private void finishMemberRead(REFSET_FILES rf, StreamTokenizer st, UUID refsetUuid,
			UUID memberUuid, UUID statusUuid, UUID componentUuid, Date statusDate, UUID pathUuid) throws Exception {
		switch (rf) {
		case BOOLEAN:
			readBooleanMember(st, refsetUuid, memberUuid, statusUuid, componentUuid, statusDate, pathUuid);
			break;
		case CONCEPT:
			readConceptMember(st, refsetUuid, memberUuid, statusUuid, componentUuid, statusDate, pathUuid);
			break;
		case CONINT:
			readConIntMember(st, refsetUuid, memberUuid, statusUuid, componentUuid, statusDate, pathUuid);
			break;
		case INTEGER:
			readIntegerMember(st, refsetUuid, memberUuid, statusUuid, componentUuid, statusDate, pathUuid);
			break;
		case LANGUAGE:
			readLanguageMember(st, refsetUuid, memberUuid, statusUuid, componentUuid, statusDate, pathUuid);
			break;
		case MEASUREMENT:
			readMeasurementMember(st, refsetUuid, memberUuid, statusUuid, componentUuid, statusDate, pathUuid);
			break;
		default:
			throw new IOException("Can't handle refset type: " + rf);
		}
		
	}

	protected abstract void readConIntMember(StreamTokenizer st, UUID refsetUuid, UUID memberUuid,
			UUID statusUuid, UUID componentUuid, Date statusDate, UUID pathUuid) throws IOException, ParseException, DatabaseException, Exception; 

	protected abstract void readConceptMember(StreamTokenizer st, UUID refsetUuid, UUID memberUuid,
			UUID statusUuid, UUID componentUuid, Date statusDate, UUID pathUuid)throws IOException, ParseException, DatabaseException, Exception; 

	protected abstract void readBooleanMember(StreamTokenizer st, UUID refsetUuid, UUID memberUuid,
			UUID statusUuid, UUID componentUuid, Date statusDate, UUID pathUuid) throws IOException, ParseException, DatabaseException, Exception;

	public void execute(JarFile constantJar, String dataDir, FORMAT format)
			throws Exception {

		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		Enumeration<JarEntry> jarEnum = constantJar.entries();
		while (jarEnum.hasMoreElements()) {
			JarEntry je = jarEnum.nextElement();
			if (je.getName().startsWith(dataDir)
					&& je.getName().endsWith(".txt")) {
				int startIndex = dataDir.length();
				int endIndex = startIndex + "yyyy-MM-dd".length();
				Date releaseDate = dateFormat.parse(je.getName().substring(
						startIndex, endIndex));
				addReleaseDate(releaseDate);
				getLog().info(je.getName());
				if (je.getName().contains("concepts")) {
					InputStreamReader isr = new InputStreamReader(constantJar
							.getInputStream(je));
					readConcepts(isr, releaseDate, format);
					isr.close();
				} else if (je.getName().contains("descriptions")) {
					InputStreamReader isr = new InputStreamReader(constantJar
							.getInputStream(je));
					readDescriptions(isr, releaseDate, format);
					isr.close();
				} else if (je.getName().contains("relationships")) {
					InputStreamReader isr = new InputStreamReader(constantJar
							.getInputStream(je));
					readRelationships(isr, releaseDate, format);
					isr.close();
				} else if (je.getName().contains("illicit_words")) {
					getLog().info(
							"Found illicit_words jar entry: " + je.getName());
					InputStreamReader isr = new InputStreamReader(constantJar
							.getInputStream(je));
					readIllicitWords(isr);
				} else if (je.getName().contains("licit_words")) {
					getLog().info(
							"Found licit_words jar entry: " + je.getName());
					InputStreamReader isr = new InputStreamReader(constantJar
							.getInputStream(je));
					readLicitWords(isr);
				} else {
					getLog().info(
							"Don't know what to do with jar entry: "
									+ je.getName());
				}
			}
			cleanup(null);
		}
	}

	protected Object getId(StreamTokenizer st) {
		if (st.sval.length() != 36) {
			return st.sval;
		}
		return UUID.fromString(st.sval);
	}

	protected Object getDescType(StreamTokenizer st) {
		return UUID.fromString(st.sval);
	}

	protected Object getStatus(StreamTokenizer st) {
		return UUID.fromString(st.sval);
	}

	protected Object getRefinability(StreamTokenizer st) {
		return UUID.fromString(st.sval);
	}

	protected Object getCharacteristic(StreamTokenizer st) {
		return UUID.fromString(st.sval);
	}

	protected abstract void readMeasurementMember(StreamTokenizer st, UUID refsetUuid,
			UUID memberUuid, UUID statusUuid, UUID componentUuid,
			Date statusDate, UUID pathUuid) throws Exception;

	protected abstract void readIntegerMember(StreamTokenizer st, UUID refsetUuid,
			UUID memberUuid, UUID statusUuid, UUID componentUuid,
			Date statusDate, UUID pathUuid) throws Exception;

	protected abstract void readLanguageMember(StreamTokenizer st, UUID refsetUuid,
			UUID memberUuid, UUID statusUuid, UUID componentUuid,
			Date statusDate, UUID pathUuid) throws Exception;

}
