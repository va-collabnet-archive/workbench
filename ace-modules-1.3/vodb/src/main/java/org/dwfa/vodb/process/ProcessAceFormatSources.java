/**
 * Copyright (c) 2009 International Health Terminology Standards Development
 * Organisation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dwfa.vodb.process;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StreamTokenizer;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.dwfa.ace.log.AceLog;
import org.dwfa.util.AceDateFormat;
import org.dwfa.util.io.JarExtractor;

import com.sleepycat.je.DatabaseException;

public abstract class ProcessAceFormatSources extends ProcessSources {

	enum REFSET_FILE_TYPES {
		BOOLEAN("boolean.refset"), CONCEPT("concept.refset"), CONINT(
				"conint.refset"), MEASUREMENT("measurement.refset"), INTEGER("integer.refset"),
				LANGUAGE("language.refset"), STRING("string.refset");

		private String fileNameSuffix;

		REFSET_FILE_TYPES(String fileName) {
			this.fileNameSuffix = fileName;
		}

        public String getFileNameSuffix() {
            return fileNameSuffix;
        }
	};

	private static class NamedThreadFactory implements ThreadFactory {
	    private int threadCount = 0;
	    private String factoryName = "factory";

	    public NamedThreadFactory(String factoryName) {
            super();
            this.factoryName = factoryName;
        }

        public Thread newThread(Runnable r) {
	        Thread t = new Thread(r);
	        t.setName(factoryName + "-" + threadCount++);
	        return t;
	      }

	}

    protected static ExecutorService executors = Executors.newFixedThreadPool(8, new NamedThreadFactory("import executor"));

    private static ExecutorService refsetExecutors = Executors.newFixedThreadPool(6, new NamedThreadFactory("refset executor"));

	public ProcessAceFormatSources() throws DatabaseException {
		super(false);
	}

	public static int countLines(File file) throws IOException
    {
        Reader reader = new InputStreamReader(new FileInputStream(file));

        int lineCount = 0;
        char[] buffer = new char[4096];
        for (int charsRead = reader.read(buffer); charsRead >= 0; charsRead = reader.read(buffer))
        {
            for (int charIndex = 0; charIndex < charsRead ; charIndex++)
            {
                if (buffer[charIndex] == '\n')
                    lineCount++;
            }
        }
        reader.close();
        return lineCount;
    }

	public void executeSnomed(File constantDir) throws Exception {

		DateFormat dateFormat = AceDateFormat.getRf1DirectoryDateFormat();

		for (File releaseDir : constantDir.listFiles(new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return name.startsWith(".") == false;
			}
		})) {
			getLog().info("Release directory: " + releaseDir.getName());
			Date releaseDate = dateFormat.parse(releaseDir.getName());

			addReleaseDate(releaseDate);

            Comparator<File> fileComparator = new Comparator<File>() {
                public int compare(File o1, File o2) {
                    return o1.getName().compareTo(o2.getName());
                }
            };
            SortedSet<File> sortedFiles = new TreeSet<File>();
			for (File contentFile : releaseDir.listFiles(new FilenameFilter() {
				public boolean accept(File dir, String name) {
					return name.endsWith(".txt")
							&& (name.equals("ids.txt") == false);
				}
			})) {
                sortedFiles.add(contentFile);
            }

            for (File contentFile : sortedFiles) {
			    int lineCount = countLines(contentFile);
				getLog().info("Content file: " + contentFile.getName() + " has lines: " + lineCount);



				FileReader fr = new FileReader(contentFile);
				BufferedReader br = new BufferedReader(fr);



				if (contentFile.getName().startsWith("concepts")) {
					readConcepts(br, releaseDate, FORMAT.SNOMED,  new CountDownLatch(Integer.MAX_VALUE));
				} else if (contentFile.getName().startsWith("descriptions")) {
					readDescriptions(br, releaseDate, FORMAT.SNOMED,  new CountDownLatch(Integer.MAX_VALUE));
				} else if (contentFile.getName().startsWith("relationships")) {
					readRelationships(br, releaseDate, FORMAT.SNOMED,  new CountDownLatch(Integer.MAX_VALUE));
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
			readIds(br, new CountDownLatch(Integer.MAX_VALUE));
		}
		cleanupSNOMED(null);
	}

	public static enum FORMAT {
		SNOMED, ACE
	};

	private class LoadDescriptionCallable implements Callable<Boolean> {

        private File dataFile;
        private FORMAT format;
        private CountDownLatch descriptionLatch;
        String encoding;

        private LoadDescriptionCallable(File dataFile, String encoding, FORMAT format, CountDownLatch descriptionLatch) {
            super();
            this.dataFile = dataFile;
            this.format = format;
            this.descriptionLatch = descriptionLatch;
            this.encoding = encoding;
        }

        public Boolean call() throws Exception {
        	try {
        		Reader isr = new BufferedReader(new InputStreamReader(new FileInputStream(dataFile), encoding));
        		readDescriptions(isr, null, format, descriptionLatch);
        		isr.close();
        		return true;
        	} catch (RuntimeException ex) {
        		AceLog.getAppLog().nonModalAlertAndLogException(ex);
        		throw new Exception(ex);
        	}
        }

	}

	/**
	 * This is the one I think we will support going forward.
	 * @param dataDir
	 * @param encoding
	 * @throws Exception
	 */
	public void executeFromDir(File dataDir, String encoding) throws Exception {
		FORMAT format = FORMAT.ACE;
		File[] dataFiles = dataDir.listFiles(new FileFilter() {
			public boolean accept(File f) {
				return f.getName().endsWith(".txt")
						&& ((f.getName().equals("ids.txt") == false)
						&& (f.getName().toLowerCase().contains("_report") == false));
			}
		});

        HashMap<String, CountDownLatch> latchMap = new HashMap<String, CountDownLatch>();
        HashMap<String, Future<Boolean>> futureMap = new HashMap<String, Future<Boolean>>();

        SortedSet<File> sortedDataFiles = new TreeSet<File>(Arrays.asList(dataFiles));
        for (File dataFile : sortedDataFiles) {
            int lineCount = countLines(dataFile);
            getLog().info("Content file: " + dataFile.getName() + " has lines: " + lineCount);

            getLog().info(dataFile.getName());
			if (dataFile.getName().contains("concepts")) {
			    CountDownLatch conceptLatch = new CountDownLatch(lineCount);
				Reader isr = new BufferedReader(new InputStreamReader(new FileInputStream(dataFile), encoding));
				readConcepts(isr, null, format, conceptLatch);
				isr.close();
				getLog().info("Awaiting concept latch: " + conceptLatch.getCount());
				conceptLatch.await();
			} else if (dataFile.getName().contains("descriptions")) {
                CountDownLatch descriptionLatch = new CountDownLatch(lineCount);
                latchMap.put("descriptions", descriptionLatch);
                LoadDescriptionCallable descriptionLoader = new LoadDescriptionCallable(dataFile, encoding, format, descriptionLatch);
                Future<Boolean> future = executors.submit(descriptionLoader);
                futureMap.put("descriptions", future);
				getLog().info("Awaiting description latch: " + descriptionLatch.getCount());
                descriptionLatch.await();
			} else if (dataFile.getName().contains("relationships")) {
                CountDownLatch relationshipLatch = new CountDownLatch(lineCount);
				Reader isr = new BufferedReader(new InputStreamReader(new FileInputStream(dataFile), encoding));
				readRelationships(isr, null, format, relationshipLatch);
				isr.close();
                getLog().info("Awaiting relationshipLatch: " + relationshipLatch.getCount());
                relationshipLatch.await();
			} else {
				getLog().info(
						"(2) Don't know what to do with file: "
								+ dataFile.getName());
			}
		}

		for (String latchKey: latchMap.keySet()) {
		    CountDownLatch latch =latchMap.get(latchKey);
		    getLog().info("awating latch: " + latchKey + " current count: " + latch.getCount());
		    latch.await();
		    Future<Boolean> future = futureMap.get(latchKey);
		    if (future != null) {
		        getLog().info("Found future: " + future);
		        future.get();
		    }
		}

	    HashMap<String, CountDownLatch> refsetLatchMap = new HashMap<String, CountDownLatch>();
	    HashMap<String, Future<Boolean>> refsetFutureMap = new HashMap<String, Future<Boolean>>();

		for (final REFSET_FILE_TYPES refsetFileType : REFSET_FILE_TYPES.values()) {
		    File[] matchingFiles = dataDir.listFiles(new FileFilter() {
                public boolean accept(File f) {
                    if (f.getName().toLowerCase().endsWith(refsetFileType.getFileNameSuffix().toLowerCase())) {
                        return true;
                    }
                    return false;
                }
		    });

		    if (matchingFiles != null) {
		        for (File match: matchingFiles) {
	                getLog().info("Refset file: " + match);
	                int lineCount = countLines(match);
	                getLog().info("Content file: " + match.getName() + " has lines: " + lineCount);
	                CountDownLatch refsetLatch = new CountDownLatch(lineCount);
	                refsetLatchMap.put(match.toString(), refsetLatch);
	                BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(match), encoding));
	                LoadRefset refsetLoader = new LoadRefset(br, refsetFileType, match, refsetLatch);
	                Future<Boolean> future = refsetExecutors.submit(refsetLoader);
	                refsetFutureMap.put(match.toString(), future);
		        }
		    }
		}

        for (String latchKey: refsetLatchMap.keySet()) {
            CountDownLatch latch =refsetLatchMap.get(latchKey);
            getLog().info("awaiting refset latch: " + latchKey + " current count: " + latch.getCount());
            latch.await();
        }

        for (String futureKey: refsetFutureMap.keySet()) {
            Future<Boolean> future =refsetFutureMap.get(futureKey);
            CountDownLatch latch =refsetLatchMap.get(futureKey);
            getLog().info("awaiting refset future: " + futureKey + " latch count: " + latch.getCount());
            future.get();
        }

        getLog().info("flushing id buffer.");
		flushIdBuffer();
        getLog().info("Done flushing id buffer.");

		// Do the id file last...
		File idFile = new File(dataDir, "ids.txt");
		if (idFile.exists()) {
			getLog().info("Id file: " + idFile.getName());
            int lineCount = countLines(idFile);
            getLog().info("Content file: " + idFile.getName() + " has lines: " + lineCount);
            CountDownLatch idLatch = new CountDownLatch(lineCount);
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(idFile), encoding));
			readIds(br, idLatch);
            getLog().info("Awaiting idLatch: " + idLatch.getCount());
            idLatch.await();
		}

		writeNewPaths();

		cleanupSNOMED(null);
	}

	protected abstract void flushIdBuffer() throws Exception;

	/**
	 * Add all the new paths to the paths store.
	 *
	 * @throws Exception looking up path int id.
	 */
	public abstract void writeNewPaths() throws Exception;

	private class LoadRefset implements Callable<Boolean>  {

	    Reader r;
	    File rf;
	    CountDownLatch refsetLatch;
        private REFSET_FILE_TYPES refsetType;

	    private LoadRefset(Reader r, REFSET_FILE_TYPES refsetType, File rf, CountDownLatch refsetLatch) {
            super();
            this.r = r;
            this.rf = rf;
            this.refsetLatch = refsetLatch;
            this.refsetType = refsetType;
        }

        public Boolean call() throws Exception {
            try {
                long start = System.currentTimeMillis();
                getLog().info("Started load of " + rf);

                startRefsetRead(refsetType, rf);
                StreamTokenizer st = new StreamTokenizer(r);
                st.resetSyntax();
                st.wordChars('\u001F', '\u00FF');
                st.whitespaceChars('\t', '\t');
                st.eolIsSignificant(true);
                int members = 0;

                skipLineOne(st, refsetLatch);
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

                    finishMemberRead(refsetType, st, refsetUuid, memberUuid, statusUuid, componentUuid, statusDate,
                        pathUuid, rf.getAbsolutePath());

                    members++;

                    tokenType = st.nextToken();

                    // CR or LF
                    if (tokenType == 13) { // is CR
                        // LF
                        tokenType = st.nextToken();
                    }

                    refsetLatch.countDown();
                    // Beginning of loop
                    tokenType = st.nextToken();
                    while (tokenType == 10) {
                        tokenType = st.nextToken();
                    }

                    if (members % 100000 == 0) {
                        getLog().info("processed " + members + " members of " + rf);
                    }

                }
                getLog().info("starting finish of " + rf);
                finishRefsetRead(refsetType, rf, refsetLatch);
                r.close();
                getLog().info("Process time: " + (System.currentTimeMillis() - start) + " Parsed members: " + members);
            } catch (Exception ex) {
                ex.printStackTrace();
                throw ex;
            }
            return true;
        }

	}

	private void finishMemberRead(REFSET_FILE_TYPES refsetFileType, StreamTokenizer st, UUID refsetUuid,
            UUID memberUuid, UUID statusUuid, UUID componentUuid, Date statusDate, UUID pathUuid, String readInfo)
            throws Exception {
        switch (refsetFileType) {
        case BOOLEAN:
            readBooleanMember(st, refsetUuid, memberUuid, statusUuid, componentUuid, statusDate, pathUuid, readInfo);
            break;
        case CONCEPT:
            readConceptMember(st, refsetUuid, memberUuid, statusUuid, componentUuid, statusDate, pathUuid, readInfo);
            break;
        case CONINT:
            readConIntMember(st, refsetUuid, memberUuid, statusUuid, componentUuid, statusDate, pathUuid, readInfo);
            break;
        case INTEGER:
            readIntegerMember(st, refsetUuid, memberUuid, statusUuid, componentUuid, statusDate, pathUuid, readInfo);
            break;
        case LANGUAGE:
            readLanguageMember(st, refsetUuid, memberUuid, statusUuid, componentUuid, statusDate, pathUuid, readInfo);
            break;
        case MEASUREMENT:
            readMeasurementMember(st, refsetUuid, memberUuid, statusUuid, componentUuid, statusDate, pathUuid, readInfo);
            break;
        case STRING:
            readStringMember(st, refsetUuid, memberUuid, statusUuid, componentUuid, statusDate, pathUuid, readInfo);
            break;
		default:
			throw new IOException("Can't handle refset type: " + refsetFileType);
		}

	}

	protected abstract void startRefsetRead(REFSET_FILE_TYPES refsetFileType, File rf) throws IOException;

	protected abstract void finishRefsetRead(REFSET_FILE_TYPES refsetFileType, File rf, CountDownLatch refsetLatch) throws IOException, Exception;

    protected abstract void readConIntMember(StreamTokenizer st, UUID refsetUuid, UUID memberUuid, UUID statusUuid,
            UUID componentUuid, Date statusDate, UUID pathUuid, String readInfo) throws IOException, ParseException,
            DatabaseException, Exception;

    protected abstract void readConceptMember(StreamTokenizer st, UUID refsetUuid, UUID memberUuid, UUID statusUuid,
            UUID componentUuid, Date statusDate, UUID pathUuid, String readInfo) throws IOException, ParseException,
            DatabaseException, Exception;

    protected abstract void readBooleanMember(StreamTokenizer st, UUID refsetUuid, UUID memberUuid, UUID statusUuid,
            UUID componentUuid, Date statusDate, UUID pathUuid, String readInfo) throws IOException, ParseException,
            DatabaseException, Exception;

    public void execute(File jarFile, String dataDir, FORMAT format, File extractDir) throws Exception {

	    if (format == FORMAT.ACE) {
            extractJarFile(jarFile, dataDir, extractDir);
	    } else {
	        //Need to depricate this method for processing SNOMED, and inferring history.
	        //Better to have everyone use ace format.
	        JarFile constantJar = new JarFile(jarFile);

	        DateFormat dateFormat = AceDateFormat.getRf1DirectoryDateFormat();
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
	                    readConcepts(isr, releaseDate, format, new CountDownLatch(Integer.MAX_VALUE));
	                    isr.close();
	                } else if (je.getName().contains("descriptions")) {
	                    InputStreamReader isr = new InputStreamReader(constantJar
	                            .getInputStream(je));
	                    readDescriptions(isr, releaseDate, format,  new CountDownLatch(Integer.MAX_VALUE));
	                    isr.close();
	                } else if (je.getName().contains("relationships")) {
	                    InputStreamReader isr = new InputStreamReader(constantJar
	                            .getInputStream(je));
	                    readRelationships(isr, releaseDate, format,  new CountDownLatch(Integer.MAX_VALUE));
	                    isr.close();
	                } else {
	                    getLog().info(
	                            "Don't know what to do with jar entry: "
	                                    + je.getName());
	                }
	            }
	            cleanupSNOMED(null);
	        }
	    }

	}

    private void extractJarFile(final File jarFile, final String dataDir, final File extractDir) throws Exception {
        JarExtractor.execute(jarFile, extractDir);
        AceLog.getEditLog().info("Extracted jar into: " + extractDir.getAbsolutePath());
        File rootDir = new File(extractDir, dataDir);
        AceLog.getEditLog().info("rootDir: " + rootDir.getAbsolutePath());
        File[] subRoots = rootDir.listFiles(new FileFilter() {

            public boolean accept(File pathname) {
                if (pathname.isDirectory() && pathname.getName().length() == "yyyy-MM-dd".length()) {
                    return true;
                }
                return false;
            }
        });
        if (subRoots != null && subRoots.length > 0) {
            rootDir = subRoots[0];
            AceLog.getEditLog().info("Found sub root: " + rootDir.getAbsolutePath());
        }
        executeFromDir(rootDir, "UTF-8");
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

    protected abstract void readMeasurementMember(StreamTokenizer st, UUID refsetUuid, UUID memberUuid,
            UUID statusUuid, UUID componentUuid, Date statusDate, UUID pathUuid, String readInfo) throws Exception;

    protected abstract void readIntegerMember(StreamTokenizer st, UUID refsetUuid, UUID memberUuid, UUID statusUuid,
            UUID componentUuid, Date statusDate, UUID pathUuid, String readInfo) throws Exception;

    protected abstract void readLanguageMember(StreamTokenizer st, UUID refsetUuid, UUID memberUuid, UUID statusUuid,
            UUID componentUuid, Date statusDate, UUID pathUuid, String readInfo) throws Exception;

    protected abstract void readStringMember(StreamTokenizer st, UUID refsetUuid, UUID memberUuid, UUID statusUuid,
            UUID componentUuid, Date statusDate, UUID pathUuid, String readInfo) throws Exception;

}
