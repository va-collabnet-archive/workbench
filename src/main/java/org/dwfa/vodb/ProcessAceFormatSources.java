package org.dwfa.vodb;

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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.dwfa.ace.log.AceLog;
import org.dwfa.util.io.JarExtractor;

import com.sleepycat.je.DatabaseException;

public abstract class ProcessAceFormatSources extends ProcessSources {

	enum REFSET_FILES {
		BOOLEAN("boolean.refset"), CONCEPT("concept.refset"), CONINT(
				"conint.refset"), MEASUREMENT("measurement.refset"), INTEGER("integer.refset"), 
				LANGUAGE("language.refset"), STRING("string.refset");

		private String fileName;

		REFSET_FILES(String fileName) {
			this.fileName = fileName;
		}

		private File getFile(File rootDir) {
			return new File(rootDir, fileName);
		}
	};
	

    protected static ExecutorService executors = Executors.newFixedThreadPool(8);
    private static ExecutorService refsetExecutors = Executors.newFixedThreadPool(6);



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
				} else if (contentFile.getName().startsWith("illicit_words")) {
					getLog().info(
							"Found illicit_words file: "
									+ contentFile.getName());
					readIllicitWords(br,  new CountDownLatch(Integer.MAX_VALUE));
				} else if (contentFile.getName().startsWith("licit_words")) {
					getLog().info(
							"Found licit_words file: " + contentFile.getName());
					readLicitWords(br, new CountDownLatch(Integer.MAX_VALUE));
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
		cleanup(null);
	}

	public void execute(File constantJar) throws Exception {
		execute(constantJar, "org/jehri/cement/", FORMAT.SNOMED);
	}

	public static enum FORMAT {
		SNOMED, ACE
	};
	
	private class LoadDescriptionCallable implements Callable<Boolean> {

        private File dataFile;
        private FORMAT format;
        private CountDownLatch descriptionLatch;


        private LoadDescriptionCallable(File dataFile, FORMAT format, CountDownLatch descriptionLatch) {
            super();
            this.dataFile = dataFile;
            this.format = format;
            this.descriptionLatch = descriptionLatch;
        }


        public Boolean call() throws Exception {
            Reader isr = new BufferedReader(new FileReader(dataFile));
            readDescriptions(isr, null, format, descriptionLatch);
            isr.close();
            return true;
        }
	    
	}

	/**
	 * This is the one I think we will support going forward. 
	 * @param dataDir
	 * @throws Exception
	 */
	public void executeFromDir(File dataDir) throws Exception {
		FORMAT format = FORMAT.ACE;
		File[] dataFiles = dataDir.listFiles(new FileFilter() {
			public boolean accept(File f) {
				return f.getName().endsWith(".txt")
						&& (f.getName().equals("ids.txt") == false);
			}
		});
		
        HashMap<String, CountDownLatch> latchMap = new HashMap<String, CountDownLatch>();
        HashMap<String, Future<Boolean>> futureMap = new HashMap<String, Future<Boolean>>();
		
		for (File dataFile : dataFiles) {
            int lineCount = countLines(dataFile);
            getLog().info("Content file: " + dataFile.getName() + " has lines: " + lineCount);

            getLog().info(dataFile.getName());
			if (dataFile.getName().contains("concepts")) {
			    CountDownLatch conceptLatch = new CountDownLatch(lineCount);
				Reader isr = new BufferedReader(new FileReader(dataFile));
				readConcepts(isr, null, format, conceptLatch);
				isr.close();
				getLog().info("Awaiting concept latch: " + conceptLatch.getCount());
				conceptLatch.await();
			} else if (dataFile.getName().contains("descriptions")) {
                CountDownLatch descriptionLatch = new CountDownLatch(lineCount);
                latchMap.put("descriptions", descriptionLatch);
                LoadDescriptionCallable descriptionLoader = new LoadDescriptionCallable(dataFile, format, descriptionLatch);
                Future<Boolean> future = executors.submit(descriptionLoader);
                futureMap.put("descriptions", future);
			} else if (dataFile.getName().contains("relationships")) {
                CountDownLatch relationshipLatch = new CountDownLatch(lineCount);
				Reader isr = new BufferedReader(new FileReader(dataFile));
				readRelationships(isr, null, format, relationshipLatch);
				isr.close();
                getLog().info("Awaiting relationshipLatch: " + relationshipLatch.getCount());
                relationshipLatch.await();
			} else if (dataFile.getName().contains("illicit_words")) {
                CountDownLatch illicitWordLatch = new CountDownLatch(lineCount);
				getLog().info(
						"Found illicit_words jar entry: " + dataFile.getName());
				Reader isr = new BufferedReader(new FileReader(dataFile));
				readIllicitWords(isr, illicitWordLatch);
                getLog().info("Awaiting illicitWordLatch: " + illicitWordLatch.getCount());
                illicitWordLatch.await();
			} else if (dataFile.getName().contains("licit_words")) {
                CountDownLatch licitWordLatch = new CountDownLatch(lineCount);
				getLog().info(
						"Found licit_words jar entry: " + dataFile.getName());
				Reader isr = new BufferedReader(new FileReader(dataFile));
				readLicitWords(isr, licitWordLatch);
                getLog().info("Awaiting licitWordLatch: " + licitWordLatch.getCount());
                licitWordLatch.await();
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
        
		for (REFSET_FILES rf : REFSET_FILES.values()) {
			if (rf.getFile(dataDir).exists()) {
				getLog().info("Refset file: " + rf);
	            int lineCount = countLines(rf.getFile(dataDir));
	            getLog().info("Content file: " + rf.getFile(dataDir).getName() + " has lines: " + lineCount);
                CountDownLatch refsetLatch = new CountDownLatch(lineCount);
                refsetLatchMap.put(rf.toString(), refsetLatch);
				FileReader fr = new FileReader(rf.getFile(dataDir));
				BufferedReader br = new BufferedReader(fr);
				LoadRefset refsetLoader = new LoadRefset(br, rf, refsetLatch);
                Future<Boolean> future = refsetExecutors.submit(refsetLoader);
                refsetFutureMap.put(rf.toString(), future);
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
			FileReader fr = new FileReader(idFile);
			BufferedReader br = new BufferedReader(fr);
			readIds(br, idLatch);
            getLog().info("Awaiting idLatch: " + idLatch.getCount());
            idLatch.await();
		}
		cleanup(null);
	}
	
	protected abstract void flushIdBuffer() throws Exception;

	private class LoadRefset implements Callable<Boolean>  {
	    
	    Reader r;
	    REFSET_FILES rf;
	    CountDownLatch refsetLatch;
	    
	    private LoadRefset(Reader r, REFSET_FILES rf, CountDownLatch refsetLatch) {
            super();
            this.r = r;
            this.rf = rf;
            this.refsetLatch = refsetLatch;
        }

        public Boolean call() throws Exception {
            try {
                long start = System.currentTimeMillis();
                getLog().info("Started load of " + rf);

                startRefsetRead(rf);
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

                    finishMemberRead(rf, st, refsetUuid, memberUuid, statusUuid, componentUuid, statusDate, pathUuid);

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
                finishRefsetRead(rf, refsetLatch);
                r.close();
                getLog().info("Process time: " + (System.currentTimeMillis() - start) + " Parsed members: " + members);
            } catch (Exception ex) {
                ex.printStackTrace();
                throw ex;
            }
            return true;
        }

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
		case STRING:
            readStringMember(st, refsetUuid, memberUuid, statusUuid, componentUuid, statusDate, pathUuid);
            break;
		default:
			throw new IOException("Can't handle refset type: " + rf);
		}
		
	}
	
	protected abstract void startRefsetRead(REFSET_FILES rf) throws IOException;
	protected abstract void finishRefsetRead(REFSET_FILES rf, CountDownLatch refsetLatch) throws IOException, Exception;

    protected abstract void readConIntMember(StreamTokenizer st, UUID refsetUuid, UUID memberUuid,
			UUID statusUuid, UUID componentUuid, Date statusDate, UUID pathUuid) throws IOException, ParseException, DatabaseException, Exception; 

	protected abstract void readConceptMember(StreamTokenizer st, UUID refsetUuid, UUID memberUuid,
			UUID statusUuid, UUID componentUuid, Date statusDate, UUID pathUuid)throws IOException, ParseException, DatabaseException, Exception; 

	protected abstract void readBooleanMember(StreamTokenizer st, UUID refsetUuid, UUID memberUuid,
			UUID statusUuid, UUID componentUuid, Date statusDate, UUID pathUuid) throws IOException, ParseException, DatabaseException, Exception;

	public void execute(File jarFile, String dataDir, FORMAT format)
			throws Exception {

	    if (format == FORMAT.ACE) {
	        File extractDir = new File("target", "unjar");
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
	        executeFromDir(rootDir);
	    } else {
	        //Need to depricate this method for processing SNOMED, and inferring history. 
	        //Better to have everyone use ace format. 
	        JarFile constantJar = new JarFile(jarFile);

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
	                } else if (je.getName().contains("illicit_words")) {
	                    getLog().info(
	                            "Found illicit_words jar entry: " + je.getName());
	                    InputStreamReader isr = new InputStreamReader(constantJar
	                            .getInputStream(je));
	                    readIllicitWords(isr,  new CountDownLatch(Integer.MAX_VALUE));
	                } else if (je.getName().contains("licit_words")) {
	                    getLog().info(
	                            "Found licit_words jar entry: " + je.getName());
	                    InputStreamReader isr = new InputStreamReader(constantJar
	                            .getInputStream(je));
	                    readLicitWords(isr,  new CountDownLatch(Integer.MAX_VALUE));
	                } else {
	                    getLog().info(
	                            "Don't know what to do with jar entry: "
	                                    + je.getName());
	                }
	            }
	            cleanup(null);
	        }
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

    protected abstract void readStringMember(StreamTokenizer st, UUID refsetUuid,
        UUID memberUuid, UUID statusUuid, UUID componentUuid,
        Date statusDate, UUID pathUuid) throws Exception;

}
